package net.vpc.app.nuts.core.impl.def.wscommands;

import net.vpc.app.nuts.core.wscommands.AbstractNutsDeployCommand;
import net.vpc.app.nuts.core.CoreNutsConstants;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.createInputSource;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.resolveNutsDescriptorFromFileContent;
import static net.vpc.app.nuts.core.util.io.CoreIOUtils.toPathInputSource;
import net.vpc.app.nuts.core.util.io.ZipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsDeployCommand run() {
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : ws.search().session(getSession().copy().trace(false)).addIds(ids.toArray(new NutsId[0])).latest().setRepository(fromRepository).getResultIds()) {
                NutsDefinition fetched = ws.fetch().content().id(nutsId).session(getSession()).getResultDefinition();
                if (fetched.getPath() != null) {
                    runDeployFile(fetched.getPath(), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "Missing component to Deploy");
        }
        if (getValidSession().isTrace()) {
            ws.object().session(getValidSession()).value(result).println();
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(Object content, Object descriptor0, String descSHA1) {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsWorkspaceUtils.of(ws).checkReadOnly();
        try {
            Path tempFile = null;
            InputSource contentSource;
            contentSource = CoreIOUtils.createInputSource(content).multi();
            NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

            CharacterizedDeployFile characterizedFile = null;
            Path contentFile2 = null;
            try {
                if (descriptor == null) {
                    NutsFetchCommand p = ws.fetch().transitive().session(getValidSession());
                    characterizedFile = characterizeForDeploy(ws, contentSource, p, getValidSession());
                    if (characterizedFile.descriptor == null) {
                        throw new NutsIllegalArgumentException(ws, "Missing descriptor");
                    }
                    descriptor = characterizedFile.descriptor;
                }
                String name = ws.config().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
                tempFile = ws.io().createTempFile(name);
                ws.io().copy().session(getValidSession()).from(contentSource.open()).to(tempFile).safeCopy().run();
                contentFile2 = tempFile;

                Path contentFile0 = contentFile2;
                String repository = this.getTargetRepository();

                NutsWorkspaceUtils.of(ws).checkReadOnly();
                Path contentFile = contentFile0;
                Path tempFile2 = null;
                NutsFetchCommand fetchOptions = ws.fetch().transitive();
                try {
                    if (Files.isDirectory(contentFile)) {
                        Path descFile = contentFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                        NutsDescriptor descriptor2;
                        if (Files.exists(descFile)) {
                            descriptor2 = ws.descriptor().parse(descFile);
                        } else {
                            descriptor2 = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                    CoreIOUtils.createInputSource(contentFile).multi(),
                                    fetchOptions, getValidSession());
                        }
                        if (descriptor == null) {
                            descriptor = descriptor2;
                        } else {
                            if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                                ws.descriptor().value(descriptor).print(descFile);
                            }
                        }
                        if (descriptor != null) {
                            if ("zip".equals(descriptor.getPackaging())) {
                                Path zipFilePath = ws.io().path(ws.io().expandPath(contentFile.toString() + ".zip"));
                                try {
                                    ZipUtils.zip(ws,contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                                } catch (IOException ex) {
                                    throw new UncheckedIOException(ex);
                                }
                                contentFile = zipFilePath;
                                tempFile2 = contentFile;
                            } else {
                                throw new NutsIllegalArgumentException(ws, "Invalid Nut Folder source. expected 'zip' ext in descriptor");
                            }
                        }
                    } else {
                        if (descriptor == null) {
                            descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(
                                    CoreIOUtils.createInputSource(contentFile).multi(), fetchOptions, getValidSession());
                        }
                    }
                    if (descriptor == null) {
                        throw new NutsNotFoundException(ws, " at " + contentFile);
                    }
                    //remove workspace
                    descriptor = descriptor.builder().setId(descriptor.getId().builder().setNamespace(null).build()).build();
                    if (CoreStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                        throw new NutsIllegalArgumentException(ws, "Invalid Version " + descriptor.getId().getVersion());
                    }

                    NutsId effId = dws.resolveEffectiveId(descriptor, ws.fetch().transitive().session(getValidSession()));
                    for (String os : descriptor.getOs()) {
                        CorePlatformUtils.checkSupportedOs(ws.id().parseRequired(os).getShortName());
                    }
                    for (String arch : descriptor.getArch()) {
                        CorePlatformUtils.checkSupportedArch(ws.id().parseRequired(arch).getShortName());
                    }
                    if (CoreStringUtils.isBlank(repository)) {
                        NutsRepositoryFilter repositoryFilter = null;
                        //TODO CHECK ME, why offline
                        for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositories(NutsRepositorySupportedAction.DEPLOY, effId, repositoryFilter, NutsFetchMode.LOCAL, fetchOptions)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), repo, NutsFetchMode.LOCAL, fetchOptions);

                            effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor)
//                                    .setAlternative(CoreStringUtils.trim(descriptor.getAlternative()))
                            ;
                            repo.deploy()
                                    .setSession(rsession)
                                    .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                    .run();
                            addResult(effId);
                            return this;
                        }
                    } else {
                        NutsRepository repo = ws.config().getRepository(repository, true);
                        if (repo == null) {
                            throw new NutsRepositoryNotFoundException(ws, repository);
                        }
                        if (!repo.config().isEnabled()) {
                            throw new NutsRepositoryNotFoundException(ws, "Repository " + repository + " is disabled.");
                        }
                        NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), repo, NutsFetchMode.LOCAL, fetchOptions);
                        effId = ws.config().createContentFaceId(effId.builder().setProperties("").build(), descriptor)
