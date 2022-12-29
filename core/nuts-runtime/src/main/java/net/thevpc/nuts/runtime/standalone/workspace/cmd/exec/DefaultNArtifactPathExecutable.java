/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNInstallInfo;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NDependencySolver;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.util.NLogger;
import net.thevpc.nuts.util.NLoggerVerb;

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
public class DefaultNArtifactPathExecutable extends AbstractNExecutableCommand {

    private final NLogger LOG;
    String cmdName;
    String[] args;
    List<String> executorOptions;
    List<String> workspaceOptions;
    NExecutionType executionType;
    NRunAs runAs;
    NSession session;
    NSession execSession;
    DefaultNExecCommand execCommand;

    public DefaultNArtifactPathExecutable(String cmdName, String[] args, List<String> executorOptions, List<String> workspaceOptions, NExecutionType executionType, NRunAs runAs, NSession session, NSession execSession, DefaultNExecCommand execCommand, boolean inheritSystemIO) {
        super(cmdName,
                NCommandLine.of(args).toString(),
                NExecutableType.ARTIFACT);
        LOG = NLogger.of(DefaultNArtifactPathExecutable.class, session);
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
    public NId getId() {
        try (final CharacterizedExecFile c = characterizeForExec(NPath.of(cmdName, session), session, executorOptions)) {
            return c.getDescriptor() == null ? null : c.getDescriptor().getId();
        }
    }

    @Override
    public void execute() {
        executeHelper();
    }

    public void executeHelper() {
        try (final CharacterizedExecFile c = characterizeForExec(NPath.of(cmdName, session), session, executorOptions)) {
            if (c.getDescriptor() == null) {
                throw new NNotFoundException(execSession, null, NMsg.ofCstyle("unable to resolve a valid descriptor for %s", cmdName), null);
            }
            String tempFolder = NPaths.of(session)
                    .createTempFolder("exec-path-").toString();
            NId _id = c.getDescriptor().getId();
            DefaultNDefinition nutToRun = new DefaultNDefinition(
                    null,
                    null,
                    _id.getLongId(),
                    c.getDescriptor(),
                    NPath.of(c.getContentFile(), execSession).setUserCache(false).setUserTemporary(c.getTemps().size() > 0)
                    ,
                    DefaultNInstallInfo.notInstalled(_id),
                    null, session
            );
            NDependencySolver resolver = NDependencySolver.of(session);
            NDependencyFilters ff = NDependencyFilters.of(session);

            resolver
                    .setFilter(ff.byScope(NDependencyScopePattern.RUN)
//                            .and(ff.byOptional(getOptional())
//                            ).and(getDependencyFilter())
                    );
            for (NDependency dependency : c.getDescriptor().getDependencies()) {
                resolver.add(dependency);
            }
            nutToRun.setDependencies(resolver.solve());
//            System.out.println(String.join(" ",args));
            try {
                execCommand.ws_execId(nutToRun, cmdName, args, executorOptions, workspaceOptions, execCommand.getEnv(),
                        execCommand.getDirectory(), execCommand.isFailFast(), true, session, execSession, executionType, runAs);
            } finally {
                try {
                    CoreIOUtils.delete(session, Paths.get(tempFolder));
                } catch (UncheckedIOException | NIOException e) {
                    LOG.with().session(session).level(Level.FINEST).verb(NLoggerVerb.FAIL)
                            .log(NMsg.ofJstyle("unable to delete temp folder created for execution : {0}", tempFolder));
                }
            }
        }
    }

    public static CharacterizedExecFile characterizeForExec(NInputSource contentFile, NSession session, List<String> execOptions) {
        String classifier = null;//TODO how to get classifier?
        CharacterizedExecFile c = new CharacterizedExecFile(session);
        try {
            c.setStreamOrPath(contentFile);
            c.setContentFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true, session));
            Path fileSource = c.getContentFile();
            if (!Files.exists(fileSource)) {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("file does not exists %s", fileSource));
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.setDescriptor(NDescriptorParser.of(session).parse(ext).get(session));
                } else {
                    c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions, session));
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = NPath.of(fileSource + ".zip", session)
                                .toAbsolute().toFile();
                        ZipUtils.zip(session, fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentFile(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid nuts folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.getContentFile().getFileName().toString().endsWith(NConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = Files.newInputStream(c.getContentFile())) {
                        c.setDescriptor(NDescriptorParser.of(session).parse(in).get(session));
                    }
                    c.setContentFile(null);
                    if (c.getStreamOrPath() instanceof NPath && ((NPath) c.getStreamOrPath()).isURL()) {
                        URLBuilder ub = new URLBuilder(((NPath) c.getStreamOrPath()).toURL().toString());
                        try {
                            c.setContentFile(CoreIOUtils.toPathInputSource(
                                    NPath.of(ub.resolveSibling(session.locations().getDefaultIdFilename(c.getDescriptor().getId())).toURL(), session),
                                    c.getTemps(), true, session));
                        } catch (Exception ex) {
                            //TODO FIX ME
                            ex.printStackTrace();
                        }
                    }
                    if (c.getContentFile() == null) {
                        for (NIdLocation location0 : c.getDescriptor().getLocations()) {
                            if (CoreFilterUtils.acceptClassifier(location0, classifier)) {
                                String location = location0.getUrl();
                                if (NPath.of(location, session).isHttp()) {
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NPath.of(new URL(location), session),
                                                c.getTemps(), true, session));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                } else {
                                    URLBuilder ub = new URLBuilder(((NPath)c.getStreamOrPath()).toURL().toString());
                                    try {
                                        c.setContentFile(CoreIOUtils.toPathInputSource(
                                                NPath.of(ub.resolveSibling(session.locations()
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
                        throw new NIllegalArgumentException(session, NMsg.ofCstyle("unable to locale package for %s", c.getStreamOrPath()));
                    }
                } else {
                    c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getContentFile(), execOptions, session));
                    if (c.getDescriptor() == null) {
                        CoreDigestHelper d = new CoreDigestHelper(session);
                        d.append(c.getContentFile());
                        String artifactId = d.getDigest();
                        c.setDescriptor(new DefaultNDescriptorBuilder()
                                .setId("temp:" + artifactId + "#1.0")
                                .setPackaging(CoreIOUtils.getFileExtension(contentFile.getInputMetaData().getName().orElse("")))
                                .build());
                    }
                }
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("path does not denote a valid file or folder %s", c.getStreamOrPath()));
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return c;
    }

    @Override
    public String toString() {
        return "nuts " + cmdName + " " + NCommandLine.of(args).toString();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
