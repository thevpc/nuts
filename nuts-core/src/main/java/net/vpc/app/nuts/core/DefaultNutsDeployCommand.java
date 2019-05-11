package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.InputSource;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.core.util.io.ZipOptions;
import net.vpc.app.nuts.core.util.io.ZipUtils;

public class DefaultNutsDeployCommand extends NutsWorkspaceCommandBase<NutsDeployCommand> implements NutsDeployCommand {

    private List<NutsId> result;
    private Object content;
    private Object descriptor;
    private String sha1;
    private String descSha1;
    private String fromRepository;
    private String toRepository;
    private boolean offline = false;
    private boolean transitive = true;
    private final List<NutsId> ids = new ArrayList<>();

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
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
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setContent(Path file) {
        content = file;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(InputStream stream) {
        descriptor = stream;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(String path) {
        descriptor = path;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(File file) {
        descriptor = file;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand setDescriptor(URL url) {
        descriptor = url;
        invalidateResult();
        return this;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    @Override
    public NutsDeployCommand setSha1(String sha1) {
        this.sha1 = sha1;
        invalidateResult();
        return this;
    }

    public String getDescSha1() {
        return descSha1;
    }

    @Override
    public NutsDeployCommand setDescSha1(String descSha1) {
        this.descSha1 = descSha1;
        invalidateResult();
        return this;
    }

    public Object getContent() {
        return content;
    }

    @Override
    public NutsDeployCommand setContent(URL url) {
        content = url;
        invalidateResult();
        return this;
    }

    public Object getDescriptor() {
        return descriptor;
    }

    @Override
    public NutsDeployCommand setDescriptor(NutsDescriptor descriptor) {
        this.descriptor = descriptor;
        invalidateResult();
        return this;
    }

    @Override
    public String getTargetRepository() {
        return toRepository;
    }

    @Override
    public NutsDeployCommand to(String repository) {
        return targetRepository(repository);
    }

    @Override
    public NutsDeployCommand targetRepository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setRepository(String repository) {
        return setTargetRepository(repository);
    }

    @Override
    public NutsDeployCommand setTargetRepository(String repository) {
        this.toRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public NutsDeployCommand from(String repository) {
        return sourceRepository(repository);
    }

    @Override
    public NutsDeployCommand sourceRepository(String repository) {
        return setSourceRepository(repository);
    }

    @Override
    public NutsDeployCommand setSourceRepository(String repository) {
        this.fromRepository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isOffline() {
        return offline;
    }

    @Override
    public NutsDeployCommand setOffline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsDeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        invalidateResult();
        return this;
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
        this.descriptor = path;
        invalidateResult();
        return this;
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
    public NutsDeployCommand descSha1(String descSha1) {
        return setDescSha1(descSha1);
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
        return setTargetRepository(repository);
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
    public NutsDeployCommand transitive() {
        return setTransitive(true);
    }

    @Override
    public NutsDeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public NutsId[] getResult() {
        if (result == null) {
            run();
        }
        return result.toArray(new NutsId[0]);
    }

    @Override
    protected void invalidateResult() {
        result = null;
    }

    @Override
    public NutsDeployCommand run() {
        if (content != null || descriptor != null || sha1 != null || descSha1 != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : ws.find().setSession(getSession()).addIds(ids.toArray(new NutsId[0])).latestVersions().setRepository(fromRepository).getResultIds()) {
                NutsDefinition fetched = ws.fetch().id(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getPath() != null) {
                    runDeployFile(fetched.getPath(), fetched.getDescriptor(), null);
                }
            }
        }
        if (result == null || result.isEmpty()) {
            throw new NutsIllegalArgumentException("Missing component to Deploy");
        }
        if (getValidSession().isTrace()) {
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {

                if (getValidSession().getOutputFormat() != null && getValidSession().getOutputFormat() != NutsOutputFormat.PLAIN) {
                    switch (getValidSession().getOutputFormat()) {
                        case JSON: {
                            getValidSession().getTerminal().out().printf(ws.io().json().pretty().toJsonString(result));
                            break;
                        }
                        case PROPS: {
                            Map<String, String> props = new LinkedHashMap<>();
                            for (int i = 0; i < result.size(); i++) {
                                props.put(String.valueOf(i + 1), result.get(i).toString());
                            }
                            CoreIOUtils.storeProperties(props, getValidSession().getTerminal().out());
                            break;
                        }

                    }
                }
            }
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(Object content, Object descriptor0, String descSHA1) {
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsWorkspaceUtils.checkReadOnly(ws);
        try {
            Path tempFile = null;
            InputSource contentSource;
            contentSource = CoreIOUtils.createInputSource(content).multi();
            NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

            CharacterizedFile characterizedFile = null;
            Path contentFile2 = null;
            try {
                if (descriptor == null) {
                    NutsFetchCommand p = ws.fetch().setTransitive(this.isTransitive()).setSession(getValidSession());
                    characterizedFile = CoreIOUtils.characterize(ws, contentSource, p, getValidSession());
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
                String repository = this.getTargetRepository();

                NutsWorkspaceUtils.checkReadOnly(ws);
                Path contentFile = contentFile0;
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
                                    fetchOptions, getValidSession());
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
                            descriptor = CoreIOUtils.resolveNutsDescriptorFromFileContent(ws, CoreIOUtils.createInputSource(contentFile).multi(), fetchOptions, getValidSession());
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

                    NutsId effId = dws.resolveEffectiveId(descriptor, ws.fetch().setTransitive(true).session(getValidSession()));
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
                            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);

                            effId = ws.config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(CoreStringUtils.trim(descriptor.getAlternative()));
                            repo.deploy()
                                    .setOffline(this.isOffline())
                                    .setTransitive(this.isTransitive())
                                    .setSession(rsession)
                                    .setId(effId).setContent(contentFile).setDescriptor(descriptor).setRepository(repository)
                                    .run();
                            addResult(effId);
                            return this;
                        }
                    } else {

                        NutsRepository repo = ws.config().getRepository(repository);
                        if (repo == null) {
                            throw new NutsRepositoryNotFoundException(repository);
                        }
                        if (!repo.config().isEnabled()) {
                            throw new NutsRepositoryNotFoundException("Repository " + repository + " is disabled.");
                        }
                        NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(getValidSession(), repo, this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE, fetchOptions);
                        effId = ws.config().createComponentFaceId(effId.unsetQuery(), descriptor).setAlternative(CoreStringUtils.trim(descriptor.getAlternative()));
                        repo.deploy()
                                .setOffline(this.isOffline())
                                .setTransitive(this.isTransitive())
                                .setSession(rsession)
                                .setId(effId)
                                .setContent(contentFile)
                                .setDescriptor(descriptor)
                                .setRepository(repository)
                                .run();
                        addResult(effId);
                        return this;
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

    private void addResult(NutsId nid) {
        if (getValidSession().isTrace()) {
            if (result == null) {
                result = new ArrayList<>();
            }
            result.add(nid);
            if (getValidSession().getOutputFormat() == null || getValidSession().getOutputFormat() == NutsOutputFormat.PLAIN) {
                getValidSession().getTerminal().out().printf("Nuts %N deployed successfully to ==%s==%n", ws.formatter().createIdFormat().toString(nid), toRepository == null ? "<default-repo>" : toRepository);
            }
        }
    }

    protected NutsDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        NutsDescriptor mdescriptor = null;
        if (NutsDescriptor.class.isInstance(descriptor)) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !ws.io().hash().sha1().source(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException("Invalid Content Hash");
            }
            return mdescriptor;
        } else if (CoreIOUtils.isValidInputStreamSource(descriptor.getClass())) {
            InputSource inputStreamSource = CoreIOUtils.createInputSource(descriptor);
            if (descSHA1 != null) {
                inputStreamSource = inputStreamSource.multi();
                try (InputStream is = inputStreamSource.open()) {
                    if (!ws.io().hash().sha1().source(is).computeString().equalsIgnoreCase(descSHA1)) {
                        throw new NutsIllegalArgumentException("Invalid Content Hash");
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
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
    public NutsDeployCommand ids(String... values) {
        return addIds(values);
    }

    @Override
    public NutsDeployCommand addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!CoreStringUtils.isBlank(s)) {
                    ids.add(ws.parser().parseRequiredId(s));
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

    @Override
    public NutsDeployCommand removeId(NutsId id) {
        if (id != null) {
            removeId(id.toString());
        }
        return this;
    }

    @Override
    public NutsDeployCommand id(String id) {
        return addId(id);
    }

    @Override
    public NutsDeployCommand removeId(String id) {
        ids.remove(ws.parser().parseId(id));
        return this;
    }

    @Override
    public NutsDeployCommand addId(String id) {
        if (!CoreStringUtils.isBlank(id)) {
            ids.add(ws.parser().parseRequiredId(id));
        }
        return this;
    }

    @Override
    public NutsId[] getIds() {
        return this.ids.toArray(new NutsId[0]);
    }

    @Override
    public NutsDeployCommand parseOptions(String... args) {
        NutsCommandLine cmd = new NutsCommandLine(args);
        NutsCommandArg a;
        while ((a = cmd.next()) != null) {
            switch (a.strKey()) {
                case "-o":
                case "--offline": {
                    setOffline(a.getBooleanValue());
                    break;
                }
                case "-d":
                case "--desc": {
                    setDescriptor(cmd.getValueFor(a).getString());
                    break;
                }
                case "-s":
                case "--source":
                case "--from": {
                    from(cmd.getValueFor(a).getString());
                    break;
                }
                case "-r":
                case "--target":
                case "--to": {
                    to(cmd.getValueFor(a).getString());
                    break;
                }
                case "--desc-sha1": {
                    this.setDescSha1(cmd.getValueFor(a).getString());
                    break;
                }
                case "--desc-sha1-file": {
                    try {
                        this.setDescSha1(new String(Files.readAllBytes(Paths.get(cmd.getValueFor(a).getString()))));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    break;
                }
                case "--sha1": {
                    this.setSha1(cmd.getValueFor(a).getString());
                    break;
                }
                case "--sha1-file": {
                    try {
                        this.setSha1(new String(Files.readAllBytes(Paths.get(cmd.getValueFor(a).getString()))));
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                    break;
                }
                default: {
                    if (!super.parseOption(a, cmd)) {
                        if (a.isOption()) {
                            throw new NutsIllegalArgumentException("Unsupported option " + a);
                        } else {
                            String idOrPath = a.getString();
                            if (idOrPath.indexOf('/') >= 0 || idOrPath.indexOf('\\') >= 0) {
                                setContent(idOrPath);
                            } else {
                                id(idOrPath);
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

}
