/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NRef;
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

    @Override
    public int execute() {
        NSession session = NSession.of();
        if (session.isDry()) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NCmdLine cmdLine = NCmdLine.of(args);
        List<String> ids = new ArrayList<>();
        NRef<Boolean> withDependencies = NRef.of(true);
        NRef<Boolean> withOptional = NRef.of(false);
        NRef<String> withAppVersion = NRef.of(null);
        NRef<String> withAppName = NRef.of(null);
        NRef<String> withAppTitle = NRef.of(null);
        NRef<String> withAppDesc = NRef.of(null);
        NRef<String> withTarget = NRef.of(null);
        NRef<String> withFormat = NRef.of(null);
        NRef<Boolean> withClean = NRef.of(false);
        while (cmdLine.hasNext()) {
            NArg a = cmdLine.peek().get();
            if (a.isOption()) {
                switch (a.key()) {
                    case "--optional": {
                        cmdLine.withNextFlag((v, ar) -> withOptional.set(v));
                        break;
                    }
                    case "--deps":
                    case "--dependencies": {
                        cmdLine.withNextFlag((v, ar) -> withDependencies.set(v));
                        break;
                    }
                    case "--app-version": {
                        cmdLine.withNextEntry((v, ar) -> withAppVersion.set(v));
                        break;
                    }
                    case "--app-name":
                    case "--name": {
                        cmdLine.withNextEntry((v, ar) -> withAppName.set(v));
                        break;
                    }
                    case "--app-desc":
                    case "--desc": {
                        cmdLine.withNextEntry((v, ar) -> withAppDesc.set(v));
                        break;
                    }
                    case "--app-title":
                    case "--title": {
                        cmdLine.withNextEntry((v, ar) -> withAppTitle.set(v));
                        break;
                    }
                    case "--target": {
                        cmdLine.withNextEntry((v, ar) -> withTarget.set(v));
                        break;
                    }
                    case "--dir":
                    case "--as-dir": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("dir" );
                            }
                        });
                        break;
                    }
                    case "--exploded":
                    case "--as-exploded": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("exploded" );
                            }
                        });
                        break;
                    }
                    case "--jar":
                    case "--as-jar": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("jar" );
                            }
                        });
                        break;
                    }
                    case "--as-zip":
                    case "--zip": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("zip" );
                            }
                        });
                        break;
                    }
                    case "--clean": {
                        cmdLine.withNextFlag((v, ar) -> withClean.set(v));
                        break;
                    }
                    default: {
                        session.configureLast(cmdLine);
                    }
                }
            } else {
                ids.add(cmdLine.next().get().toString());
            }
        }
        NPath rootFolder = null;
        NPath bundleFolder = null;
        boolean tempBundleFolder = false;
        String format = withFormat.get();
        if (format == null) {
            format = "jar";
        }
        boolean includeConfigFiles = true;


        Set<NId> nIds = new LinkedHashSet<>();
        Set<NId> toBaseDir = new LinkedHashSet<>();
        NWorkspace ws = session.getWorkspace();
        NId runnerId = null;
        NId mainId = null;
        boolean tooManyMains = false;
        if (ids.isEmpty() || (ids.size() == 1 && ids.get(0).equals("nuts" ))) {
            nIds.add(ws.getApiId());
            nIds.add(ws.getRuntimeId());
            nIds.add(ws.getAppId());
            mainId = ws.getAppId();
            runnerId = ws.getAppId();
        } else {
            List<NId> secondaryIds = new ArrayList<>();
            for (String id : ids) {
                if ("nuts".equals(id)) {
                    NId apiId = ws.getApiId();
                    NId appId = resolveNutsAppIdFromApiId(apiId);
                    nIds.add(apiId);
                    runnerId = appId;
                    secondaryIds.add(appId);
                } else if ("nuts-runtime".equals(id)) {
                    NId apiId = ws.getApiId();
                    NId appId = resolveNutsAppIdFromApiId(apiId);
                    nIds.add(apiId);
                    nIds.add(appId);
                    nIds.add(ws.getRuntimeId());
                    runnerId = appId;
                    secondaryIds.add(appId);
                } else {
                    List<NId> found = NSearchCmd.of().addId(id)
                            .setLatest(true)
                            .setDistinct(true)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setInlineDependencies(true)
                            .getResultIds().toList();
                    if (found.isEmpty()) {
                        throw new NNotFoundException(NId.get(id).get());
                    }
                    for (NId resultId : found) {
                        if (resultId.getShortName().equals(ws.getApiId().getShortName())) {
                            NId appId = resolveNutsAppIdFromApiId(resultId);
                            nIds.add(appId);
                            runnerId = appId;
                            secondaryIds.add(appId);
                        } else if (mainId == null) {
                            mainId = resultId;
                        } else {
                            tooManyMains = true;
                        }
                        nIds.add(resultId);
                    }
                }
            }
            if (mainId == null) {
                if (secondaryIds.size() >= 0) {
                    if (secondaryIds.size() == 1) {
                        mainId = secondaryIds.get(0);
                    } else {
                        mainId = secondaryIds.get(0);
                        tooManyMains = true;
                    }
                }
            }
            cmdLine.throwUnexpectedArgument(NMsg.ofC("%s", ids));
        }
        Set<String> sId = new TreeSet<>(ids);
        if (sId.size() > 3) {
            sId = new LinkedHashSet<>(sId.stream().limit(3).collect(Collectors.toSet()));
            sId.add("etc" );
        }
        if (sId.isEmpty()) {
            sId.add("nuts" );
        }
        String defaultName = sId.stream().map(x -> NId.get(x).get().getArtifactId()).distinct().sorted()
                .collect(Collectors.joining("-" )) + "-bundle";
        String appName = NStringUtils.firstNonBlank(withAppName.get(), withAppTitle.get(), defaultName);

        switch (format) {
            case "jar":
            case "zip": {
                rootFolder = NPath.ofTempFolder("bundle" );
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle" );
                break;
            }
            case "exploded": {
                rootFolder = NBlankable.isBlank(withTarget.get()) ?
                        NPath.ofUserDirectory().resolve(appName + "-bundle" )
                        : NPath.of(withTarget.get())
                ;
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle" );
                if (withClean.get()) {
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
                rootFolder = NBlankable.isBlank(withTarget.get()) ?
                        NPath.ofUserDirectory().resolve(appName + "-bundle" )
                        : NPath.of(withTarget.get())
                ;
                bundleFolder = rootFolder;
                if (withClean.get()) {
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
                    .from(getClass().getResource("/META-INF/bundle/NutsBundleRunner.class.template" ))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("net/thevpc/nuts/runtime/standalone/installer/NutsBundleRunner.class" ))
                    .run();
            cp
                    .from(getClass().getResource("/META-INF/bundle/MANIFEST-COPY.MF" ))
                    .setMkdirs(true)
                    .to(rootFolder.resolve("META-INF/MANIFEST.MF" ))
                    .run();
        }


        NFetchCmd f = NFetchCmd.of();
        NStringBuilder nuts_bundle_files_config = new NStringBuilder();
        NStringBuilder nuts_bundle_info_config = new NStringBuilder();

        //load all definitions
        Map<NId, NDefinition> allIds = new HashMap<>();
        Stack<NId> toProcess = new Stack<>();
        toProcess.addAll(nIds);
        while (!toProcess.isEmpty()) {
            NId id = toProcess.pop();
            if (!allIds.containsKey(id)) {
                allIds.put(id, null);
                NDefinition resultDefinition = f.setId(id).setContent(true).getResultDefinition();
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
                    nuts_bundle_files_config.println("copy /" + fullPath
                            + " $target/"
                            + fullPath
                    );
                }
            }

            fullPath = ExtraApiUtils.resolveNutsDescriptorPath(id);
            cp.from(NDescriptorFormat.of().setValue(resultDefinition.getDescriptor()).setNtf(false).toString().getBytes())
                    .to(bundleFolder.resolve(fullPath))
                    .run();
            if (includeConfigFiles) {
                nuts_bundle_files_config.println("copy /" + fullPath
                        + " $target/"
                        + fullPath
                );
            }
        }
        if (runnerId == null) {
            runnerId = ws.getAppId();
        }
        bundleFolder.resolve(".nuts-repository" ).writeString("{}" );
        if (includeConfigFiles) {
            nuts_bundle_files_config.println("copy /.nuts-repository $target/.nuts-repository" );
            for (NId id : toBaseDir) {
                String fullPath = ExtraApiUtils.resolveJarPath(id);

                nuts_bundle_files_config.println("copy /" + fullPath
                        + " $target/"
                        + NPath.of(fullPath).getName()
                );
                nuts_bundle_files_config.println(
                        "copy /" + fullPath
                                + " ${user.dir}/"
                                + NPath.of(fullPath).getName()
                );
            }
            String mainIdStr = "";
            if (mainId != null
                    && !NConstants.Ids.NUTS_API.equals(mainId.getShortName())
                    && !NConstants.Ids.NUTS_APP.equals(mainId.getShortName())
                    && !NConstants.Ids.NUTS_RUNTIME.equals(mainId.getShortName())
            ) {
                mainIdStr = mainId.getLongName();
            }
            // create posix runner
            bundleFolder.resolve("posix-runner.sh" ).writeString(
                    new NStringBuilder()
                            .println("#!/bin/sh" )
                            .println("" )
                            .println("java -jar \"lib/" + ExtraApiUtils.resolveJarPath(runnerId) + "\" --repo==lib " +
                                    (mainIdStr.isEmpty() ? "" : ("'" + mainIdStr + "'" ))
                                    + " \"$@\"" )
                            .build()
            );
            nuts_bundle_files_config.println(
                    "copy /posix-runner.sh"
                            + " ${user.dir}/" + appName + ".sh"
            );
            // create posix windows runner
            bundleFolder.resolve("windows-runner.bat" ).writeString(
                    new NStringBuilder()
                            .println("" )
                            .println("java.exe -jar \"lib/" + ExtraApiUtils.resolveJarPath(runnerId) + "\" --repo==lib " +
                                    (mainIdStr.isEmpty() ? "" : ("\"" + mainIdStr + "\"" ))
                                    + " $*" )
                            .build()
            );
            nuts_bundle_files_config.println(
                    "copy /windows-runner.bat"
                            + " ${user.dir}/" + appName + ".bat"
            );
            rootFolder.resolve("META-INF/nuts-bundle-files.config" ).writeString(nuts_bundle_files_config.toString());
        }


        String appVersion = NStringUtils.firstNonBlank(withAppVersion.get(), mainId == null ? null : mainId.getVersion().toString(), "1.0" );
        String appTitle = NStringUtils.firstNonBlank(withAppTitle.get(), withAppName.get(), defaultName);
        String appDesc = NStringUtils.firstNonBlank(withAppDesc.get(), withAppTitle.get());

        if (includeConfigFiles) {

            nuts_bundle_info_config.println("target=${user.dir}/lib" );
            if (appVersion != null) {
                nuts_bundle_info_config.println("version=" + appVersion);
            }
            if (appTitle != null) {
                nuts_bundle_info_config.println("title=" + appTitle);
            }
            if (appName != null) {
                nuts_bundle_info_config.println("name=" + appName);
            }
            if (appDesc != null) {
                nuts_bundle_info_config.println("description=" + appDesc);
            }
            rootFolder.resolve("META-INF/nuts-bundle-info.config" ).writeString(nuts_bundle_info_config.toString());
        }

        NSession nSession = NSession.of();
        switch (format) {
            case "jar": {
                NCompress zip = NCompress.of().setPackaging("zip" );
                NPath target = NPath.of(NStringUtils.firstNonBlank(withTarget.get(),
                        appName + ".jar" )).toAbsolute();
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
                        NTrace.out().println(NMsg.ofC("bundle created %s", target));
                    } else {
                        NTrace.out().println(NMapBuilder.of().put("bundlePath", target).build());
                    }
                }
                break;
            }
            case "zip": {
                NCompress zip = NCompress.of().setPackaging("zip" );
                NPath target = NPath.of(NStringUtils.firstNonBlank(withTarget.get(),
                        appName + ".zip" )).toAbsolute();
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
                        NTrace.out().println(NMsg.ofC("bundle created %s", target));
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
                        NTrace.out().println(NMsg.ofC("bundle created %s", target));
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

    private NId resolveNutsAppIdFromApiId(NId apiId) {
        NVersion v = apiId.getVersion();
        if (v.compareTo("0.8.5" ) < 0) {
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
