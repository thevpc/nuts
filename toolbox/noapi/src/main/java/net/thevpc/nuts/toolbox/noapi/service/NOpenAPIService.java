package net.thevpc.nuts.toolbox.noapi.service;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPathExtensionType;
import net.thevpc.nuts.io.NPathNameParts;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.toolbox.noapi.service.docs.ConfigMarkdownGenerator;
import net.thevpc.nuts.toolbox.noapi.service.docs.MainMarkdownGenerator;
import net.thevpc.nuts.toolbox.noapi.model.SupportedTargetType;
import net.thevpc.nuts.toolbox.noapi.util.AppMessages;
import net.thevpc.nuts.toolbox.noapi.util.NoApiUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.*;
import java.util.stream.Collectors;

public class NOpenAPIService {

    private NSession session;
    private AppMessages msg;
    private List<String> defaultAdocHeaders = Arrays.asList(
            ":source-highlighter: coderay",
            ":icons: font",
            ":icon-set: pf",
            ":doctype: book",
            ":toc:",
            ":toclevels: 3",
            ":appendix-caption: Appx",
            ":sectnums:",
            ":chapter-label:"
    );
    ;

    public NOpenAPIService(NSession session) {
        this.session = session;
        msg = new AppMessages(null, getClass().getResource("/net/thevpc/nuts/toolbox/noapi/messages-en.json"), session);
    }

    public NPath resolvePath(String source) {
        NPath sourcePath = NPath.of(source).normalize().toAbsolute();
        if (!sourcePath.exists()) {
            throw new NNoSuchElementException(NMsg.ofC("file not found %s", sourcePath));
        }
        if (sourcePath.isDirectory()) {
            List<NPath> found = sourcePath.list().stream().filter(x -> x.getName().endsWith(".json") && !x.getName().endsWith(".config.json")).collect(Collectors.toList());
            if (found.size() == 1) {
                return found.get(0);
            }
            if (found.isEmpty()) {
                throw new NNoSuchElementException(NMsg.ofC("missing json files in folder %s", sourcePath));
            }
            throw new NNoSuchElementException(NMsg.ofC("too many json files in folder %s", sourcePath));
        }
        return sourcePath;
    }

    public void run(String source, String target, String varsPath, Map<String, String> varsMap, boolean keep) {
        Map<String, String> vars = new HashMap<>();
        if (!NBlankable.isBlank(varsPath)) {
            Map<Object, Object> m = NElements.of().parse(NPath.of(varsPath), Map.class);
            for (Map.Entry<Object, Object> o : m.entrySet()) {
                vars.put(String.valueOf(o.getKey()), String.valueOf(o.getValue()));
            }
        }
        if (varsMap != null) {
            vars.putAll(varsMap);
        }
        NPath sourcePath = resolvePath(source);
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("read open-api file %s", sourcePath));
        }
        String sourceBaseName = sourcePath.getNameParts(NPathExtensionType.SMART).getBaseName();
        NElement apiElement = NoApiUtils.loadElement(sourcePath, session);
        NObjectElement infoObj = apiElement.asObject().get().getObject("info").orElse(NElements.of().ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

//        Path path = Paths.get("/data/from-git/RapiPdf/docs/specs/maghrebia-api-1.1.2.yml");
        SupportedTargetType targetType = NoApiUtils.resolveTarget(target, SupportedTargetType.PDF);
        NPath sourceFolder = sourcePath.getParent();
        NPath parentPath = sourceFolder.resolve("dist-version-" + documentVersion);
        NPath targetPathObj = NoApiUtils.addExtension(sourcePath, parentPath, NPath.of(target), targetType, documentVersion, session);

        //start copying json file
        NPath openApiFileCopy = targetPathObj.resolveSibling(targetPathObj.getNameParts(NPathExtensionType.SMART).getBaseName() + "." + sourcePath.getNameParts(NPathExtensionType.SHORT).getExtension());
        sourcePath.copyTo(openApiFileCopy);
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("copy open-api file %s", openApiFileCopy));
        }

        NPath targetParent = targetPathObj.getParent();

        List<NPath> allConfigFiles = sourceFolder.stream().filter(
                (NPath x) ->
                {
                    return x.getName().endsWith(".config.json")
                            && (
                            x.getName().startsWith(sourceBaseName + ".")
                                    || x.getName().startsWith(sourceBaseName + "-")
                                    || x.getName().startsWith(sourceBaseName + "_")
                    );
                }
        ).withDesc(NEDesc.of("config files")).toList();
        for (NPath cf : allConfigFiles) {
            NElement z = NElements.of().parse(cf);
            //remove version, will be added later
            NPathNameParts smartParts = cf.getNameParts(NPathExtensionType.SMART);
            NPath configFileCopy = targetPathObj.resolveSibling(smartParts.getBaseName() + "-" + documentVersion + "." + smartParts.getExtension());
            cf.copyTo(configFileCopy);
            if (session.isPlainTrace()) {
                session.out().println(NMsg.ofC("copy  config  file %s", configFileCopy));
            }
            NPath targetPathObj2 = NoApiUtils.addExtension(sourcePath, parentPath, NPath.of(target), targetType, "", session);
            generateConfigDocument(z, apiElement, parentPath, sourceFolder, targetPathObj2.getNameParts(NPathExtensionType.SMART).getBaseName(), targetPathObj.getName(), targetType, keep, vars);
        }

        MainMarkdownGenerator mg = new MainMarkdownGenerator(session, msg);
        MdDocument md = mg.createMarkdown(apiElement, sourceFolder, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, targetPathObj, keep, targetType, session);
    }

    private void generateConfigDocument(NElement configElements, NElement apiElement, NPath parentPath, NPath sourceFolder, String baseName, String apiFileName, SupportedTargetType targetType, boolean keep, Map<String, String> vars) {
        NObjectElement obj = configElements.asObject().get();
        String targetName = obj.getString("target-name").get();
        String targetId = obj.getString("target-id").get();
        if (NBlankable.isBlank(targetId)) {
            targetId = targetName;
        }
        vars.put("config.target", targetName);
        NObjectElement infoObj = apiElement.asObject().get().getObject("info").orElse(NElements.of().ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

        NPath newFile = parentPath.resolve(baseName + "-" + NoApiUtils.toValidFileName(targetId) + "-" + documentVersion + ".pdf");
        ConfigMarkdownGenerator mg = new ConfigMarkdownGenerator(session, msg);
        MdDocument md = mg.createMarkdown(obj, apiElement.asObject().get(), newFile.getParent(), sourceFolder, apiFileName, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, newFile, keep, targetType, session);
    }

}
