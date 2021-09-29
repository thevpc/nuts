/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.model.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.DefaultNutsInstallInfo;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.URLBuilder;
import net.thevpc.nuts.runtime.bundles.io.ZipOptions;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.runtime.standalone.wscommands.search.NutsDependenciesResolver;

/**
 * @author thevpc
 */
public class DefaultNutsArtifactPathExecutable extends AbstractNutsExecutableCommand {

    private final NutsLogger LOG;
    String cmdName;
    String[] args;
    String[] executorOptions;
    NutsExecutionType executionType;
    NutsRunAs runAs;
    NutsSession traceSession;
    NutsSession execSession;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsArtifactPathExecutable(String cmdName, String[] args, String[] executorOptions, NutsExecutionType executionType, NutsRunAs runAs,NutsSession traceSession, NutsSession execSession, DefaultNutsExecCommand execCommand, boolean inheritSystemIO) {
        super(cmdName,
                execSession.commandLine().create(args).toString(),
                NutsExecutableType.ARTIFACT);
        LOG = execSession.log().of(DefaultNutsArtifactPathExecutable.class);
        this.runAs = runAs;
        this.cmdName = cmdName;
        this.args = args;
        this.executionType = executionType;
        this.traceSession = traceSession;
        this.execSession = execSession;
        this.execCommand = execCommand;
        List<String> executorOptionsList = new ArrayList<>();
        for (String option : executorOptions) {
            NutsArgument a = traceSession.commandLine().createArgument(option);
            if (a.getKey().getString().equals("--nuts-auto-install")) {
                if (a.isKeyValue()) {
//                    autoInstall= a.isNegated() != a.getBooleanValue();
                } else {
//                    autoInstall=true;
                }
            } else {
                executorOptionsList.add(option);
            }
        }
        this.executorOptions = executorOptionsList.toArray(new String[0]);
    }

    @Override
    public NutsId getId() {
        try (final CharacterizedExecFile c = characterizeForExec(execSession.io().input().of(cmdName), traceSession, executorOptions)) {
            return c.descriptor == null ? null : c.descriptor.getId();
        }
    }

    @Override
    public void execute() {
        executeHelper(false);
    }

    @Override
    public void dryExecute() {
        executeHelper(true);
    }

    public void executeHelper(boolean dry) {
        NutsWorkspace ws = execSession.getWorkspace();
        try (final CharacterizedExecFile c = characterizeForExec(ws.io().input().of(cmdName), traceSession, executorOptions)) {
            if (c.descriptor == null) {
                throw new NutsNotFoundException(execSession, null, NutsMessage.cstyle("unable to resolve a valid descriptor for %s",cmdName), null);
            }
            String tempFolder = ws.io().tmp()
                    .setSession(traceSession)
                    .createTempFolder("exec-path-");
            NutsId _id = c.descriptor.getId();
            NutsIdType idType = NutsWorkspaceExt.of(ws).resolveNutsIdType(_id, traceSession);
            DefaultNutsDefinition nutToRun = new DefaultNutsDefinition(
                    null,
                    null,
                    _id,
                    c.descriptor,
                    new NutsDefaultContent(
                            execSession.io().path(c.getContentPath())
                            , false, c.temps.size() > 0),
                    DefaultNutsInstallInfo.notInstalled(_id),
                    idType, null, traceSession
            );
            NutsDependenciesResolver resolver = new NutsDependenciesResolver(traceSession);
            NutsDependencyFilterManager ff = ws.dependency().filter();

            resolver
                    .setDependencyFilter(ff.byScope(NutsDependencyScopePattern.RUN)
//                            .and(ff.byOptional(getOptional())
//                            ).and(getDependencyFilter())
                    );
            for (NutsDependency dependency : c.descriptor.getDependencies()) {
                resolver.addRootDefinition(dependency);
            }
            nutToRun.setDependencies(resolver.resolve());

            try {
                execCommand.ws_execId(nutToRun, cmdName, args, executorOptions, execCommand.getEnv(),
                        execCommand.getDirectory(), execCommand.isFailFast(), true, traceSession, execSession, executionType,runAs, dry);
            } finally {
                try {
                    CoreIOUtils.delete(traceSession, Paths.get(tempFolder));
                } catch (UncheckedIOException | NutsIOException e) {
                    LOG.with().session(traceSession).level(Level.FINEST).verb(NutsLogVerb.FAIL).log("Unable to delete temp folder created for execution : " + tempFolder);
                }
            }
        }
    }

