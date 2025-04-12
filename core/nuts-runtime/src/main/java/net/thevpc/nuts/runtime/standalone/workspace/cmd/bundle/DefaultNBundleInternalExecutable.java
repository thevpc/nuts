/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
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

        public ResultingIds add(NId id) {
            if (!NBlankable.isBlank(id)) {
                if (classPath.containsKey(id.getLongId())) {
                    return this;
                }
                List<NDefinition> list = new ArrayList<>();
//                List<NDefinition> resultDefinitions0 = NSearchCmd.of().addId(id)
//                        .setLatest(true)
////                        .setDistinct(false)
//                        .setEffective(true)
//                        .setDependencyFilter(
//                                NDependencyFilters.of().byScope(NDependencyScopePattern.RUN, NDependencyScopePattern.COMPILE)
//                                        .and(NDependencyFilters.of().byRegularType())
//                        )
//                        .setInlineDependencies(true)
//                        .setIgnoreCurrentEnvironment(true)
//                        .setContent(true)
//                        .getResultDefinitions().toList();


                NStream<NDefinition> resultDefinitions = NSearchCmd.of().addId(id)
                        .setLatest(true)
                        .setDistinct(true)
                        .setEffective(true)
                        .setDependencyFilter(
                                NDependencyFilters.of().byScope(NDependencyScopePattern.RUN, NDependencyScopePattern.COMPILE)
                                .and(NDependencyFilters.of().byRegularType())
                        )
                        .setInlineDependencies(true)
                        .setIgnoreCurrentEnvironment(true)
                        .setContent(true)
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
                createAppScripts(executableAppId, resultingIds.findNutsAppId(), bundleFolder, nuts_bundle_files_config);
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

    private void createAppScripts(NId mainIdStr, NId nutsId, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        if (mainIdStr == null) {
            return;
        }
        String appName = preferredAppName(mainIdStr);
//        if(!mainIdStr.getVersion().isBlank()) {
//            appName = appName + "-" + mainIdStr.getVersion();
//        }
        createPosixScript(mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config);
        createWindowsScript(mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config);
    }

    private void createPosixScript(NId mainIdStr, NId nutsId, String appName, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        // create posix runner
        bundleFolder.resolve(appName + "-posix-runner.sh").writeString(
                new NStringBuilder()
                        .println("#!/bin/sh")
                        .println("#-------------------------------------")
                        .println("# Nuts Bundle Launcher Script " + NWorkspace.of().getRuntimeId().getVersion())
                        .println("# This bundle was created for " + mainIdStr.getShortName())
                        .println("# (c) 2025 thevpc")
                        .println("#-------------------------------------")
                        .println("")
                        .println("# resolve current script path")
                        .println("NS_SCRIPT_PATH=\"${BASH_SOURCE[0]:-${(%):-%x}}\"")
                        .println("NS_SCRIPT_DIR=\"$(cd -- \"$(dirname -- \"$N_SCRIPT_PATH\")\" && pwd)\"")
                        .println("NS_WS_JAR=\"$NS_SCRIPT_DIR/.nuts-bundle/lib/" + nutsId.getMavenPath("jar") + "\"")
                        .println("NS_JAVA_OPTIONS=\"\"")
                        .println("")
                        .println("# resolve workspace options")
                        .println("NS_WS_OPTIONS=\"--repo==$NS_SCRIPT_DIR/.nuts-bundle/lib -w=$NS_SCRIPT_DIR/.nuts-bundle/ws\"")
                        .println("# add workspace isolation options")
                        .println("NS_WS_OPTIONS=\"$NS_WS_OPTIONS ---m2=false --desktop-launcher=unsupported --menu-launcher=unsupported --user-launcher=unsupported --!switch --!init-platforms --!init-scripts --!init-launchers --!install-companions\"")
                        .println("# add other options like ...")
                        .println("#  --verbose : for more logging")
                        .println("#  -Zy       : to reset the whole workspace")
                        .println("# NS_WS_OPTIONS=\"$NS_WS_OPTIONS --verbose\"")
                        .println("# NS_WS_OPTIONS=\"$NS_WS_OPTIONS --embedded\"")
                        .println("#")
                        .println("# add other JVM options like for debug mode")
                        .println("# NS_JAVA_OPTIONS=\"$NS_JAVA_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005\"")
                        .println("")

                        .println("java $NS_JAVA_OPTIONS -jar \"$NS_WS_JAR\" $NS_WS_OPTIONS " +
                                (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                                + " \"$@\"")
                        .build()
        );
        nuts_bundle_files_config.installPosixExecutable(
                "/" + appName + "-posix-runner.sh",
                "${target}/" + appName /*+ ".sh"*/ // no need for bat extension
        );
    }

    private void createWindowsScript(NId mainIdStr, NId nutsId, String appName, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        // create windows runner
        bundleFolder.resolve(appName + "-windows-runner.bat").writeString(
                new NStringBuilder()
                        .println("::-------------------------------------")
                        .println(":: Nuts Bundle Launcher Script " + NWorkspace.of().getRuntimeId().getVersion())
                        .println(":: This bundle was created for " + mainIdStr.getShortName())
                        .println(":: (c) 2025 thevpc")
                        .println("::-------------------------------------")
                        .println("")
                        .println("@echo off")
                        .println(":: resolve current script path")
                        .println("SET NS_SCRIPT_PATH=%~dp0")
                        .println("SET NS_SCRIPT_DIR=%NS_SCRIPT_PATH:~0,-1%")
                        .println("SET NS_WS_JAR=%NS_SCRIPT_DIR%\\.nuts-bundle\\lib\\" + nutsId.getMavenPath("jar").replace("/", "\\"))
                        .println("SET NS_JAVA_OPTIONS=")
                        .println("")
                        .println(":: resolve workspace options")
                        .println("SET NS_WS_OPTIONS=--repo==%NS_SCRIPT_DIR%\\.nuts-bundle\\lib -w=%NS_SCRIPT_DIR%\\.nuts-bundle\\ws")
                        .println(":: add workspace isolation options")
                        .println("SET NS_WS_OPTIONS=%NS_WS_OPTIONS% ---m2=false --desktop-launcher=unsupported --menu-launcher=unsupported --user-launcher=unsupported --!switch --!init-platforms --!init-scripts --!init-launchers --!install-companions")
                        .println(":: add other options like --verbose")
                        .println("::  --verbose : for more logging")
                        .println("::  -Zy       : to reset the whole workspace")
                        .println("REM SET NS_WS_OPTIONS=%NS_WS_OPTIONS% --verbose")
                        .println("REM SET NS_WS_OPTIONS=%NS_WS_OPTIONS% --embedded")
                        .println("::")
                        .println(":: add other JVM options like for debug mode")
                        .println("REM SET NS_JAVA_OPTIONS=%NS_JAVA_OPTIONS% -agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005")
                        .println("")
                        .println("java.exe %NS_JAVA_OPTIONS% -jar \"%NS_WS_JAR%\" %NS_WS_OPTIONS% " +
                                (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                                + " %*")
                        .build()
        );
        nuts_bundle_files_config.installWindows("/" + appName + "-windows-runner.bat", "${target}/" + appName + ".bat");
    }


}
