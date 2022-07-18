/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsInputSource;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsInstallInfo;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NutsDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.util.NutsLogger;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNutsArtifactPathExecutable extends AbstractNutsExecutableCommand {

    private final NutsLogger LOG;
    String cmdName;
    String[] args;
    List<String> executorOptions;
    List<String> workspaceOptions;
    NutsExecutionType executionType;
    NutsRunAs runAs;
    NutsSession session;
    NutsSession execSession;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsArtifactPathExecutable(String cmdName, String[] args, List<String> executorOptions, List<String> workspaceOptions, NutsExecutionType executionType, NutsRunAs runAs, NutsSession session, NutsSession execSession, DefaultNutsExecCommand execCommand, boolean inheritSystemIO) {
        super(cmdName,
                NutsCommandLine.of(args).toString(),
                NutsExecutableType.ARTIFACT);
        LOG = NutsLogger.of(DefaultNutsArtifactPathExecutable.class, session);
        this.runAs = runAs;
        this.cmdName = cmdName;
        this.args = args;
        this.executionType = executionType;
        this.session = session;
        this.execSession = execSession;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions;
        this.workspaceOptions = workspaceOptions;
    }

    @Override
    public NutsId getId() {
        try (final CharacterizedExecFile c = characterizeForExec(NutsPath.of(cmdName, session), session, executorOptions)) {
            return c.getDescriptor() == null ? null : c.getDescriptor().getId();
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
        try (final CharacterizedExecFile c = characterizeForExec(NutsPath.of(cmdName, session), session, executorOptions)) {
            if (c.getDescriptor() == null) {
                throw new NutsNotFoundException(execSession, null, NutsMessage.ofCstyle("unable to resolve a valid descriptor for %s", cmdName), null);
            }
            String tempFolder = NutsPaths.of(session)
                    .createTempFolder("exec-path-").toString();
            NutsId _id = c.getDescriptor().getId();
            DefaultNutsDefinition nutToRun = new DefaultNutsDefinition(
                    null,
                    null,
                    _id.getLongId(),
                    c.getDescriptor(),
                    NutsPath.of(c.getContentFile(), execSession).setUserCache(false).setUserTemporary(c.getTemps().size() > 0)
                    ,
                    DefaultNutsInstallInfo.notInstalled(_id),
                    null, session
            );
            NutsDependencySolver resolver = NutsDependencySolver.of(session);
            NutsDependencyFilters ff = NutsDependencyFilters.of(session);

            resolver
                    .setFilter(ff.byScope(NutsDependencyScopePattern.RUN)
//                            .and(ff.byOptional(getOptional())
//                            ).and(getDependencyFilter())
                    );
            for (NutsDependency dependency : c.getDescriptor().getDependencies()) {
                resolver.add(dependency);
            }
            nutToRun.setDependencies(resolver.solve());
//            System.out.println(String.join(" ",args));
            try {
                execCommand.ws_execId(nutToRun, cmdName, args, executorOptions, workspaceOptions, execCommand.getEnv(),
                        execCommand.getDirectory(), execCommand.isFailFast(), true, session, execSession, executionType, runAs, dry);
            } finally {
                try {
                    CoreIOUtils.delete(session, Paths.get(tempFolder));
                } catch (UncheckedIOException | NutsIOException e) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLoggerVerb.FAIL)
                            .log(NutsMessage.ofJstyle("unable to delete temp folder created for execution : {0}", tempFolder));
                }
            }
        }
    }

    public static CharacterizedExecFile characterizeForExec(NutsInputSource contentFile, NutsSession session, List<String> execOptions) {
        String classifier = null;//TODO how to get classifier?
        CharacterizedExecFile c = new CharacterizedExecFile(session);
        try {
            c.setStreamOrPath(contentFile);
            c.setContentFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true, session));
            Path fileSource = c.getContentFile();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("file does not exists %s", fileSource));
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.setDescriptor(NutsDescriptorParser.of(session).parse(ext).get(session));
                } else {
                    c.setDescriptor(NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions, session));
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = NutsPath.of(fileSource + ".zip", session)
                                .toAbsolute().toFile();
                        ZipUtils.zip(session, fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentFile(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("invalid nuts folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.getContentFile().getFileName().toString().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = Files.newInputStream(c.getContentFile())) {
                        c.setDescriptor(NutsDescriptorParser.of(session).parse(in).get(session));
                    }
                    c.setContentFile(null);
                    if (c.getStreamOrPath() instanceof NutsPath && ((NutsPath) c.getStreamOrPath()).isURL()) {
                        URLBuilder ub = new URLBuilder(((NutsPath) c.getStreamOrPath()).toURL().toString());
                        try {
                            c.setContentFile(CoreIOUtils.toPathInputSource(
                                    NutsPath.of(ub.resolveSibling(session.locations().getDefaultIdFilename(c.getDescriptor().getId())).toURL(), session),
                                    c.getTemps(), true, session));
                        } catch (Exception ex) {
                            //TODO FIX ME
                            ex.printStackTrace();
                        }
                    }
                    if (c.getContentFile() == null) {
                        for (NutsIdLocation location0 : c.getDescriptor().getLocations()) {
                            if (CoreFilterUtils.acceptClassifier(location0, classifier)) {
                                String location = location0.getUrl();
                                if (NutsPath.of(location, session).isHttp()) {
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NutsPath.of(new URL(location), session),
                                                c.getTemps(), true, session));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    URLBuilder ub = new URLBuilder(((NutsPath)c.getStreamOrPath()).toURL().toString());
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NutsPath.of(ub.resolveSibling(session.locations()
                                                        .getDefaultIdFilename(c.getDescriptor().getId())).toURL(), session),
                                                c.getTemps(), true, session));
                                    } catch (Exception ex) {
                                        //TODO add log here
                                        ex.printStackTrace();
                                    }
                                }
                                if (c.getContentFile() == null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (c.getContentFile() == null) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("unable to locale package for %s", c.getStreamOrPath()));
                    }
                } else {
                    c.setDescriptor(NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions, session));
                    if (c.getDescriptor() == null) {
                        CoreDigestHelper d = new CoreDigestHelper(session);
                        d.append(c.getContentFile());
                        String artifactId = d.getDigest();
                        c.setDescriptor(new DefaultNutsDescriptorBuilder()
                                .setId("temp:" + artifactId + "#1.0")
                                .setPackaging(CoreIOUtils.getFileExtension(contentFile.getInputMetaData().getName().orElse("")))
                                .build());
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("path does not denote a valid file or folder %s", c.getStreamOrPath()));
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return c;
    }

    @Override
    public String toString() {
        return "nuts " + cmdName + " " + NutsCommandLine.of(args).toString();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }
}
