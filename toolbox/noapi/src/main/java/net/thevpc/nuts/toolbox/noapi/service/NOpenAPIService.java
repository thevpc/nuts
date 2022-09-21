package net.thevpc.nuts.toolbox.noapi.service;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.toolbox.noapi.service.docs.ConfigMarkdownGenerator;
import net.thevpc.nuts.toolbox.noapi.service.docs.MainMakdownGenerator;
import net.thevpc.nuts.toolbox.noapi.model.SupportedTargetType;
import net.thevpc.nuts.toolbox.noapi.util.AppMessages;
import net.thevpc.nuts.toolbox.noapi.util.NoApiUtils;

import java.util.*;

public class NOpenAPIService {

    private NutsApplicationContext appContext;
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

    public NOpenAPIService(NutsApplicationContext appContext) {
        this.appContext = appContext;
        msg = new AppMessages(null, getClass().getResource("/net/thevpc/nuts/toolbox/noapi/messages-en.json"), appContext.getSession());
    }

    public void run(String source, String target, String varsPath, Map<String, String> varsMap, boolean keep) {
        Map<String, String> vars = new HashMap<>();
        NutsSession session = appContext.getSession();
        if (!NutsBlankable.isBlank(varsPath)) {
            Map<Object, Object> m = NutsElements.of(session).parse(NutsPath.of(varsPath, session), Map.class);
            for (Map.Entry<Object, Object> o : m.entrySet()) {
                vars.put(String.valueOf(o.getKey()), String.valueOf(o.getValue()));
            }
        }
        if (varsMap != null) {
            vars.putAll(varsMap);
        }
        NutsPath sourcePath = NutsPath.of(source, session).normalize().toAbsolute();
        if (!sourcePath.exists()) {
            throw new NutsNoSuchElementException(session, NutsMessage.ofCstyle("file not found %s", sourcePath));
        }
        if (session.isPlainTrace()) {
            session.out().printf("read open-api file %s\n", sourcePath);
        }
        String sourceBaseName = sourcePath.getSmartBaseName();
        NutsElement apiElement = NoApiUtils.loadElement(sourcePath, appContext.getSession());
        NutsObjectElement infoObj = apiElement.asObject().get(session).getObject("info").orElse(NutsElements.of(session).ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

//        Path path = Paths.get("/data/from-git/RapiPdf/docs/specs/maghrebia-api-1.1.2.yml");
        SupportedTargetType targetType = NoApiUtils.resolveTarget(target, SupportedTargetType.PDF);
        NutsPath sourceFolder = sourcePath.getParent();
        NutsPath parentPath = sourceFolder.resolve("dist-version-" + documentVersion);
        NutsPath targetPathObj = NoApiUtils.addExtension(sourcePath, parentPath, NutsPath.of(target, session), targetType, documentVersion, session);

        //start copying json file
        NutsPath openApiFileCopy = targetPathObj.resolveSibling(targetPathObj.getSmartBaseName() + "." + sourcePath.getLastExtension());
        sourcePath.copyTo(openApiFileCopy);
        if (session.isPlainTrace()) {
            session.out().printf("copy open-api file %s\n", openApiFileCopy);
        }

        NutsPath targetParent = targetPathObj.getParent();

        List<NutsPath> allConfigFiles = sourceFolder.list().filter(
                x ->
                {
                    return x.getName().endsWith(".config.json")
                            && (
                            x.getName().startsWith(sourceBaseName + ".")
                                    || x.getName().startsWith(sourceBaseName + "-")
                                    || x.getName().startsWith(sourceBaseName + "_")
                    );
                }
                , s -> NutsElements.of(s).toElement("config files")).toList();
        for (NutsPath cf : allConfigFiles) {
            NutsElement z = NutsElements.of(session).parse(cf);
            //remove version, will be added later
            NutsPath configFileCopy = targetPathObj.resolveSibling(cf.getSmartBaseName() +"-"+documentVersion+ "." + cf.getSmartExtension());
            cf.copyTo(configFileCopy);
            if (session.isPlainTrace()) {
                session.out().printf("copy  config  file %s\n", configFileCopy);
            }
            NutsPath targetPathObj2 = NoApiUtils.addExtension(sourcePath, parentPath, NutsPath.of(target, session), targetType, "", session);
            generateConfigDocument(z, apiElement, parentPath, sourceFolder, targetPathObj2.getSmartBaseName(),targetPathObj.getName(), targetType, keep, vars);
        }

        MainMakdownGenerator mg = new MainMakdownGenerator(appContext, msg);
        MdDocument md = mg.createMarkdown(apiElement, sourceFolder, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, targetPathObj, keep, targetType, session);
    }

    private void generateConfigDocument(NutsElement configElements, NutsElement apiElement, NutsPath parentPath, NutsPath sourceFolder, String baseName, String apiFileName, SupportedTargetType targetType, boolean keep, Map<String, String> vars) {
        NutsSession session = appContext.getSession();
        NutsObjectElement obj = configElements.asObject().get(session);
        String targetName = obj.getString("target-name").get(session);
        String targetId = obj.getString("target-id").get(session);
        if(NutsBlankable.isBlank(targetId)){
            targetId=targetName;
        }
        vars.put("config.target", targetName);
        NutsObjectElement infoObj = apiElement.asObject().get(session).getObject("info").orElse(NutsElements.of(session).ofEmptyObject());
        String documentVersion = infoObj.getString("version").orNull();

        NutsPath newFile = parentPath.resolve(baseName + "-" + NoApiUtils.toValidFileName(targetId)+"-"+documentVersion + ".pdf");
        ConfigMarkdownGenerator mg = new ConfigMarkdownGenerator(appContext, msg);
        MdDocument md = mg.createMarkdown(obj, apiElement.asObject().get(session), newFile.getParent(), sourceFolder, apiFileName, vars, defaultAdocHeaders);
        NoApiUtils.writeAdoc(md, newFile, keep, targetType, session);
    }

}
