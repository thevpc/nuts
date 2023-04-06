/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NCompress;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNBundleInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNBundleInternalExecutable(String[] args, NSession session) {
        super("bundle", args, session);
    }

    @Override
    public void execute() {
        if (getSession().isDry()) {
            dryExecute();
            return;
        }
        if (NAppUtils.processHelpOptions(args, getSession())) {
            showDefaultHelp();
            return;
        }
        NSession session = getSession();
        NCmdLine commandLine = NCmdLine.of(args);
        List<String> ids = new ArrayList<>();
        NRef<Boolean> withDependencies = NRef.of(true);
        NRef<Boolean> withOptional = NRef.of(false);
        NRef<String> withAppVersion = NRef.of(null);
        NRef<String> withAppName = NRef.of(null);
        NRef<String> withAppTitle = NRef.of(null);
        NRef<String> withAppDesc = NRef.of(null);
        NRef<String> withTarget = NRef.of(null);
        while (commandLine.hasNext()) {
            NArg a = commandLine.peek().get(session);
            if (a.isOption()) {
                switch (a.key()) {
                    case "--optional": {
                        commandLine.withNextFlag((v, ar, s) -> withOptional.set(v));
                        break;
                    }
                    case "--deps":
                    case "--dependencies": {
                        commandLine.withNextFlag((v, ar, s) -> withDependencies.set(v));
                        break;
                    }
                    case "--app-version": {
                        commandLine.withNextEntry((v, ar, s) -> withAppVersion.set(v));
                        break;
                    }
                    case "--app-name":
                    case "--name": {
                        commandLine.withNextEntry((v, ar, s) -> withAppName.set(v));
                        break;
                    }
                    case "--app-desc":
                    case "--desc": {
                        commandLine.withNextEntry((v, ar, s) -> withAppDesc.set(v));
                        break;
                    }
                    case "--app-title":
                    case "--title": {
                        commandLine.withNextEntry((v, ar, s) -> withAppTitle.set(v));
                        break;
                    }
                    case "--target": {
                        commandLine.withNextEntry((v, ar, s) -> withTarget.set(v));
                        break;
                    }
                    default: {
                        session.configureLast(commandLine);
                    }
                }
            } else {
                ids.add(commandLine.next().get().toString());
            }
        }
        NPath d = NPaths.of(session).createTempFolder("bundle");
        NCp cp = NCp.of(session);
        cp
                .from(getClass().getResource("/META-INF/bundle/NutsBundleRunner.class"))
                .setMkdirs(true)
                .to(d.resolve("net/thevpc/nuts/runtime/standalone/installer/NutsBundleRunner.class"))
                .run();
        cp
                .from(getClass().getResource("/META-INF/bundle/MANIFEST-COPY.MF"))
                .setMkdirs(true)
                .to(d.resolve("META-INF/MANIFEST.MF"))
                .run();
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
                    for (NId resultId : NSearchCommand.of(session).addId(id)
                            .setLatest(true)
                            .setDistinct(true)
                            .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                            .setInlineDependencies(true)
                            .getResultIds()) {
                        if (resultId.getShortName().equals(session.getWorkspace().getApiId().getShortName())) {
                            toBaseDir.add(resultId);
                        }
                        nIds.add(resultId);
                    }
                }
            }
            commandLine.throwUnexpectedArgument(NMsg.ofC("%s", ids));
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
        NFetchCommand f = NFetchCommand.of(session);
        NStringBuilder nuts_bundle_files_config = new NStringBuilder();
        NStringBuilder nuts_bundle_info_config = new NStringBuilder();

        for (NId id : nIds) {
            String fileName = id.getArtifactId() + "-" + id.getVersion() + ".jar";
            String fullPath = id.getGroupId().replace('.', '/')
                    + '/'
                    + id.getArtifactId()
                    + '/'
                    + fileName;
            cp.from(f
                            .setId(id)
                            .getResultContent())
                    .to(d.resolve("META-INF/bundle").resolve(fullPath))
                    .run();
            nuts_bundle_files_config.println("copy /" + fullPath
                    + " $target/"
                    + fullPath
            );

             fileName = id.getArtifactId() + "-" + id.getVersion() + ".nuts";
             fullPath = id.getGroupId().replace('.', '/')
                    + '/'
                    + id.getArtifactId()
                    + '/'
                    + fileName;
            cp.from(
                    NDescriptorFormat.of(session).setValue(f.setId(id).getResultDescriptor()).setNtf(false).toString().getBytes()
                    )
                    .to(d.resolve("META-INF/bundle").resolve(fullPath))
                    .run();
            nuts_bundle_files_config.println("copy /" + fullPath
                    + " $target/"
                    + fullPath
            );


        }
        cp.from("{}".getBytes())
                .to(d.resolve("META-INF/bundle").resolve(".nuts-repository"))
                .run();
        nuts_bundle_files_config.println("copy /.nuts-repository $target/.nuts-repository");
        for (NId id : toBaseDir) {
            String fileName = id.getArtifactId() + "-" + id.getVersion() + ".jar";
            String fullPath = id.getGroupId().replace('.', '/')
                    + '/'
                    + id.getArtifactId()
                    + '/'
                    + fileName;
            nuts_bundle_files_config.println("copy /" + fullPath
                    + " $target/"
                    + fileName
            );
            nuts_bundle_files_config.println(
                    "copy /" + fullPath
                            + " ${user.dir}/"
                            + fileName
            );
        }
        d.resolve("META-INF/nuts-bundle-files.config").writeString(nuts_bundle_files_config.toString());


        nuts_bundle_info_config.println("target=${user.dir}/lib");

        String appVersion = NStringUtils.firstNonBlank(withAppVersion.get(), "1.0");
        String appTitle = NStringUtils.firstNonBlank(withAppTitle.get(), withAppName.get(), defaultName);
        String appName = NStringUtils.firstNonBlank(withAppName.get(), withAppTitle.get(), defaultName);
        String appDesc = NStringUtils.firstNonBlank(withAppDesc.get(), withAppTitle.get());

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
        d.resolve("META-INF/nuts-bundle-info.config").writeString(nuts_bundle_info_config.toString());

        NCompress zip = NCompress.of(session).setFormat("zip");
        zip.addSource(d)
                .setSkipRoot(true)
                .setTarget(
                        NStringUtils.firstNonBlank(withTarget.get(),
                                appName + ".jar")
                )
                .run();
    }

}
