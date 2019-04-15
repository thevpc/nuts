package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.io.ZipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

public class DefaultNutsDeploymentBuilder implements NutsDeployCommand {

    private Object content;
    private Object descriptor;
    private String sha1;
    private String descSHA1;
    private String repository;
    private boolean trace = true;
    private boolean force = false;
    private boolean offline = false;
    private boolean transitive = true;
    private NutsWorkspace ws;
    private NutsSession session;
    private NutsResultFormatType formatType = NutsResultFormatType.PLAIN;

    public DefaultNutsDeploymentBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDeployCommand setContent(InputStream stream) {
        content = stream;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(String path) {
        content = path;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(File file) {
        content = file;
        return this;
    }

    @Override
    public NutsDeployCommand setContent(Path file) {
        content = file;
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(InputStream stream) {
        descriptor = stream;
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(String path) {
        descriptor = path;
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(File file) {
        descriptor = file;
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(URL url) {
        descriptor = url;
        return this;
    }

    public String getSha1() {
        return sha1;
    }

    @Override
    public NutsDeployCommand setSha1(String sha1) {
        this.sha1 = sha1;
        return this;
    }

    public String getDescSHA1() {
        return descSHA1;
    }

    @Override
    public NutsDeployCommand setDescSHA1(String descSHA1) {
        this.descSHA1 = descSHA1;
        return this;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeployCommand setContent(URL url) {
        content = url;
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        return this;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsDeployCommand setRepository(String repository) {
        this.repository = repository;
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsDeployCommand setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsDeployCommand setForce(boolean force) {
        this.force = force;
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsDeployCommand setOffline(boolean offline) {
        this.offline = offline;
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsDeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    public void setWs(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDeployCommand setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDeployCommand content(InputStream value) {
        return setContent(value);
    }

    @Override
    public NutsDeployCommand content(String path) {
        return setContent(path);
    }

    @Override
    public NutsDeployCommand content(File file) {
        return setContent(file);
    }

    @Override
    public NutsDeployCommand content(Path file) {
        return setContent(file);
    }

    @Override
    public NutsDeployCommand descriptor(InputStream stream) {
        return setDescriptor(stream);
    }

    @Override
    public NutsDeployCommand descriptor(Path path) {
        return setDescriptor(path);
    }

    @Override
    public NutsDeployCommand setDescriptor(Path path) {
        return setDescriptor(path);
    }

    @Override
    public NutsDeployCommand descriptor(String path) {
        return setDescriptor(path);
    }

    @Override
    public NutsDeployCommand descriptor(File file) {
        return setDescriptor(file);
    }

    @Override
    public NutsDeployCommand descriptor(URL url) {
        return setDescriptor(url);
    }

    @Override
    public NutsDeployCommand sha1(String sha1) {
        return setSha1(sha1);
    }

    @Override
    public NutsDeployCommand descSHA1(String descSHA1) {
        return setDescSHA1(descSHA1);
    }

    @Override
    public NutsDeployCommand content(URL url) {
        return setContent(url);
    }

    @Override
    public NutsDeployCommand descriptor(NutsDescriptor descriptor) {
        return setDescriptor(descriptor);
    }

    @Override
    public NutsDeployCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsDeployCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsDeployCommand force() {
        return setForce(true);
    }

    @Override
    public NutsDeployCommand force(boolean force) {
        return setForce(force);
    }

    @Override
    public NutsDeployCommand offline() {
        return setOffline(true);
    }

    @Override
    public NutsDeployCommand offline(boolean offline) {
        return setOffline(offline);
    }

    @Override
    public NutsDeployCommand trace() {
        return setTrace(true);
    }

    @Override
    public NutsDeployCommand trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsDeployCommand transitive() {
        return setTransitive(true);
    }

    @Override
    public NutsDeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public NutsId deploy() {
//        DefaultNutsDeployment deployment = new DefaultNutsDeployment(ws);
//        deployment.setContent(content);
//        deployment.setDescriptor(descriptor);
//        deployment.setDescSHA1(descSHA1);
//        deployment.setRepository(repository);
//        deployment.setSha1(sha1);
//        deployment.setTrace(trace);
//        deployment.setForce(force);
//        deployment.setOffline(offline);
//        deployment.setTransitive(transitive);

        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsWorkspaceUtils.checkReadOnly(ws);
        try {
            Path tempFile = null;
            Object content = this.getContent();
            InputSource contentSource;
            contentSource = CoreIOUtils.createInputSource(content).multi();
            NutsDescriptor descriptor = buildDescriptor();

            CharacterizedFile characterizedFile = null;
            Path contentFile2 = null;
            try {
                if (descriptor == null) {
                    NutsFetchCommand p = ws.fetch().setTransitive(this.isTransitive()).setSession(session);
                    characterizedFile = CoreIOUtils.characterize(ws, contentSource, p, session);
                    if (characterizedFile.descriptor == null) {
                        throw new NutsIllegalArgumentException("Missing descriptor");
                    }
                    descriptor = characterizedFile.descriptor;
                }
                String name = ws.config().getDefaultIdFilename(descriptor.getId().setFaceDescriptor());
                tempFile = ws.io().createTempFile(name);
                ws.io().copy().from(contentSource.open()).to(tempFile).safeCopy().run();
                contentFile2 = tempFile;

                Path contentFile0 = contentFile2;
                String repository = this.getRepository();

                NutsWorkspaceUtils.checkReadOnly(ws);
                Path contentFile = contentFile0;
                session = NutsWorkspaceUtils.validateSession(ws, session);
                Path tempFile2 = null;
                NutsFetchCommand fetchOptions = ws.fetch().setTransitive(this.isTransitive());
                try {
                    if (Files.isDirectory(contentFile)) {
                        Path descFile = contentFile.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                        NutsDescriptor descriptor2;
                        if (Files.exists(descFile)) {
                            descriptor2 = ws.parser().parseDescriptor(descFile);
                        } else {
                            descriptor2 = CoreIOUtils.resolveNutsDescriptorFromFileContent(ws,
                                    CoreIOUtils.createInputSource(contentFile).multi(),
                                    fetchOptions, session);
                        }
                        if (descriptor == null) {
                            descriptor = descriptor2;
                        } else {
                            if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                                ws.formatter().createDescriptorFormat().setPretty(true).print(descriptor, descFile);
                            }
                        }
                        if (descriptor != null) {
                            if ("zip".equals(descriptor.getPackaging())) {
                                Path zipFilePath = ws.io().path(ws.io().expandPath(contentFile.toString() + ".zip"));
                                try {
                                    ZipUtils.zip(contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                                } catch (IOException ex) {
                                    throw new UncheckedIOException(ex);
                                }
                                contentFile = zipFilePath;
                                tempFile2 = contentFile;
                            } else {
                                throw new NutsIllegalArgumentException("Invalid Nut Folder source. expected 'zip' ext in descriptor");
                            }
                        }
                    } else {
                        if (descriptor == null) {
                            descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(ws, CoreIOUtils.createInputSource(contentFile).multi(), fetchOptions, session);
                        }
                    }
                    if (descriptor == null) {
                        throw new NutsNotFoundException(" at " + contentFile);
                    }
                    //remove workspace
                    descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
                    if (CoreStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                        throw new NutsIllegalArgumentException("Invalid Version " + descriptor.getId().getVersion());
                    }

                    NutsId effId = dws.resolveEffectiveId(descriptor, ws.fetch().setTransitive(true).session(session));
                    for (String os : descriptor.getOs()) {
                        CorePlatformUtils.checkSupportedOs(ws.parser().parseRequiredId(os).getSimpleName());
                    }
                    for (String arch : descriptor.getArch()) {
                        CorePlatformUtils.checkSupportedArch(ws.parser().parseRequiredId(arch).getSimpleName());
                    }
                    if (CoreStringUtils.isBlank(repository)) {
                        NutsRepositoryFilter repositoryFilter = null;
                        //TODO CHECK ME, why offline
                        for (NutsRepository repo : NutsWorkspaceUtils.filterRepositories(ws, NutsRepositorySupportedAction.FIND, effId, repositoryFilter, NutsFetchMode.LOCAL, fetchOptions)) {
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);

                            effId = ws.config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(CoreStringUtils.trim(descriptor.getAlternative()));
                            repo.deploy(
                                    new DefaultNutsRepositoryDeploymentOptions()
                                            .setForce(this.isForce())
                                            .setOffline(this.isOffline())
                                            .setTrace(this.isTrace())
                                            .setTransitive(this.isTransitive())
                                            .setId(effId).setContent(contentFile).setDescriptor(descriptor).setRepository(repository), rsession);
                            return effId;
                        }
                    } else {

                        NutsRepository repo = ws.config().getRepository(repository);
                        if (repo == null) {
                            throw new NutsRepositoryNotFoundException(repository);
                        }
                        if (!repo.config().isEnabled()) {
                            throw new NutsRepositoryNotFoundException("Repository " + repository + " is disabled.");
                        }
                        NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);
                        effId = ws.config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(CoreStringUtils.trim(descriptor.getAlternative()));
                        repo.deploy(new DefaultNutsRepositoryDeploymentOptions()
                                .setForce(this.isForce())
                                .setOffline(this.isOffline())
                                .setTrace(this.isTrace())
                                .setTransitive(this.isTransitive())
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor).setRepository(repository), rsession);
                        return effId;
                    }
                    throw new NutsRepositoryNotFoundException(repository);
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
                    CoreIOUtils.delete(tempFile);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

    }

    protected NutsDescriptor buildDescriptor() {
        if (descriptor == null) {
            return null;
        }
        NutsDescriptor mdescriptor = null;
        if (NutsDescriptor.class.isInstance(descriptor)) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (getDescSHA1() != null && !ws.io().getSHA1(mdescriptor).equals(getDescSHA1())) {
                throw new NutsIllegalArgumentException("Invalid Content Hash");
            }
            return mdescriptor;
        } else if (CoreIOUtils.isValidInputStreamSource(descriptor.getClass())) {
            InputSource inputStreamSource = CoreIOUtils.createInputSource(descriptor);
            if (getDescSHA1() != null) {
                inputStreamSource = inputStreamSource.multi();
                if (!CoreIOUtils.evalSHA1(inputStreamSource.open(), true).equalsIgnoreCase(getDescSHA1())) {
                    throw new NutsIllegalArgumentException("Invalid Content Hash");
                }
            }
            try (InputStream is = inputStreamSource.open()) {
                return ws.parser().parseDescriptor(is, true);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }

        } else {
            throw new NutsException("Unexpected type " + descriptor.getClass().getName());
        }
    }

    @Override
    public NutsDeployCommand formatType(NutsResultFormatType formatType) {
        return setFormatType(formatType);
    }

    @Override
    public NutsDeployCommand setFormatType(NutsResultFormatType formatType) {
        if (formatType == null) {
            formatType = NutsResultFormatType.PLAIN;
        }
        this.formatType = formatType;
        return this;
    }

    @Override
    public NutsDeployCommand json() {
        return setFormatType(NutsResultFormatType.JSON);
    }

    @Override
    public NutsDeployCommand plain() {
        return setFormatType(NutsResultFormatType.PLAIN);
    }

    @Override
    public NutsDeployCommand props() {
        return setFormatType(NutsResultFormatType.PROPS);
    }

    @Override
    public NutsResultFormatType getFormatType() {
        return this.formatType;
    }
}
