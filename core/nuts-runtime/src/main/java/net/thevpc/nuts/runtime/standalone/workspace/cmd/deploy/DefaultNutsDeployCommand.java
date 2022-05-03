package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NutsDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.id.util.NutsIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import net.thevpc.nuts.util.NutsUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * local implementation
 */
public class DefaultNutsDeployCommand extends AbstractNutsDeployCommand {

    public DefaultNutsDeployCommand(NutsWorkspace ws) {
        super(ws);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NutsInputSource contentFile, NutsFetchCommand options, List<String> parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        CharacterizedDeployFile c = new CharacterizedDeployFile(session);
        try {
            c.setBaseFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true, session));
            c.setContentStreamOrPath(contentFile);
            if (!Files.exists(c.getBaseFile())) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("file does not exists %s", c.getBaseFile()));
            }
            if (c.getDescriptor() == null) {
                NutsInputSource p = c.getContentStreamOrPath();
                if(p instanceof NutsPath) {
                    NutsPath pp=(NutsPath) p;
                    try {
                        c.setDescriptor(NutsDescriptorParser.of(session).parse(
                                pp.resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME)
                        ).get(session));
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            if (Files.isDirectory(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    Path ext = c.getBaseFile().resolve(NutsConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.setDescriptor(NutsDescriptorParser.of(session).parse(ext).get(session));
                    } else {
                        c.setDescriptor(NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions, session));
                    }
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = Paths.get(NutsPath.of(c.getBaseFile().toString() + ".zip", session).toAbsolute().toString());
                        ZipUtils.zip(session, c.getBaseFile().toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentStreamOrPath(NutsPath.of(zipFilePath, session));
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    NutsPath ext = NutsPath.of(c.getBaseFile().toString() + "." + NutsConstants.Files.DESCRIPTOR_FILE_NAME, session)
                            .toAbsolute();
                    if (ext.exists()) {
                        c.setDescriptor(NutsDescriptorParser.of(session).parse(ext).get(session));
                    } else {
                        c.setDescriptor(NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions, session));
                    }
                }
            } else {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("path does not denote a valid file or folder %s", c.getContentStreamOrPath()));
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return c;
    }

    @Override
    public NutsDeployCommand run() {
//        checkSession();
        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NutsId nutsId : session.search().setSession(getSession())
                    .addIds(ids.toArray(new NutsId[0])).setLatest(true).setRepositoryFilter(fromRepository).getResultIds()) {
                NutsDefinition fetched = session.fetch().setContent(true).setId(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getContent().isPresent()) {
                    runDeployFile(fetched.getContent().get(session), fetched.getDescriptor(), null);
                }
            }
        }
        NutsUtils.requireNonBlank(result,session, "package to deploy");
        if (getSession().isTrace()) {
            switch (getSession().getOutputFormat()) {
                case PLAIN: {
                    for (Result nid : result) {
                        getSession().getTerminal().out().resetLine().printf("%s deployed successfully as %s to %s%n",
                                nid.source,
                                nid.id,
                                NutsTexts.of(session).ofStyled(nid.repository, NutsTextStyle.primary3())
                        );
                    }
                    break;
                }
                default: {
                    getSession().out().printlnf(result);
                }
            }
        }
        return this;
    }

    private NutsDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NutsDeployCommand runDeployFile(NutsInputSource content, Object descriptor0, String descSHA1) {
        checkSession();
        NutsSession session = getSession();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(session.getWorkspace());
        NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(this.session);
        wu.checkReadOnly();

        Path tempFile = null;
        NutsInputSource contentSource = NutsIO.of(session).createMultiRead(content);
        NutsDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            if (descriptor == null) {
                NutsFetchCommand p = this.session.fetch()
                        .setSession(session.copy().setTransitive(true));
                characterizedFile = characterizeForDeploy(contentSource, p, getParseOptions(), session);
                NutsUtils.requireNonBlank(characterizedFile.getDescriptor(),session, "descriptor");
                descriptor = characterizedFile.getDescriptor();
            }
            String name = this.session.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = NutsPaths.of(this.session)
                    .createTempFile(name, session).toFile();
            NutsCp.of(this.session).setSession(session).from(contentSource.getInputStream()).to(tempFile).addOptions(NutsPathOption.SAFE).run();
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
                        descriptor2 = NutsDescriptorParser.of(session).parse(descFile).get(session);
                    } else {
                        descriptor2 = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile,
                                getParseOptions(), session);
                    }
                    if (descriptor == null) {
                        descriptor = descriptor2;
                    } else {
                        if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                            descriptor.formatter(session).print(descFile);
                        }
                    }
                    if (descriptor != null) {
                        if ("zip".equals(descriptor.getPackaging())) {
                            Path zipFilePath = Paths.get(NutsPath.of(contentFile.toString() + ".zip", this.session)
                                    .toAbsolute().toString());
                            try {
                                ZipUtils.zip(session, contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                            } catch (IOException ex) {
                                throw new NutsIOException(session, ex);
                            }
                            contentFile = zipFilePath;
                            tempFile2 = contentFile;
                        } else {
                            throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("invalid nuts folder source; expected 'zip' ext in descriptor"));
                        }
                    }
                } else {
                    if (descriptor == null) {
                        descriptor = NutsDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile, getParseOptions(), session);
                    }
                }
                if (descriptor == null) {
                    throw new NutsNotFoundException(getSession(), null, NutsMessage.ofCstyle("artifact not found at %s", contentFile));
                }
                //remove workspace
                descriptor = descriptor.builder().setId(descriptor.getId().builder().setRepository(null).build()).build();
                if (NutsStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofCstyle("invalid version %s", descriptor.getId().getVersion()));
                }

                NutsId effId = dws.resolveEffectiveId(descriptor, session);
                CorePlatformUtils.checkAcceptCondition(descriptor.getCondition(), false, session);
                if (NutsBlankable.isBlank(repository)) {
                    effId = NutsIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor, session);
                    for (NutsRepository repo : wu.filterRepositoriesDeploy(effId, null)
                            .stream()
                            .filter(x -> x.config().getDeployWeight() > 0)
                            .sorted(Comparator.comparingInt(x -> x.config().getDeployWeight()))
                            .collect(Collectors.toList())) {
                        int deployOrder = repo.config().getDeployWeight();
                        NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(session)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId, repo.getName(), NutsTexts.of(session).toText(content));
                        return this;
                    }
                } else {
                    NutsRepository repo = getSession().repos().getRepository(repository);
                    if (repo == null) {
                        throw new NutsRepositoryNotFoundException(getSession(), repository);
                    }
                    if (!repo.config().isEnabled()) {
                        throw new NutsRepositoryDisabledException(getSession(), repository);
                    }
                    effId = NutsIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor, session);
                    NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setSession(session)
                            .setId(effId)
                            .setContent(contentFile)
                            .setDescriptor(descriptor)
                            .run();
                    addResult(effId, repo.getName(), NutsTexts.of(this.session).toText(content));
                    return this;
                }
                throw new NutsRepositoryNotFoundException(getSession(), repository);
            } finally {
                if (tempFile2 != null) {
                    try {
                        Files.delete(tempFile2);
                    } catch (IOException ex) {
                        throw new NutsIOException(session, ex);
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
        NutsSession session = getSession();
        NutsDescriptor mdescriptor = null;
        if (descriptor instanceof NutsDescriptor) {
            mdescriptor = (NutsDescriptor) descriptor;
            if (descSHA1 != null && !NutsDigest.of(session).sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("invalid content Hash"));
            }
            return mdescriptor;
        } else {
            InputStream inputStream = (InputStream) descriptor;
            NutsInputSource nutsStreamOrPath = NutsIO.of(session).createInputSource(inputStream);
            if (nutsStreamOrPath != null) {
                NutsInputSource d = NutsIO.of(session).createMultiRead(nutsStreamOrPath);
                try {
                    if (descSHA1 != null) {
                        try (InputStream is = d.getInputStream()) {
                            if (!NutsDigest.of(session).sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                                throw new NutsIllegalArgumentException(getSession(), NutsMessage.ofPlain("invalid content Hash"));
                            }
                        } catch (IOException ex) {
                            throw new NutsIOException(session, ex);
                        }
                    }
                    try (InputStream is = d.getInputStream()) {
                        return NutsDescriptorParser.of(session).parse(is).get(session);
                    } catch (IOException ex) {
                        throw new NutsIOException(session, ex);
                    }
                } finally {
                    d.disposeMultiRead();
                }
            } else {
                throw new NutsException(getSession(), NutsMessage.ofCstyle("unexpected type %s", descriptor.getClass().getName()));
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
                    ids.add(NutsId.of(s).get(session));
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
