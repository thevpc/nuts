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
import java.util.stream.Collectors;

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
    private static class ResultingIds{
        Set<NId> classPathIds = new LinkedHashSet<>();
        Set<NId> nAppIds = new LinkedHashSet<>();
        NId nAppId;
        Set<NId> executableAppIds = new LinkedHashSet<>();

        private void add(NDescriptor desc) {
            NId resultId=desc.getId();
            if (resultId.getShortName().equals(NConstants.Ids.NUTS_API)) {
                NId appId = resolveNutsAppIdFromApiId(resultId);
                if(nAppId==null){
                    nAppId=appId;
                }
                nAppIds.add(appId);
                executableAppIds.add(appId);
                classPathIds.add(appId);
            }else if (resultId.getShortName().equals(NConstants.Ids.NUTS_APP)) {
                if(nAppId==null){
                    nAppId=resultId;
                }
                nAppIds.add(resultId);
                executableAppIds.add(resultId);
                classPathIds.add(resultId);
            } else {
                if (desc.isExecutable() || desc.isApplication()) {
                    executableAppIds.add(resultId);
                }
            }
            classPathIds.add(resultId);

        }
        private NId resolveNutsAppIdFromApiId(NId apiId) {
            NVersion v = apiId.getVersion();
            if (v.compareTo("0.8.5") < 0) {
                return apiId;
            }
            NId appId = NWorkspace.of().getAppId();
            return appId.builder().setVersion(apiId.getVersion()).build();
//        List<NId> found = NSearchCmd.of().addId(appId.getShortId())
//                .setLatest(true)
//                .setDistinct(true)
//                .setDescriptorFilter(NDescriptorFilters.of().byApiVersion(apiId.getVersion()))
//                .setDependencyFilter(NDependencyFilters.of()
//                        .byRunnable()
//                )
//                .setInlineDependencies(true)
//                .getResultIds().toList();
//        if (found.size() > 0) {
//            return found.get(0);
//        }
//        throw new NIllegalArgumentException(NMsg.ofC("unable to resolve app for %s", apiId));
        }
    }
    @Override
    public int execute() {
        List<NDescriptor> aa = searchDescriptors(NId.of("nuts-runtime"));
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
        nuts_bundle_info_config.target = "${user.dir}/lib";

        NPath rootFolder = null;
        NPath bundleFolder = null;
        boolean tempBundleFolder = false;
        String format = boptions.withFormat;
        if (format == null) {
            format = "jar";
        }
        boolean includeConfigFiles = true;

        ResultingIds resultingIds = new ResultingIds();

        NWorkspace ws = session.getWorkspace();
        NTrace.println(NMsg.ofC("computing dependencies for %s", boptions.ids));
        NChronometer chrono = NChronometer.startNow();
        for (String id : boptions.ids) {
            for (NDescriptor resultId : searchDescriptors(NId.of(id))) {
                resultingIds.add(resultId);
            }
        }
        if(resultingIds.nAppId==null) {
            for (NDescriptor resultId : searchDescriptors(ws.getAppId())) {
                resultingIds.add(resultId);
            }
        }
        cmdLine.throwUnexpectedArgument(NMsg.ofC("%s", boptions.ids));
        chrono.stop();
        NTrace.println(NMsg.ofC("found %s dependency and %s apps in %s...", resultingIds.classPathIds.size(), resultingIds.executableAppIds.size(), chrono.getDuration()));
        String defaultName = "nuts-bundle";
        String defaultVersion = "1.0";
        if (resultingIds.executableAppIds.size() == 1) {
            for (NId executableAppId : resultingIds.executableAppIds) {
                defaultName = executableAppId.getArtifactId() + "-bundle";
                defaultVersion = executableAppId.getVersion().toString();
                break;
            }
        } else if (resultingIds.executableAppIds.size() > 1) {
            for (NId executableAppId : resultingIds.executableAppIds) {
                defaultName = executableAppId.getArtifactId() + "-and-all-bundle";
                defaultVersion = executableAppId.getVersion().toString();
                break;
            }
        }
        nuts_bundle_info_config.appName = NStringUtils.firstNonBlank(boptions.withAppName, boptions.withAppTitle, defaultName);

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
                        NPath.ofUserDirectory().resolve(nuts_bundle_info_config.appName + "-bundle")
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
                        NPath.ofUserDirectory().resolve(nuts_bundle_info_config.appName + "-bundle")
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


        NFetchCmd f = NFetchCmd.of();

        //load all definitions
        Map<NId, NDefinition> allIds = new HashMap<>();
        Stack<NId> toProcess = new Stack<>();
        toProcess.addAll(resultingIds.classPathIds);
        while (!toProcess.isEmpty()) {
            NId id = toProcess.pop();
            if (!allIds.containsKey(id)) {
                allIds.put(id, null);
                NDefinition resultDefinition = f.setId(id)
                        .setContent(true)
                        .getResultDefinition();
                allIds.put(id, resultDefinition);
                for (NId parent : resultDefinition.getDescriptor().getParents()) {
                    toProcess.push(parent);
                }
            }
        }

        for (Map.Entry<NId, NDefinition> entry : allIds.entrySet()) {
            NId id = entry.getKey();
            NDefinition resultDefinition = entry.getValue();
            String fullPath = ExtraApiUtils.resolveJarPath(id);
            if (resultDefinition.getContent().isPresent()) {
                cp.from(resultDefinition.getContent().get())
                        .to(bundleFolder.resolve(fullPath))
                        .run();
                if (includeConfigFiles) {
                    nuts_bundle_files_config.install(
                            "/" + fullPath
                            , "$target/" + fullPath
                    );
                    NTrace.println(NMsg.ofC("adding to classpath %s", id));
                }
            }

            fullPath = ExtraApiUtils.resolveNutsDescriptorPath(id);
            cp.from(NDescriptorFormat.of().setValue(resultDefinition.getDescriptor()).setNtf(false).toString().getBytes())
                    .to(bundleFolder.resolve(fullPath))
                    .run();
            if (includeConfigFiles) {
                nuts_bundle_files_config.install("/" + fullPath
                        , "$target/" + fullPath
                );
            }
        }
        NTrace.println(NMsg.ofC("resolving workspace runner as %s", resultingIds.nAppId));
        bundleFolder.resolve(".nuts-repository").writeString("{}");
        if (includeConfigFiles) {
            NTrace.println(NMsg.ofC("building repository"));
            nuts_bundle_files_config.install("/.nuts-repository", "$target/.nuts-repository");
            for (NId executableAppId : resultingIds.executableAppIds) {
                NTrace.println(NMsg.ofC("building executable script for %s", executableAppId));
                createAppScripts(executableAppId, resultingIds.nAppId, bundleFolder, nuts_bundle_files_config);
            }
            rootFolder.resolve("META-INF/nuts-bundle-files.config").writeString(nuts_bundle_files_config.toString());
        }


        nuts_bundle_info_config.appVersion = NStringUtils.firstNonBlank(boptions.withAppVersion, defaultVersion, "1.0");
        nuts_bundle_info_config.appTitle = NStringUtils.firstNonBlank(boptions.withAppTitle, boptions.withAppName, defaultName);
        nuts_bundle_info_config.appDesc = NStringUtils.firstNonBlank(boptions.withAppDesc, boptions.withAppTitle);

        if (includeConfigFiles) {
            rootFolder.resolve("META-INF/nuts-bundle-info.config").writeString(nuts_bundle_info_config.toString());
        }

        NSession nSession = NSession.of();
        switch (format) {
            case "jar": {
                NCompress zip = NCompress.of().setPackaging("zip");
                NPath target = NPath.of(NStringUtils.firstNonBlank(boptions.withTarget,
                        nuts_bundle_info_config.appName + ".jar")).toAbsolute();
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
                        nuts_bundle_info_config.appName + ".zip")).toAbsolute();
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
    private List<NDescriptor> searchDescriptors(NId id){
        List<NDescriptor> list = NSearchCmd.of().addId(id)
                .setLatest(true)
                .setDistinct(true)
                .setEffective(true)
                .setDependencyFilter(NDependencyFilters.of().byScope(NDependencyScopePattern.RUN, NDependencyScopePattern.COMPILE))
                .setInlineDependencies(true)
                .getResultDescriptors().toList();
        if (list.isEmpty()) {
            throw new NNotFoundException(id);
        }
        return list;
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

    private void createAppScripts(NId mainIdStr, NId nutsId, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        if (mainIdStr == null) {
            return;
        }
        String appName = mainIdStr.getArtifactId();
        if (appName.equals("nuts-app")) {
            appName = "nuts";
        }
        createPosixScript(mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config);
        createWindowsScript(mainIdStr, nutsId, appName, bundleFolder, nuts_bundle_files_config);
    }

    private void createPosixScript(NId mainIdStr, NId nutsId, String appName, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        // create posix runner
        bundleFolder.resolve(appName + "-posix-runner.sh").writeString(
                new NStringBuilder()
                        .println("#!/bin/sh")
                        .println("")
                        .println("SCRIPT_PATH=\"${BASH_SOURCE[0]:-${(%):-%x}}\"")
                        .println("DIR=\"$(cd -- \"$(dirname -- \"$SCRIPT_PATH\")\" && pwd)\"")
                        .println("java -jar " +
                                "\"$DIR/lib/" + ExtraApiUtils.resolveJarPath(nutsId) + "\" " +
                                "--repo==$DIR/lib " +
                                "-w=$DIR/ws " +
                                "---m2=false " +
                                "--desktop-launcher=unsupported " +
                                "--menu-launcher=unsupported " +
                                "--user-launcher=unsupported " +
                                "'--!switch' " +
                                "'--!init-platforms' " +
                                "'--!init-scripts' " +
                                "'--!init-launchers' " +
                                "'--install-companions=false' " +
                                (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                                + " \"$@\"")
                        .build()
        );
        nuts_bundle_files_config.installPosixExecutable(
                "/" + appName + "-posix-runner.sh",
                "${user.dir}/" + appName + ".sh"
        );
    }

    private void createWindowsScript(NId mainIdStr, NId nutsId, String appName, NPath bundleFolder, NutsBundleFilesConfig nuts_bundle_files_config) {
        // create windows runner
        bundleFolder.resolve(appName + "-windows-runner.bat").writeString(
                new NStringBuilder()
                        .println("")
                        .println("SET FPATH=%~dp0")
                        .println("SET DIR=%FPATH:~0,-1%")
                        .println("java.exe -jar " +
                                "\"%DIR%\\lib\\" + ExtraApiUtils.resolveJarPath(nutsId).replace("/", "\\") + "\" " +
                                "--repo==%DIR%\\lib " +
                                "-w=%DIR%\\ws " +
                                "---m2=false " +
                                "--desktop-launcher=unsupported " +
                                "--menu-launcher=unsupported " +
                                "--user-launcher=unsupported " +
                                "--!switch " +
                                "--!init-platforms " +
                                "--!init-scripts " +
                                "--!init-launchers " +
                                "--install-companions=false " +
                                (NConstants.Ids.NUTS_APP.equals(mainIdStr.getShortName()) ? "" : ("\"" + mainIdStr + "\""))
                                + " $*")
                        .build()
        );
        nuts_bundle_files_config.installWindows("/" + appName + "-windows-runner.bat", "${user.dir}/" + appName + ".bat");
    }



}