    public static CharacterizedExecFile characterizeForExec(NutsInput contentFile, NutsSession session, String[] execOptions) {
        NutsWorkspace ws = session.getWorkspace();
        String classifier = null;//TODO how to get classifier?
        CharacterizedExecFile c = new CharacterizedExecFile();
        try {
            c.baseFile = contentFile;
            c.contentFile = CoreIOUtils.toPathInputSource(contentFile, c.temps, session);
            Path fileSource = c.contentFile.getFilePath();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("file does not exists %s",fileSource));
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.descriptor = ws.descriptor().parser().setSession(session).parse(ext);
                } else {
                    c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, execOptions, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(ws.io().path(fileSource.toString() + ".zip")
                                .builder().withAppBaseDir().build().toString()
                        );
                        ZipUtils.zip(session.getWorkspace(), fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = ws.io().input().setMultiRead(true).of(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid nuts folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.contentFile.getName().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = c.contentFile.open()) {
                        c.descriptor = ws.descriptor().parser().setSession(session).parse(in);
                    }
                    c.contentFile = null;
                    if (c.baseFile.isURL()) {
                        URLBuilder ub = new URLBuilder(c.baseFile.getURL().toString());
                        try {
                            c.contentFile = CoreIOUtils.toPathInputSource(
                                    ws.io().input().of(ub.resolveSibling(ws.locations().getDefaultIdFilename(c.descriptor.getId())).toURL()),
                                    c.temps, session);
                        } catch (Exception ex) {

                        }
                    }
                    if (c.contentFile == null) {
                        for (NutsIdLocation location0 : c.descriptor.getLocations()) {
                            if (CoreNutsUtils.acceptClassifier(location0, classifier)) {
                                String location = location0.getUrl();
                                if (CoreIOUtils.isPathHttp(location)) {
                                    try {
                                        c.contentFile = CoreIOUtils.toPathInputSource(
                                                ws.io().input().of(new URL(location)),
                                                c.temps, session);
                                    } catch (Exception ex) {

                                    }
                                } else {
                                    URLBuilder ub = new URLBuilder(c.baseFile.getURL().toString());
                                    try {
                                        c.contentFile = CoreIOUtils.toPathInputSource(
                                                ws.io().input().of(ub.resolveSibling(ws.locations().getDefaultIdFilename(c.descriptor.getId())).toURL()),
                                                c.temps, session);
                                    } catch (Exception ex) {

                                    }
                                }
                                if (c.contentFile == null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (c.contentFile == null) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to locale package for %s" , c.baseFile));
                    }
                } else {
                    c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, execOptions, session);
                    if (c.descriptor == null) {
                        c.descriptor = ws.descriptor().descriptorBuilder()
                                .setId("temp")
                                .setPackaging(CoreIOUtils.getFileExtension(contentFile.getName()))
                                .build();
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("path does not denote a valid file or folder %s", c.baseFile));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    @Override
    public String toString() {
        return "NUTS " + cmdName + " " + execSession.commandLine().create(args).toString();
    }

    public static class CharacterizedExecFile implements AutoCloseable {

        public NutsInput contentFile;
        public NutsInput baseFile;
        public List<Path> temps = new ArrayList<>();
        public NutsDescriptor descriptor;
        public NutsId executor;

        public Path getContentPath() {
            return contentFile.getFilePath();
        }

        public void addTemp(Path f) {
            temps.add(f);
        }

        @Override
        public void close() {
            for (Iterator<Path> it = temps.iterator(); it.hasNext();) {
                Path temp = it.next();
                try {
                    Files.delete(temp);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
                it.remove();
            }
        }
    }

}
