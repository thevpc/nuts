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
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CorePlatformUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.bundledlibs.io.ZipOptions;
import net.vpc.app.nuts.core.util.bundledlibs.io.ZipUtils;

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
    public NutsDeployCommand setDescriptorPath(String path) {
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
        CoreNutsUtils.checkReadOnly(ws);
        try {
            Path tempFile = null;
            Object content = this.getContent();
            CoreIOUtils.SourceItem contentSource;
            contentSource = CoreIOUtils.createSource(content).toMultiReadSourceItem();
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

                CoreNutsUtils.checkReadOnly(ws);
                Path contentFile = contentFile0;
                session = CoreNutsUtils.validateSession(session, ws);
                Path tempFile2 = null;
                NutsFetchCommand fetchOptions = ws.fetch().setTransitive(this.isTransitive());
                try {
                    if (Files.isDirectory(contentFile)) {
                        Path descFile = contentFile.resolve(NutsConstants.DESCRIPTOR_FILE_NAME);
                        NutsDescriptor descriptor2;
                        if (Files.exists(descFile)) {
                            descriptor2 = ws.parser().parseDescriptor(descFile);
                        } else {
                            descriptor2 = CoreIOUtils.resolveNutsDescriptorFromFileContent(ws, 
                                    CoreIOUtils.createSource(contentFile).toMultiReadSourceItem()
                                    , fetchOptions, session);
                        }
                        if (descriptor == null) {
                            descriptor = descriptor2;
                        } else {
                            if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                                ws.formatter().createDescriptorFormat().setPretty(true).format(descriptor, descFile);
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
                            descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(ws, CoreIOUtils.createSource(contentFile).toMultiReadSourceItem(), fetchOptions, session);
                        }
                    }
                    if (descriptor == null) {
                        throw new NutsNotFoundException(" at " + contentFile);
                    }
                    //remove workspace
                    descriptor = descriptor.setId(descriptor.getId().setNamespace(null));
                    if (CoreStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(NutsConstants.VERSION_CHECKED_OUT_EXTENSION)) {
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
                        for (NutsRepository repo : dws.getEnabledRepositories(NutsWorkspaceHelper.FilterMode.DEPLOY, effId, repositoryFilter, session, NutsFetchMode.LOCAL, fetchOptions)) {
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
            CoreIOUtils.SourceItem inputStreamSource = CoreIOUtils.createSource(descriptor);
            if (getDescSHA1() != null) {
                inputStreamSource = inputStreamSource.toMultiReadSourceItem();
                if (!CoreSecurityUtils.evalSHA1(inputStreamSource.open(), true).equals(getDescSHA1())) {
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

}
