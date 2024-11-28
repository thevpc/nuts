/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
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

    public DefaultNBundleInternalExecutable(NWorkspace workspace,String[] args, NExecCmd execCommand) {
        super(workspace,"bundle", args, execCommand);
    }

    @Override
    public int execute() {
        NSession session = workspace.currentSession();
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
                                withFormat.set("dir");
                            }
                        });
                        break;
                    }
                    case "--exploded":
                    case "--as-exploded": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("exploded");
                            }
                        });
                        break;
                    }
                    case "--jar":
                    case "--as-jar": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("jar");
                            }
                        });
                        break;
                    }
                    case "--as-zip":
                    case "--zip": {
                        cmdLine.withNextFlag((v, ar) -> {
                            if (v) {
                                withFormat.set("zip");
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
        if (ids.isEmpty() || (ids.size() == 1 && ids.get(0).equals("nuts"))) {
            nIds.add(session.getWorkspace().getApiId());
            nIds.add(session.getWorkspace().getRuntimeId());
        } else {
            for (String id : ids) {
                if ("nuts".equals(id)) {
                    NId apiId = session.getWorkspace().getApiId();
                    toBaseDir.add(apiId);
                    nIds.add(apiId);
                } else if ("nuts-runtime".equals(id)) {
                    NId apiId = session.getWorkspace().getApiId();
                    toBaseDir.add(apiId);
                    nIds.add(apiId);
                    nIds.add(session.getWorkspace().getRuntimeId());
                } else {
                    List<NId> found = NSearchCmd.of().addId(id)
                            .setLatest(true)
                            .setDistinct(true)
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .setInlineDependencies(true)
                            .getResultIds().toList();
                    if (found.isEmpty()) {
                        throw new NNotFoundException(NId.of(id).get());
                    }
                    for (NId resultId : found) {
                        if (resultId.getShortName().equals(session.getWorkspace().getApiId().getShortName())) {
                            toBaseDir.add(resultId);
                        }
                        nIds.add(resultId);
                    }
                }
            }
            cmdLine.throwUnexpectedArgument(NMsg.ofC("%s", ids));
        }
        Set<String> sId = new TreeSet<>(ids);
        if (sId.size() > 3) {
            sId = new LinkedHashSet<>(sId.stream().limit(3).collect(Collectors.toSet()));
            sId.add("etc");
        }
        if (sId.isEmpty()) {
            sId.add("nuts");
        }
        String defaultName = sId.stream().map(x -> NId.of(x).get().getArtifactId()).distinct().sorted()
                .collect(Collectors.joining("-")) + "-bundle";
        String appName = NStringUtils.firstNonBlank(withAppName.get(), withAppTitle.get(), defaultName);

        switch (format) {
            case "jar":
            case "zip": {
                rootFolder = NPath.ofTempFolder("bundle");
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
                break;
            }
            case "exploded": {
                rootFolder = NBlankable.isBlank(withTarget.get()) ?
                        NPath.ofUserDirectory().resolve(appName + "-bundle")
                        : NPath.of(withTarget.get())
                ;
                includeConfigFiles = true;
                bundleFolder = rootFolder.resolve("META-INF/bundle");
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
                        NPath.ofUserDirectory().resolve(appName + "-bundle")
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

        bundleFolder.resolve(".nuts-repository").writeString("{}");
        if (includeConfigFiles) {
            nuts_bundle_files_config.println("copy /.nuts-repository $target/.nuts-repository");
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
            rootFolder.resolve("META-INF/nuts-bundle-files.config").writeString(nuts_bundle_files_config.toString());
        }


        String appVersion = NStringUtils.firstNonBlank(withAppVersion.get(), "1.0");
        String appTitle = NStringUtils.firstNonBlank(withAppTitle.get(), withAppName.get(), defaultName);
        String appDesc = NStringUtils.firstNonBlank(withAppDesc.get(), withAppTitle.get());

        if (includeConfigFiles) {

            nuts_bundle_info_config.println("target=${user.dir}/lib");
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
            rootFolder.resolve("META-INF/nuts-bundle-info.config").writeString(nuts_bundle_info_config.toString());
        }

        switch (format) {
            case "jar": {
                NCompress zip = NCompress.of().setPackaging("zip");
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(
                                NStringUtils.firstNonBlank(withTarget.get(),
                                        appName + ".jar")
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                break;
            }
            case "zip": {
                NCompress zip = NCompress.of().setPackaging("zip");
                zip.addSource(rootFolder)
                        .setSkipRoot(true)
                        .setTarget(
                                NStringUtils.firstNonBlank(withTarget.get(),
                                        appName + ".zip")
                        )
                        .run();
                if (tempBundleFolder) {
                    rootFolder.deleteTree();
                }
                break;
            }
            case "dir":
            case "exploded": {
                break;
            }
            default: {
                cmdLine.throwError(NMsg.ofC("invalid format %s", format));
            }
        }
        return NExecutionException.SUCCESS;
    }

}
