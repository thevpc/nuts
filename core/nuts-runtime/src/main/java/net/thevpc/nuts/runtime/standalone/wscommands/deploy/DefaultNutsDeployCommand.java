package net.thevpc.nuts.runtime.standalone.wscommands.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.CorePlatformUtils;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsWorkspace ws, NutsInput contentFile, NutsFetchCommand options, String[] parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new String[0];
        }
        NutsWorkspaceUtils.checkSession(ws, session);
        CharacterizedDeployFile c = new CharacterizedDeployFile();
        try {
            c.baseFile = CoreIOUtils.toPathInputSource(contentFile, c.temps, session);
            c.contentFile = contentFile;
            Path fileSource = c.contentFile.getFilePath();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("file does not exists %s", fileSource));
            }
            if (c.descriptor == null && c.baseFile.isURL()) {
                try {
                    c.descriptor = ws.descriptor().parser().setSession(session).parse(ws.io().input().of(c.baseFile.getURL().toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME).open());
                } catch (Exception ex) {
                    //ignore
                }
            }
            if (Files.isDirectory(fileSource)) {
                if (c.descriptor == null) {
                    Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.descriptor = ws.descriptor().parser().setSession(session).parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, parseOptions, session);
                    }
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = Paths.get(ws.io().path(fileSource.toString() + ".zip").builder().withAppBaseDir().build().toString());
                        ZipUtils.zip(ws, fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = ws.io().input().setMultiRead(true).of(zipFilePath);
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.descriptor == null) {
                    File ext = new File(ws.io().path(fileSource.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME).builder().withAppBaseDir().build().toString());
                    if (ext.exists()) {
                        c.descriptor = ws.descriptor().parser().setSession(session).parse(ext);
                    } else {
                        c.descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(c.contentFile, parseOptions, session);
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("path does not denote a valid file or folder %s", c.contentFile));
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
            for (NutsId nutsId : ws.search().setSession(getSession())
                    .addIds(ids.toArray(new NutsId[0])).setLatest(true).setRepository(fromRepository).getResultIds()) {
                NutsDefinition fetched = ws.fetch().setContent(true).setId(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getPath() != null) {
                    runDeployFile(fetched.getPath(), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.formatted("missing package to deploy"));
        }
        if (getSession().isTrace()) {
            getSession().getWorkspace().formats().object(result).println();
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(Object content, Object descriptor0, String descSHA1) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
        wu.checkReadOnly();

        Path tempFile = null;
        NutsInput contentSource;
        contentSource = ws.io().input().setMultiRead(true).setTypeName("artifact binaries").of(content);
        NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            NutsSession validWorkspaceSession = getSession();
            if (descriptor == null) {
                NutsFetchCommand p = ws.fetch()
                        .setSession(validWorkspaceSession.copy().setTransitive(true));
                characterizedFile = characterizeForDeploy(ws, contentSource, p, getParseOptions(), validWorkspaceSession);
                if (characterizedFile.descriptor == null) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing descriptor"));
                }
                descriptor = characterizedFile.descriptor;
            }
            String name = ws.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = Paths.get(ws.io().tmp()
                    .setSession(session)
                    .createTempFile(name));
            ws.io().copy().setSession(validWorkspaceSession).from(contentSource.open()).to(tempFile).setSafe(true).run();
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
                                ws.io().input().setMultiRead(true).of(contentFile),
                                getParseOptions(), validWorkspaceSession);
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
                                ws.io().input().setMultiRead(true).of(contentFile), getParseOptions(), validWorkspaceSession);
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

                NutsId effId = dws.resolveEffectiveId(descriptor, validWorkspaceSession);
                CorePlatformUtils.checkSupportedSys(descriptor.getCondition(), validWorkspaceSession);
                if (NutsUtilStrings.isBlank(repository)) {
                    NutsRepositoryFilter repositoryFilter = null;
                    //TODO CHECK ME, why offline
                    for (NutsRepository repo : wu.filterRepositoriesDeploy(effId, repositoryFilter)) {

                        effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor) //                                    .setAlternative(NutsUtilStrings.trim(descriptor.getAlternative()))
                        ;
                        NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(validWorkspaceSession)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId);
                        return this;
                    }
                } else {
                    NutsRepository repo = getSession().getWorkspace().repos().getRepository(repository);
                    if (repo == null) {
                        throw new NutsRepositoryNotFoundException(getSession(), repository);
                    }
                    if (!repo.config().isEnabled()) {
                        throw new NutsRepositoryNotFoundException(getSession(), "Repository " + repository + " is disabled.");
                    }
                    effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor) //                                .setAlternative(NutsUtilStrings.trim(descriptor.getAlternative()))
                    ;
                    NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setSession(validWorkspaceSession)
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
        NutsWorkspace ws = getSession().getWorkspace();
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !ws.io().hash().sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
            }
            return mdescriptor;
        } else if (CoreIOUtils.isValidInputStreamSource(descriptor.getClass())) {
            NutsInput inputStreamSource = ws.io().input().setMultiRead(true).setTypeName("artifact descriptor").of(descriptor);
            if (descSHA1 != null) {
                inputStreamSource = ws.io().input().setMultiRead(true).of(inputStreamSource);
                try (InputStream is = inputStreamSource.open()) {
                    if (!ws.io().hash().sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content Hash"));
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            try (InputStream is = inputStreamSource.open()) {
                return ws.descriptor().parser().setSession(session).parse(is);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

        } else {
            throw new NutsException(getSession(), NutsMessage.cstyle("unexpected type %s", descriptor.getClass().getName()));
        }
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NutsUtilStrings.isBlank(s)) {
                    ids.add(ws.id().parser().parse(s));
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

    private static class CharacterizedDeployFile implements AutoCloseable {

        public NutsInput baseFile;
        public NutsInput contentFile;
        public List<Path> temps = new ArrayList<>();
        public NutsDescriptor descriptor;

        public Path getContentPath() {
            return (Path) contentFile.getSource();
        }

        public void addTemp(Path f) {
            temps.add(f);
        }

        @Override
        public void close() {
            for (Iterator<Path> it = temps.iterator(); it.hasNext(); ) {
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
