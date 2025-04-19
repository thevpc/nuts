/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell.NShellWriter;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.util.*;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultNBundleInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNBundleInternalExecutable(String[] args, NExecCmd execCommand) {
        super("bundle", args, execCommand);
    }

    static class BOptions {
        List<String> ids = new ArrayList<>();
        boolean withDependencies = true;
        boolean withOptional = false;
        String withAppVersion = null;
        String withAppName = null;
        String withAppTitle = null;
        String withAppDesc = null;
        String withTarget = null;
        String withFormat = null;
        boolean withClean = false;
        boolean embedded = false;
        boolean verbose = false;
        boolean reset = false;
        boolean yes = true;
    }

    private class ResultingIds {
        LinkedHashMap<NId, NDefinition> classPath = new LinkedHashMap<>();
        Set<NId> executableAppIds = new LinkedHashSet<>();
        Set<NId> baseIds = new LinkedHashSet<>();


        private NId findNutsApiId() {
            for (NId resultId : classPath.keySet()) {
                if (resultId.getShortName().equals(NConstants.Ids.NUTS_API)) {
                    return resultId;
                }
            }
            return null;
        }

        private NId findNutsAppId() {
            for (NId resultId : classPath.keySet()) {
                if (resultId.getShortName().equals(NConstants.Ids.NUTS_APP)) {
                    return resultId;
                }
            }
            return null;
        }

        private NId findNutsRuntimeId() {
            for (NId resultId : classPath.keySet()) {
                if (resultId.getShortName().equals(NConstants.Ids.NUTS_RUNTIME)) {
                    return resultId;
                }
            }
            return null;
        }


        public ResultingIds add(String id) {
            if (!NBlankable.isBlank(id)) {
                add(NId.of(id));
            }
            return this;
        }

        private ResultingIds addBomId(NId id) {
            if (!NBlankable.isBlank(id)) {
                if (classPath.containsKey(id.getLongId())) {
                    return this;
                }
                NDefinition imdef = NFetchCmd.of(id)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                        .getResultDefinition();
                if (!classPath.containsKey(imdef.getId().getLongId())) {
                    NId resultId = imdef.getId();
                    if (imdef.getDescriptor().isPlatformApplication() || imdef.getDescriptor().isNutsApplication()) {
                        if (isBaseId(resultId)) {
                            executableAppIds.add(resultId);
                        }
                    }
                    classPath.put(resultId.getLongId(), imdef);
                }
                for (NId parent : imdef.getDescriptor().getParents()) {
                    add(parent);
                }
                for (NDependency standardDependency : imdef.getEffectiveDescriptor().get().getStandardDependencies()) {
                    if (NDependencyScope.parse(standardDependency.getScope()).orElse(NDependencyScope.API) == NDependencyScope.IMPORT) {
                        addBomId(standardDependency.toId());
                    }
                }
            }
            return this;
        }

        public ResultingIds add(NId id) {
            if (!NBlankable.isBlank(id)) {
                if (classPath.containsKey(id.getLongId())) {
                    return this;
                }
                List<NDefinition> list = new ArrayList<>();
                NStream<NDefinition> resultDefinitions = NSearchCmd.of().addId(id)
                        .setLatest(true)
                        .setDistinct(true)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable(false, true))
                        .setInlineDependencies(true)
                        .setIgnoreCurrentEnvironment(true)
                        .getResultDefinitions();
                resultDefinitions.forEach(resultDefinition -> {
                    list.add(resultDefinition);
                    NTrace.println(NMsg.ofC(NI18n.of("loaded dependency %s for %s"),
                            resultDefinition.getId(),
                            id
                    ));
                });
                if (list.isEmpty()) {
                    throw new NNotFoundException(id);
                }
                for (NDefinition def : list) {
                    if (!classPath.containsKey(def.getId().getLongId())) {
                        NId resultId = def.getId();
                        if (def.getDescriptor().isPlatformApplication() || def.getDescriptor().isNutsApplication()) {
                            if (isBaseId(resultId)) {
                                executableAppIds.add(resultId);
                            }
                        }
                        classPath.put(resultId.getLongId(), def);
                    }
                    for (NId parent : def.getDescriptor().getParents()) {
                        add(parent);
                    }
                    for (NDependency standardDependency : def.getEffectiveDescriptor().get().getStandardDependencies()) {
                        if (NDependencyScope.parse(standardDependency.getScope()).orElse(NDependencyScope.API) == NDependencyScope.IMPORT) {
                            addBomId(standardDependency.toId());
                        }
                    }
                }
            }
            return this;
        }

        public boolean isBaseId(NId resultId) {
            for (NId baseId : baseIds) {
                if (baseId.getLongName().equals(resultId.getShortName())) {
                    return true;
                }
            }
            for (NId baseId : baseIds) {
                if (baseId.getShortName().equals(resultId.getShortName())) {
                    return true;
                }
            }
            for (NId baseId : baseIds) {
                if (NBlankable.isBlank(baseId.getGroupId()) && baseId.getArtifactId().equals(resultId.getArtifactId())) {
                    return true;
                }
            }
            return false;
        }

        public ResultingIds addAllId(String[] ids) {
            for (String id : ids) {
                if (!NBlankable.isBlank(id)) {
                    baseIds.add(NId.of(id));
                }
            }
            for (String id : ids) {
                add(id);
            }
            return this;
        }

        public void build() {
            NSession session = NSession.of();
            //ensure there is a full nuts workspace runtime (nuts-runtime)
            if (findNutsRuntimeId() == null) {
                for (NDefinition resultIdDef : new ArrayList<>(classPath.values())) {
                    if (resultIdDef.getId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                        if (resultIdDef.getId().getLongName().equals(session.getWorkspace().getAppId().getLongName())) {
                            add(session.getWorkspace().getRuntimeId());
                        } else {
                            add(session.getWorkspace().getRuntimeId().builder().setVersion(resultIdDef.getId().getVersion() + ".0").build());
                        }
                        break;
                    }
                }
            }
            if (findNutsRuntimeId() == null) {
                add(session.getWorkspace().getRuntimeId());
            }
            if (findNutsAppId() == null) {
                for (NDefinition resultIdDef : new ArrayList<>(classPath.values())) {
                    if (resultIdDef.getId().getShortName().equals(NConstants.Ids.NUTS_API)) {
                        if (resultIdDef.getId().getLongName().equals(session.getWorkspace().getAppId().getLongName())) {
                            add(session.getWorkspace().getAppId());
                        } else {
                            NVersion v = resultIdDef.getId().getVersion();
                            if (v.compareTo("0.8.5") < 0) {
                                //do nothing
                            } else {
                                NId appId = NWorkspace.of().getAppId();
                                add(appId.builder().setVersion(resultIdDef.getId().getVersion()).build());
                            }
                        }
                        break;
                    }
                }
            }
            if (findNutsRuntimeId() == null) {
                add(session.getWorkspace().getAppId());
            }
        }
    }

    private NOptional<String> ensureValidFileName(String any) {
        if (any != null) {
            StringBuilder sb = new StringBuilder();
            for (char c : any.toCharArray()) {
                if (
                        (c >= 'a' && c <= 'z')
                                || (c >= 'A' && c <= 'Z')
                                || (c >= '0' && c <= '9')
                                || (c == '_')
                                || (c == '-')
                                || (c == '+')
                                || (c == '.')
                ) {
                    sb.append(c);
                } else {
                    sb.append("_");
                }
            }
            if (sb.length() > 0) {
                return NOptional.of(sb.toString());
            }
        }
        return NOptional.ofNamedEmpty("file name");
    }

    @Override
    public int execute() {
        NChronometer allChrono = NChronometer.startNow();
        NSession session = NSession.of();
        if (session.isDry()) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NTrace.println(NMsg.ofC("nuts %s v%s",
                NMsg.ofStyledPrimary1("bundle builder"),
                NWorkspace.of().getRuntimeId().getVersion()));
        BOptions boptions = new BOptions();
        NCmdLine cmdLine = NCmdLine.of(args);
        parseBOptions(boptions, cmdLine, session);
        NutsBundleFilesConfig nuts_bundle_files_config = new NutsBundleFilesConfig();
        NutsBundleInfoConfig nuts_bundle_info_config = new NutsBundleInfoConfig();
        String repo = "${target}/.nuts-bundle/lib";
        NPath rootFolder = null;
        NPath bundleFolder = null;
        boolean tempBundleFolder = false;
        String format = boptions.withFormat;
        if (format == null) {
            format = "jar";
        }
        boolean includeConfigFiles = true;

        ResultingIds resultingIds = new ResultingIds();

        NTrace.println(NMsg.ofC(NI18n.of("computing dependencies for %s"), boptions.ids));
        NChronometer chrono = NChronometer.startNow();
        resultingIds
                .addAllId(boptions.ids.toArray(new String[0]))
                .build();


        cmdLine.throwUnexpectedArgument(NMsg.ofC("%s", boptions.ids));
        chrono.stop();
        NTrace.println(NMsg.ofC(NI18n.of("found %s deps and %s apps in %s..."), resultingIds.classPath.size(), resultingIds.executableAppIds.size(), chrono.getDuration()));
        String defaultName = "nuts-bundle";
        String defaultVersion = "1.0";
        if (resultingIds.executableAppIds.size() == 1) {
            for (NId executableAppId : resultingIds.executableAppIds) {
                defaultName = preferredAppName(executableAppId);
                defaultVersion = executableAppId.getVersion().toString();
                break;
            }
        } else if (resultingIds.executableAppIds.size() > 1) {
            NId[] explicitIds = resultingIds.executableAppIds.stream().filter(x -> {
                for (String id : boptions.ids) {
                    if (Objects.equals(NId.of(id).getShortName(), x.getShortName())) {
                        return true;
                    }
                }
                for (String id : boptions.ids) {
                    if (Objects.equals(id, x.getArtifactId())) {
                        return true;
                    }
                }
                return false;
            }).toArray(NId[]::new);
            if (explicitIds.length == 1) {
                defaultName = preferredAppName(explicitIds[0]);
                defaultVersion = explicitIds[0].getVersion().toString();
            } else {
                for (NId executableAppId : resultingIds.executableAppIds) {
                    defaultName = preferredAppName(executableAppId) + "-and-all";
                    defaultVersion = executableAppId.getVersion().toString();
                    break;
                }
            }
        }
        nuts_bundle_info_config.appName = NStringUtils.firstNonBlank(boptions.withAppName, boptions.withAppTitle, defaultName);
        nuts_bundle_info_config.appVersion = NStringUtils.firstNonBlank(boptions.withAppVersion, defaultVersion, "1.0");
        nuts_bundle_info_config.appTitle = NStringUtils.firstNonBlank(boptions.withAppTitle, nuts_bundle_info_config.appName);
        nuts_bundle_info_config.appDesc = NStringUtils.firstNonBlank(boptions.withAppDesc, nuts_bundle_info_config.appTitle);
        String fullAppFileName = ensureValidFileName(nuts_bundle_info_config.appName).orElse("app") + "-" + ensureValidFileName(nuts_bundle_info_config.appVersion).orElse("1.0");
        nuts_bundle_info_config.target = "${user.dir}/" + fullAppFileName;

        switch (format) {
            case "jar":
            case "zip": {
                rootFolder = NPath.ofTempFolder("bundle");
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
                break;
            }
            case "exploded": {
                rootFolder = NBlankable.isBlank(boptions.withTarget) ?
                        NPath.ofUserDirectory().resolve(fullAppFileName + "-bundle")
                        : NPath.of(boptions.withTarget)
                ;
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
                if (boptions.withClean) {
                    if (bundleFolder.isDirectory()) {
                        for (NPath nPath : bundleFolder.list()) {
                            nPath.deleteTree();
                        }
                    }
                    for (String s : new String[]{
                            "META-INF/nuts-bundle-info.config",
                            "META-INF/nuts-bundle-files.config"
                    }) {
                        NPath p = rootFolder.resolve(s);
                        if (p.isRegularFile()) {
                            p.delete();
                        }
                    }
                }
                break;
            }
            case "dir": {
                rootFolder = NBlankable.isBlank(boptions.withTarget) ?
                        NPath.ofUserDirectory().resolve(fullAppFileName + "-bundle")
                        : NPath.of(boptions.withTarget)
                ;
                bundleFolder = rootFolder;
                if (boptions.withClean) {
                    if (rootFolder.isDirectory()) {
                        for (NPath nPath : rootFolder.list()) {
                            nPath.deleteTree();
                        }
                    }
                }
                includeConfigFiles = false;
                break;
            }
            default: {
                cmdLine.throwUnexpectedArgument(NMsg.ofC("invalid format %s", format));
            }
        }
        bundleFolder.mkdirs();

        NCp cp = NCp.of();
        if ("jar".equals(format)) {
            cp
                    .from(getClass().getResource("/META-INF/bundle/NutsBundleRunner.class.template"))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("net/thevpc/nuts/runtime/standalone/installer/NutsBundleRunner.class"))
                    .run();
            cp
                    .from(getClass().getResource("/META-INF/bundle/MANIFEST-COPY.MF"))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("META-INF/MANIFEST.MF"))
                    .run();
        }

        for (NDefinition d : resultingIds.classPath.values()) {
            NId id = d.getId();
            String fullPath = id.getMavenPath("jar");
            if (d.getContent().isPresent()) {
                cp.from(d.getContent().get())
                        .to(bundleFolder.resolve(fullPath))
                        .run();
                if (includeConfigFiles) {
                    nuts_bundle_files_config.install(
                            "/" + fullPath
                            , repo + "/" + fullPath
                    );
                    NTrace.println(NMsg.ofC(NI18n.of("adding to classpath %s"), id));
                }
            }

            fullPath = id
                    //descriptor is not classifier aware
                    .builder().setClassifier(null).build()
                    .getMavenPath(NConstants.Files.DESCRIPTOR_FILE_EXTENSION_SIMPLE);
            cp.from(NDescriptorFormat.of().setValue(d.getDescriptor()).setNtf(false).toString().getBytes())
                    .to(bundleFolder.resolve(fullPath))
                    .run();
            if (includeConfigFiles) {
                nuts_bundle_files_config.install("/" + fullPath
                        , repo + "/" + fullPath
                );
            }
        }
        NTrace.println(NMsg.ofC(NI18n.of("resolving workspace runner as %s"), resultingIds.findNutsAppId()));
        bundleFolder.resolve(".nuts-repository").writeString("{}");
        if (includeConfigFiles) {
            NTrace.println(NMsg.ofC(NI18n.of("building repository")));
            nuts_bundle_files_config.install("/.nuts-repository", repo + "/.nuts-repository");
            for (NId executableAppId : resultingIds.executableAppIds) {
                NTrace.println(NMsg.ofC(NI18n.of("building executable script for %s"), executableAppId));
                createAppScripts(executableAppId, resultingIds.findNutsAppId(), bundleFolder, nuts_bundle_files_config, boptions);
            }
            rootFolder.resolve("META-INF/nuts-bundle-files.config").writeString(nuts_bundle_files_config.toString());
        }


        if (includeConfigFiles) {
            rootFolder.resolve("META-INF/nuts-bundle-info.config").writeString(nuts_bundle_info_config.toString());
        }

        NSession nSession = NSession.of();
        switch (format) {
            case "jar": {
                NCompress zip = NCompress.of().setPackaging("zip");
                NPath target = NPath.of(NStringUtils.firstNonBlank(boptions.withTarget,
                        fullAppFileName
                                + "-bundle"
                                + ".jar")).toAbsolute();
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(
                                target
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            case "zip": {
                NCompress zip = NCompress.of().setPackaging("zip");
                NPath target = NPath.of(NStringUtils.firstNonBlank(boptions.withTarget,
                        fullAppFileName
                                + "-bundle"
                                + ".zip")).toAbsolute();
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(target
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            case "dir":
            case "exploded": {
                NPath target = rootFolder.toAbsolute();
                if (nSession.isTrace()) {
                    if (nSession.isPlainOut()) {
                        NTrace.out().println(NMsg.ofC("bundle created %s in %s", target, allChrono.stop().getDuration()));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            default: {
                cmdLine.throwError(NMsg.ofC("invalid format %s", format));
            }
        }
        return NExecutionException.SUCCESS;
    }


    private void parseBOptions(BOptions boptions, NCmdLine cmdLine, NSession session) {
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                switch (a.key()) {
                    case "--optional": {
                        cmdLine.withNextFlag((v, ar) -> boptions.withOptional = (v));
                        break;
                    }
                    case "--deps":
                    case "--dependencies": {
                        cmdLine.withNextFlag((v, ar) -> boptions.withDependencies = (v));
                        break;
                    }
                    case "--app-version": {
                        cmdLine.withNextEntry((v, ar) -> boptions.withAppVersion = (v));
                        break;
                    }
                    case "--app-name":
                    case "--name": {
                        cmdLine.withNextEntry((v, ar) -> boptions.withAppName = (v));
                        break;
                    }
                    case "--app-desc":
                    case "--desc": {
                        cmdLine.withNextEntry((v, ar) -> boptions.withAppDesc = (v));
                        break;
                    }
                    case "--app-title":
                    case "--title": {
                        cmdLine.withNextEntry((v, ar) -> boptions.withAppTitle = (v));
                        break;
                    }
                    case "--target": {
                        cmdLine.withNextEntry((v, ar) -> boptions.withTarget = (v));
                        break;
                    }
                    case "--dir":
                    case "--as-dir": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                boptions.withFormat = ("dir");
                            }
                        });
                        break;
                    }
                    case "--exploded":
                    case "--as-exploded": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                boptions.withFormat = ("exploded");
                            }
                        });
                        break;
                    }
                    case "--jar":
                    case "--as-jar": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                boptions.withFormat = ("jar");
                            }
                        });
                        break;
                    }
                    case "--as-zip":
                    case "--zip": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                boptions.withFormat = ("zip");
                            }
                        });
                        break;
                    }
                    case "--embedded": {
                        cmdLine.withNextFlag((v, ar) -> boptions.embedded = v);
                        break;
                    }
                    case "--verbose": {
                        cmdLine.withNextFlag((v, ar) -> boptions.verbose = v);
                        break;
                    }
                    case "-y":
                    case "--yes": {
                        cmdLine.withNextFlag((v, ar) -> boptions.yes = v);
                        break;
                    }
                    case "-Z":
                    case "--reset": {
                        cmdLine.withNextFlag((v, ar) -> boptions.reset = v);
                        break;
                    }
                    case "--clean": {
                        cmdLine.withNextFlag((v, ar) -> boptions.withClean = (v));
                        break;
                    }
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                boptions.ids.add(cmdLine.next().get().toString());
            }
        }
    }

    private String preferredAppName(NId mainIdStr) {
        if (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName())) {
            return "nuts";
        }
        return mainIdStr.getArtifactId();
    }

    private void createAppScripts(NId mainIdStr, NId nutsId, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config, BOptions options) {
        if (mainIdStr == null) {
            return;
        }
        String appName = preferredAppName(mainIdStr);
        createShellScript(NOsFamily.LINUX, NShellFamily.BASH, mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config, options);
        createShellScript(NOsFamily.MACOS, NShellFamily.ZSH, mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config, options);
        createShellScript(NOsFamily.WINDOWS, NShellFamily.WIN_CMD, mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config, options);
        createShellScript(NOsFamily.UNIX, NShellFamily.BASH, mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config, options);
    }


    private void createShellScript(NOsFamily osFamily, NShellFamily shellFamily, NId mainIdStr, NId nutsId, String appName, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config, BOptions options) {
        NShellWriter out = NShellWriter.of(shellFamily).get();
        String dotExe = osFamily == NOsFamily.WINDOWS ? ".exe" : "";
        String dotBatOrSh = osFamily == NOsFamily.WINDOWS ? ".bat" : ".sh";
        String dotBatOrNothing = osFamily == NOsFamily.WINDOWS ? ".bat" : "";
        out
                .printlnComment("-------------------------------------")
                .printlnComment(" Nuts Bundle Launcher Script " + NWorkspace.of().getRuntimeId().getVersion())
                .printlnComment(" This bundle was created for " + mainIdStr.getShortName())
                .printlnComment(" (c) 2025 thevpc")
                .printlnComment("-------------------------------------")
                .println()
                .echoOff()
                .printlnComment("resolve current script path")
                .printlnSetVarScriptPath("NS_SCRIPT_PATH")
                .printlnSetVarFolderPath("NS_SCRIPT_DIR", "NS_SCRIPT_PATH")
                .printlnSetVar("NS_WS_JAR", "$NS_SCRIPT_DIR/.nuts-bundle/lib/" + nutsId.getMavenPath("jar"))
                .printlnSetVar("NS_JAVA_OPTIONS", "")
                .println()
                .printlnComment("resolve workspace options")
                .printlnSetVar("NS_WS_OPTIONS=", "--repo==$NS_SCRIPT_DIR/.nuts-bundle/lib -w=$NS_SCRIPT_DIR/.nuts-bundle/ws")
                .printlnComment("add workspace isolation options")
                .printlnSetVar("NS_WS_OPTIONS", "$NS_WS_OPTIONS ---m2=false --desktop-launcher=unsupported --menu-launcher=unsupported --user-launcher=unsupported --!switch --!init-platforms --!init-scripts --!init-launchers --!install-companions")
                .printlnComment("add other options like --verbose")
                .printlnComment("--verbose : for more logging")
                .printlnComment("-Zy      : to reset the whole workspace")
                .setCommentsMode(options.verbose).printlnSetAppendVar("NS_WS_OPTIONS", " --verbose")
                .setCommentsMode(options.yes).printlnSetAppendVar("NS_WS_OPTIONS", " --yes")
                .setCommentsMode(options.reset).printlnSetAppendVar("NS_WS_OPTIONS", " --reset")
                .setCommentsMode(options.embedded).printlnSetAppendVar("NS_WS_OPTIONS", " -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
                .setCommentsMode(false)
                .printlnComment("")
                .printlnComment("add other JVM options like for debug mode")
                .setCommentsMode(true).printlnSetAppendVar("NS_JAVA_OPTIONS", " --embedded")
                .setCommentsMode(false)
                .println()
                .printlnCommand("java" + dotExe + " $NS_JAVA_OPTIONS -jar \"$NS_WS_JAR\" $NS_WS_OPTIONS " +
                        (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                        + " ${*}")
        ;

        switch (osFamily) {
            case WINDOWS: {
                bundleFolder.resolve(appName + "-windows-runner" + dotBatOrSh).writeString(out.build());
                nuts_bundle_files_config.installWindows("/" + appName + "-windows-runner" + dotBatOrSh, "${target}/" + appName + dotBatOrNothing);
                break;
            }
            case LINUX: {
                bundleFolder.resolve(appName + "-linux-runner" + dotBatOrSh).writeString(out.build());
                nuts_bundle_files_config.installPosix("/" + appName + "-linux-runner" + dotBatOrSh, "${target}/" + appName + dotBatOrNothing);
                break;
            }
            case MACOS: {
                bundleFolder.resolve(appName + "-macos-runner" + dotBatOrSh).writeString(out.build());
                nuts_bundle_files_config.installPosix("/" + appName + "-macos-runner" + dotBatOrSh, "${target}/" + appName + dotBatOrNothing);
                break;
            }
            case UNIX: {
                bundleFolder.resolve(appName + "-unix-runner" + dotBatOrSh).writeString(out.build());
                nuts_bundle_files_config.installPosix("/" + appName + "-unix-runner" + dotBatOrSh, "${target}/" + appName + dotBatOrNothing);
                break;
            }
        }
    }


}
