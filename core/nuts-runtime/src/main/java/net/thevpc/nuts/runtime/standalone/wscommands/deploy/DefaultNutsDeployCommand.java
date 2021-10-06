package net.thevpc.nuts.runtime.standalone.wscommands.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
import net.thevpc.nuts.runtime.bundles.io.NutsStreamOrPath;
import net.thevpc.nuts.runtime.bundles.io.ZipOptions;
import net.thevpc.nuts.runtime.bundles.io.ZipUtils;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsRepositorySPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsStreamOrPath contentFile, NutsFetchCommand options, String[] parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new String[0];
        }
        CharacterizedDeployFile c = new CharacterizedDeployFile();
        try {
            c.baseFile = CoreIOUtils.toPathInputSource(contentFile, c.temps, session);
            c.contentStreamOrPath = contentFile;
            if (!Files.exists(c.baseFile)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("file does not exists %s", c.baseFile));
            }
            if (c.descriptor == null && c.contentStreamOrPath.isPath() && c.contentStreamOrPath.getPath().isURL()) {
                try {
                    c.descriptor = session.descriptor().parser().setSession(session).parse(
                            c.contentStreamOrPath.getPath().resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                    );
                } catch (Exception ex) {
                    //ignore
                }
            }
            if (Files.isDirectory(c.baseFile)) {
                if (c.descriptor == null) {
                    Path ext = c.baseFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.descriptor = session.descriptor().parser().setSession(session).parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.baseFile, parseOptions, session);
                    }
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(session.io().path(c.baseFile.toString() + ".zip").builder().withAppBaseDir().build().toString());
                        ZipUtils.zip(session, c.baseFile.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentStreamOrPath = NutsStreamOrPath.of(NutsPath.of(zipFilePath,session));
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(c.baseFile)) {
                if (c.descriptor == null) {
                    File ext = new File(session.io().path(c.baseFile.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME).builder().withAppBaseDir().build().toString());
                    if (ext.exists()) {
                        c.descriptor = session.descriptor().parser().setSession(session).parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.baseFile, parseOptions, session);
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("path does not denote a valid file or folder %s", c.contentStreamOrPath));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    @Override
    public NutsDeployCommand run() {
        checkSession();
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : session.search().setSession(getSession())
                    .addIds(ids.toArray(new NutsId[0])).setLatest(true).setRepository(fromRepository).getResultIds()) {
                NutsDefinition fetched = session.fetch().setContent(true).setId(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getPath() != null) {
                    runDeployFile(NutsStreamOrPath.of(
                           NutsPath.of(fetched.getPath(),session)
                    ), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing package to deploy"));
        }
        if (getSession().isTrace()) {
            getSession().formats().object(result).println();
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(NutsStreamOrPath content, Object descriptor0, String descSHA1) {
        checkSession();
        NutsSession ws = getSession();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws.getWorkspace());
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        wu.checkReadOnly();

        Path tempFile = null;
        NutsStreamOrPath contentSource;
        contentSource = content.toMultiRead(session);
        NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            if (descriptor == null) {
                NutsFetchCommand p = session.fetch()
                        .setSession(ws.copy().setTransitive(true));
                characterizedFile = characterizeForDeploy(contentSource, p, getParseOptions(), ws);
                if (characterizedFile.descriptor == null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing descriptor"));
                }
                descriptor = characterizedFile.descriptor;
            }
            String name = session.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = Paths.get(session.io().tmp()
                    .setSession(session)
                    .createTempFile(name));
            session.io().copy().setSession(ws).from(contentSource.getInputStream()).to(tempFile).setSafe(true).run();
            contentFile2 = tempFile;

            Path contentFile0 = contentFile2;
            String repository = this.getTargetRepository();

            wu.checkReadOnly();
            Path contentFile = contentFile0;
            Path tempFile2 = null;
            try {
                if (Files.isDirectory(contentFile)) {
                    Path descFile = contentFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    NutsDescriptor descriptor2;
                    if (Files.exists(descFile)) {
                        descriptor2 = ws.descriptor().parser().setSession(session).parse(descFile);
                    } else {
                        descriptor2 = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                contentFile,
                                getParseOptions(), ws);
                    }
                    if (descriptor == null) {
                        descriptor = descriptor2;
                    } else {
                        if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                            ws.descriptor().formatter(descriptor).print(descFile);
                        }
                    }
                    if (descriptor != null) {
                        if ("zip".equals(descriptor.getPackaging())) {
                            Path zipFilePath = Paths.get(ws.io().path(contentFile.toString() + ".zip").builder().withAppBaseDir().build().toString());
                            try {
                                ZipUtils.zip(ws, contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                            } catch (IOException ex) {
                                throw new UncheckedIOException(ex);
                            }
                            contentFile = zipFilePath;
                            tempFile2 = contentFile;
                        } else {
                            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid nuts folder source; expected 'zip' ext in descriptor"));
                        }
                    }
                } else {
                    if (descriptor == null) {
                        descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                contentFile, getParseOptions(), ws);
                    }
                }
                if (descriptor == null) {
                    throw new NutsNotFoundException(getSession(), null, NutsMessage.cstyle("artifact not found at %s", contentFile));
                }
                //remove workspace
                descriptor = descriptor.builder().setId(descriptor.getId().builder().setRepository(null).build()).build();
                if (NutsUtilStrings.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid version %s", descriptor.getId().getVersion()));
                }

                NutsId effId = dws.resolveEffectiveId(descriptor, ws);
                CorePlatformUtils.checkSupportedSys(descriptor.getCondition(),false, ws);
                if (NutsBlankable.isBlank(repository)) {
                    NutsRepositoryFilter repositoryFilter = null;
                    //TODO CHECK ME, why offline
                    for (NutsRepository repo : wu.filterRepositoriesDeploy(effId, repositoryFilter)) {

                        effId = session.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor) //                                    .setAlternative(NutsUtilStrings.trim(descriptor.getAlternative()))
                        ;
                        NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(ws)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId);
                        return this;
                    }
                } else {
                    NutsRepository repo = getSession().repos().getRepository(repository);
                    if (repo == null) {
                        throw new NutsRepositoryNotFoundException(getSession(), repository);
                    }
                    if (!repo.config().isEnabled()) {
                        throw new NutsRepositoryNotFoundException(getSession(), "Repository " + repository + " is disabled.");
                    }
                    effId = session.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor) //                                .setAlternative(NutsUtilStrings.trim(descriptor.getAlternative()))
                    ;
                    NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setSession(ws)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .setId(effId)
                            .setContent(contentFile)
                            .setDescriptor(descriptor)
                            .run();
                    addResult(effId);
                    return this;
                }
                throw new NutsRepositoryNotFoundException(getSession(), repository);
            } finally {
                if (tempFile2 != null) {
                    try {
                        Files.delete(tempFile2);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                }
            }
        } finally {
            if (characterizedFile != null) {
                characterizedFile.close();
            }
            if (tempFile != null) {
                CoreIOUtils.delete(getSession(), tempFile);
            }
        }

    }

    protected NutsDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        checkSession();
        NutsSession ws = getSession();
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !ws.io().hash().sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
            }
            return mdescriptor;
        } else {
            NutsStreamOrPath nutsStreamOrPath = NutsStreamOrPath.ofAnyInputOrNull(descriptor, session);
            if(nutsStreamOrPath!=null) {
                NutsStreamOrPath d = nutsStreamOrPath.isInputStream()?
                        NutsStreamOrPath.of((InputStream) descriptor).toDisposable(session)
                        :nutsStreamOrPath
                        ;
                try {
                    if (descSHA1 != null) {
                        try (InputStream is = d.getInputStream()) {
                            if (!ws.io().hash().sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
                            }
                        } catch (IOException ex) {
                            throw new UncheckedIOException(ex);
                        }
                    }
                    try (InputStream is = d.getInputStream()) {
                        return ws.descriptor().parser().setSession(session).parse(is);
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                } finally {
                    d.dispose();
                }
            }else{
                throw new NutsException(getSession(), NutsMessage.cstyle("unexpected type %s", descriptor.getClass().getName()));
            }
        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NutsBlankable.isBlank(s)) {
                    ids.add(session.id().parser().parse(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand addIds(NutsId... value) {
        if (value != null) {
            for (NutsId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NutsDeployCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

}
