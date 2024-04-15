package net.thevpc.nuts.toolbox.noapi.service;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
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

    public void run(String source, String target, String varsPath, Map<String, String> varsMap, boolean keep) {
        Map<String, String> vars = new HashMap<>();
        if (!NBlankable.isBlank(varsPath)) {
            Map<Object, Object> m = NElements.of(session).parse(NPath.of(varsPath, session), Map.class);
            for (Map.Entry<Object, Object> o : m.entrySet()) {
                vars.put(String.valueOf(o.getKey()), String.valueOf(o.getValue()));
            }
        }
        if (varsMap != null) {
            vars.putAll(varsMap);
        }
        NPath sourcePath = NPath.of(source, session).normalize().toAbsolute();
        if (!sourcePath.exists()) {
            throw new NNoSuchElementException(session, NMsg.ofC("file not found %s", sourcePath));
        }
        if (session.isPlainTrace()) {
            session.out().println(NMsg.ofC("read open-api file %s", sourcePath));
        }
        String sourceBaseName = sourcePath.getSmartBaseName();
        NElement apiElement = NoApiUtils.loadElement(sourcePath, session);
        NObjectElement infoObj = apiElement.asObject().get(session).getObject("info").orElse(NElements.of(session).ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

//        Path path = Paths.get("/data/from-git/RapiPdf/docs/specs/maghrebia-api-1.1.2.yml");
        SupportedTargetType targetType = NoApiUtils.resolveTarget(target, SupportedTargetType.PDF);
        NPath sourceFolder = sourcePath.getParent();
        NPath parentPath = sourceFolder.resolve("dist-version-" + documentVersion);
        NPath targetPathObj = NoApiUtils.addExtension(sourcePath, parentPath, NPath.of(target, session), targetType, documentVersion, session);

        //start copying json file
        NPath openApiFileCopy = targetPathObj.resolveSibling(targetPathObj.getSmartBaseName() + "." + sourcePath.getLastExtension());
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
            NElement z = NElements.of(session).parse(cf);
            //remove version, will be added later
            NPath configFileCopy = targetPathObj.resolveSibling(cf.getSmartBaseName() +"-"+documentVersion+ "." + cf.getSmartExtension());
            cf.copyTo(configFileCopy);
            if (session.isPlainTrace()) {
                session.out().println(NMsg.ofC("copy  config  file %s", configFileCopy));
            }
            NPath targetPathObj2 = NoApiUtils.addExtension(sourcePath, parentPath, NPath.of(target, session), targetType, "", session);
            generateConfigDocument(z, apiElement, parentPath, sourceFolder, targetPathObj2.getSmartBaseName(),targetPathObj.getName(), targetType, keep, vars);
        }

        MainMarkdownGenerator mg = new MainMarkdownGenerator(session, msg);
        MdDocument md = mg.createMarkdown(apiElement, sourceFolder, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, targetPathObj, keep, targetType, session);
    }

    private void generateConfigDocument(NElement configElements, NElement apiElement, NPath parentPath, NPath sourceFolder, String baseName, String apiFileName, SupportedTargetType targetType, boolean keep, Map<String, String> vars) {
        NObjectElement obj = configElements.asObject().get(session);
        String targetName = obj.getString("target-name").get(session);
        String targetId = obj.getString("target-id").get(session);
        if(NBlankable.isBlank(targetId)){
            targetId=targetName;
        }
        vars.put("config.target", targetName);
        NObjectElement infoObj = apiElement.asObject().get(session).getObject("info").orElse(NElements.of(session).ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

        NPath newFile = parentPath.resolve(baseName + "-" + NoApiUtils.toValidFileName(targetId)+"-"+documentVersion + ".pdf");
        ConfigMarkdownGenerator mg = new ConfigMarkdownGenerator(session, msg);
        MdDocument md = mg.createMarkdown(obj, apiElement.asObject().get(session), newFile.getParent(), sourceFolder, apiFileName, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, newFile, keep, targetType, session);
    }

}
