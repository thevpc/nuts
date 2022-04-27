/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NutsDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.io.util.NutsStreamOrPath;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNutsInstallInfo;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.URLBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.security.util.CoreDigestHelper;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NutsDependencySolver;

/**
 * @author thevpc
 */
public class DefaultNutsArtifactPathExecutable extends AbstractNutsExecutableCommand {

    private final NutsLogger LOG;
    String cmdName;
    String[] args;
    List<String> executorOptions;
    NutsExecutionType executionType;
    NutsRunAs runAs;
    NutsSession session;
    NutsSession execSession;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsArtifactPathExecutable(String cmdName, String[] args, List<String> executorOptions, NutsExecutionType executionType, NutsRunAs runAs, NutsSession session, NutsSession execSession, DefaultNutsExecCommand execCommand, boolean inheritSystemIO) {
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
        List<String> executorOptionsList = new ArrayList<>();
        for (String option : executorOptions) {
            NutsArgument a = NutsArgument.of(option);
//            if (a.getKey().getString().equals("--nuts-auto-install")) {
//                if (a.isKeyValue()) {
//                    autoInstall= a.isNegated() != a.getBooleanValue().get(session);
//                } else {
//                    autoInstall=true;
//                }
//            } else {
                executorOptionsList.add(option);
//            }
        }
        this.executorOptions = executorOptionsList;
    }

    @Override
    public NutsId getId() {
        try (final CharacterizedExecFile c = characterizeForExec(NutsStreamOrPath.of(cmdName, session), session, executorOptions)) {
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
        try (final CharacterizedExecFile c = characterizeForExec(NutsStreamOrPath.of(cmdName, session), session, executorOptions)) {
            if (c.descriptor == null) {
                throw new NutsNotFoundException(execSession, null, NutsMessage.cstyle("unable to resolve a valid descriptor for %s",cmdName), null);
            }
            String tempFolder = NutsTmp.of(session)
                    .createTempFolder("exec-path-").toString();
            NutsId _id = c.descriptor.getId();
            DefaultNutsDefinition nutToRun = new DefaultNutsDefinition(
                    null,
                    null,
                    _id.getLongId(),
                    c.descriptor,
                    new DefaultNutsContent(
                            NutsPath.of(c.contentFile,execSession)
                            , false, c.temps.size() > 0),
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
            for (NutsDependency dependency : c.descriptor.getDependencies()) {
                resolver.add(dependency);
            }
            nutToRun.setDependencies(resolver.solve());

            try {
                execCommand.ws_execId(nutToRun, cmdName, args, executorOptions, execCommand.getEnv(),
                        execCommand.getDirectory(), execCommand.isFailFast(), true, session, execSession, executionType,runAs, dry);
            } finally {
                try {
                    CoreIOUtils.delete(session, Paths.get(tempFolder));
                } catch (UncheckedIOException | NutsIOException e) {
                    LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to delete temp folder created for execution : {0}",tempFolder));
                }
            }
        }
    }

    public static CharacterizedExecFile characterizeForExec(NutsStreamOrPath contentFile, NutsSession session, List<String> execOptions) {
        String classifier = null;//TODO how to get classifier?
        CharacterizedExecFile c = new CharacterizedExecFile(session);
        try {
            c.streamOrPath = contentFile;
            c.contentFile = CoreIOUtils.toPathInputSource(contentFile, c.temps,true, session);
            Path fileSource = c.contentFile;
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("file does not exists %s",fileSource));
            }
            if (Files.isDirectory(fileSource)) {
                Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                if (Files.exists(ext)) {
                    c.descriptor = NutsDescriptorParser.of(session).parse(ext).get(session);
                } else {
                    c.descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.contentFile, execOptions, session);
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = NutsPath.of(fileSource+ ".zip",session)
                                .toAbsolute().toFile();
                        ZipUtils.zip(session, fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = zipFilePath;
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid nuts folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.contentFile.getFileName().toString().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_NAME)) {
                    try (InputStream in = Files.newInputStream(c.contentFile)) {
                        c.descriptor = NutsDescriptorParser.of(session).parse(in).get(session);
                    }
                    c.contentFile = null;
                    if (c.streamOrPath.isPath() && c.streamOrPath.getPath().isURL()) {
                        URLBuilder ub = new URLBuilder(c.streamOrPath.getPath().toURL().toString());
                        try {
                            c.contentFile = CoreIOUtils.toPathInputSource(
                                    NutsStreamOrPath.of(
                                            ub.resolveSibling(session.locations().getDefaultIdFilename(c.descriptor.getId())).toURL(),session
                                    ),
                                    c.temps, true,session);
                        } catch (Exception ex) {
                            //TODO FIX ME
                            ex.printStackTrace();
                        }
                    }
                    if (c.contentFile == null) {
                        for (NutsIdLocation location0 : c.descriptor.getLocations()) {
                            if (CoreFilterUtils.acceptClassifier(location0, classifier)) {
                                String location = location0.getUrl();
                                if (NutsPath.of(location,session).isHttp()) {
                                    try {
                                        c.contentFile = CoreIOUtils.toPathInputSource(
                                                NutsStreamOrPath.of(new URL(location),session),
                                                c.temps, true,session);
                                    } catch (Exception ex) {

                                    }
                                } else {
                                    URLBuilder ub = new URLBuilder(c.streamOrPath.getPath().toURL().toString());
                                    try {
                                        c.contentFile = CoreIOUtils.toPathInputSource(
                                                NutsStreamOrPath.of(ub.resolveSibling(session.locations().getDefaultIdFilename(c.descriptor.getId())).toURL(),session),
                                                c.temps, true,session);
                                    } catch (Exception ex) {
                                        //TODO add log here
                                    }
                                }
                                if (c.contentFile == null) {
                                    break;
                                }
                            }
                        }
                    }
                    if (c.contentFile == null) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to locale package for %s" , c.streamOrPath));
                    }
                } else {
                    c.descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.contentFile, execOptions, session);
                    if (c.descriptor == null) {
                        CoreDigestHelper d = new CoreDigestHelper(session);
                        d.append(c.contentFile);
                        String artifactId = d.getDigest();
                        c.descriptor = new DefaultNutsDescriptorBuilder()
                                .setId("temp:"+artifactId+"#1.0")
                                .setPackaging(CoreIOUtils.getFileExtension(contentFile.getName()))
                                .build();
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("path does not denote a valid file or folder %s", c.streamOrPath));
            }
        } catch (IOException ex) {
            throw new NutsIOException(session,ex);
        }
        return c;
    }

    @Override
    public String toString() {
        return "nuts " + cmdName + " " + NutsCommandLine.of(args).toString();
    }

}