//                                .setAlternative(CoreStringUtils.trim(descriptor.getAlternative()))
                        ;
                        repo.deploy()
                                .setSession(rsession)
                                .setId(effId)
                                .setContent(contentFile)
                                .setDescriptor(descriptor)
                                .run();
                        addResult(effId);
                        return this;
                    }
                    throw new NutsRepositoryNotFoundException(ws, repository);
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
                    CoreIOUtils.delete(ws,tempFile);
                }
            }

        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }

    protected NutsDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !ws.io().hash().sha1().source(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(ws, "Invalid Content Hash");
            }
            return mdescriptor;
        } else if (CoreIOUtils.isValidInputStreamSource(descriptor.getClass())) {
            InputSource inputStreamSource = CoreIOUtils.createInputSource(descriptor);
            if (descSHA1 != null) {
                inputStreamSource = inputStreamSource.multi();
                try (InputStream is = inputStreamSource.open()) {
                    if (!ws.io().hash().sha1().source(is).computeString().equalsIgnoreCase(descSHA1)) {
                        throw new NutsIllegalArgumentException(ws, "Invalid Content Hash");
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            try (InputStream is = inputStreamSource.open()) {
                return ws.descriptor().parse(is);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

        } else {
            throw new NutsException(ws, "Unexpected type " + descriptor.getClass().getName());
        }
    }

    @Override
    public NutsDeployCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.id().parseRequired(s));
                }
            }
        }
        return this;
    }

    @Override
    public NutsDeployCommand ids(NutsId... values) {
        return addIds(values);
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
    public NutsDeployCommand id(NutsId id) {
        return addId(id);
    }

    @Override
    public NutsDeployCommand addId(NutsId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsWorkspace ws, InputSource contentFile, NutsFetchCommand options, NutsSession session) {
        session = NutsWorkspaceUtils.of(ws).validateSession( session);
        CharacterizedDeployFile c = new CharacterizedDeployFile();
        try {
            c.baseFile = toPathInputSource(contentFile, c.temps, ws);
            c.contentFile = contentFile;
            Path fileSource = c.contentFile.getPath();
            if (!Files.exists(fileSource)) {
                throw new NutsIllegalArgumentException(ws, "File does not exists " + fileSource);
            }
            if (c.descriptor == null && c.baseFile.isURL()) {
                try {
                    c.descriptor = ws.descriptor().parse(CoreIOUtils.createInputSource(c.baseFile.getURL().toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME).open());
                } catch (Exception ex) {
                    //ignore
                }
            }
            if (Files.isDirectory(fileSource)) {
                if (c.descriptor == null) {
                    Path ext = fileSource.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.descriptor = ws.descriptor().parse(ext);
                    } else {
                        c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, options, session);
                    }
                }
                if (c.descriptor != null) {
                    if ("zip".equals(c.descriptor.getPackaging())) {
                        Path zipFilePath = ws.io().path(ws.io().expandPath(fileSource.toString() + ".zip"));
                        ZipUtils.zip(ws,fileSource.toString(), new ZipOptions(), zipFilePath.toString());
                        c.contentFile = createInputSource(zipFilePath).multi();
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(ws, "Invalid Nut Folder source. expected 'zip' ext in descriptor");
                    }
                }
            } else if (Files.isRegularFile(fileSource)) {
                if (c.descriptor == null) {
                    File ext = new File(ws.io().expandPath(fileSource.toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME));
                    if (ext.exists()) {
                        c.descriptor = ws.descriptor().parse(ext);
                    } else {
                        c.descriptor = resolveNutsDescriptorFromFileContent(c.contentFile, options, session);
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(ws, "Path does not denote a valid file or folder " + c.contentFile);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return c;
    }

    private static class CharacterizedDeployFile implements AutoCloseable {

        public InputSource baseFile;
        public InputSource contentFile;
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
