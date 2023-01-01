package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.id.util.NIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

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
public class DefaultNDeployCommand extends AbstractNDeployCommand {

    public DefaultNDeployCommand(NSession ws) {
        super(ws);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NInputSource contentFile, NFetchCommand options, List<String> parseOptions, NSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        CharacterizedDeployFile c = new CharacterizedDeployFile(session);
        try {
            c.setBaseFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true, session));
            c.setContentStreamOrPath(contentFile);
            if (!Files.exists(c.getBaseFile())) {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("file does not exists %s", c.getBaseFile()));
            }
            if (c.getDescriptor() == null) {
                NInputSource p = c.getContentStreamOrPath();
                if(p instanceof NPath) {
                    NPath pp=(NPath) p;
                    try {
                        c.setDescriptor(NDescriptorParser.of(session).parse(
                                pp.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME)
                        ).get(session));
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            if (Files.isDirectory(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    Path ext = c.getBaseFile().resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.setDescriptor(NDescriptorParser.of(session).parse(ext).get(session));
                    } else {
                        c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions, session));
                    }
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = Paths.get(NPath.of(c.getBaseFile().toString() + ".zip", session).toAbsolute().toString());
                        ZipUtils.zip(session, c.getBaseFile().toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentStreamOrPath(NPath.of(zipFilePath, session));
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NIllegalArgumentException(session, NMsg.ofPlain("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    NPath ext = NPath.of(c.getBaseFile().toString() + "." + NConstants.Files.DESCRIPTOR_FILE_NAME, session)
                            .toAbsolute();
                    if (ext.exists()) {
                        c.setDescriptor(NDescriptorParser.of(session).parse(ext).get(session));
                    } else {
                        c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions, session));
                    }
                }
            } else {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("path does not denote a valid file or folder %s", c.getContentStreamOrPath()));
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return c;
    }

    @Override
    public NDeployCommand run() {
//        checkSession();
        checkSession();
//        NutsWorkspace ws = getSession().getWorkspace();
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NId nutsId : session.search().setSession(getSession())
                    .addIds(ids.toArray(new NId[0])).setLatest(true).setRepositoryFilter(fromRepository).getResultIds()) {
                NDefinition fetched = session.fetch().setContent(true).setId(nutsId).setSession(getSession()).getResultDefinition();
                if (fetched.getContent().isPresent()) {
                    runDeployFile(fetched.getContent().get(session), fetched.getDescriptor(), null);
                }
            }
        }
        NAssert.requireNonBlank(result, "package to deploy", session);
        if (getSession().isTrace()) {
            switch (getSession().getOutputFormat()) {
                case PLAIN: {
                    for (Result nid : result) {
                        getSession().getTerminal().out().resetLine().printf("%s deployed successfully as %s to %s%n",
                                nid.source,
                                nid.id,
                                NTexts.of(session).ofStyled(nid.repository, NTextStyle.primary3())
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

    private NDeployCommand runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NDeployCommand runDeployFile(NInputSource content, Object descriptor0, String descSHA1) {
        checkSession();
        NSession session = getSession();
        NWorkspaceExt dws = NWorkspaceExt.of(session.getWorkspace());
        NWorkspaceUtils wu = NWorkspaceUtils.of(this.session);
        wu.checkReadOnly();

        Path tempFile = null;
        NInputSource contentSource = NIO.of(session).createMultiRead(content);
        NDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            if (descriptor == null) {
                NFetchCommand p = this.session.fetch()
                        .setSession(session.copy().setTransitive(true));
                characterizedFile = characterizeForDeploy(contentSource, p, getParseOptions(), session);
                NAssert.requireNonBlank(characterizedFile.getDescriptor(), "descriptor", session);
                descriptor = characterizedFile.getDescriptor();
            }
            String name = this.session.locations().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = NPaths.of(session)
                    .createTempFile(name).toFile();
            NCp.of(this.session).setSession(session).from(contentSource.getInputStream()).to(tempFile).addOptions(NPathOption.SAFE).run();
            contentFile2 = tempFile;

            Path contentFile0 = contentFile2;
            String repository = this.getTargetRepository();

            wu.checkReadOnly();
            Path contentFile = contentFile0;
            Path tempFile2 = null;
            try {
                if (Files.isDirectory(contentFile)) {
                    Path descFile = contentFile.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
                    NDescriptor descriptor2;
                    if (Files.exists(descFile)) {
                        descriptor2 = NDescriptorParser.of(session).parse(descFile).get(session);
                    } else {
                        descriptor2 = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
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
                            Path zipFilePath = Paths.get(NPath.of(contentFile.toString() + ".zip", this.session)
                                    .toAbsolute().toString());
                            try {
                                ZipUtils.zip(session, contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                            } catch (IOException ex) {
                                throw new NIOException(session, ex);
                            }
                            contentFile = zipFilePath;
                            tempFile2 = contentFile;
                        } else {
                            throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("invalid nuts folder source; expected 'zip' ext in descriptor"));
                        }
                    }
                } else {
                    if (descriptor == null) {
                        descriptor = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile, getParseOptions(), session);
                    }
                }
                if (descriptor == null) {
                    throw new NNotFoundException(getSession(), null, NMsg.ofCstyle("artifact not found at %s", contentFile));
                }
                //remove workspace
                descriptor = descriptor.builder().setId(descriptor.getId().builder().setRepository(null).build()).build();
                if (NStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
                    throw new NIllegalArgumentException(getSession(), NMsg.ofCstyle("invalid version %s", descriptor.getId().getVersion()));
                }

                NId effId = dws.resolveEffectiveId(descriptor, session);
                CorePlatformUtils.checkAcceptCondition(descriptor.getCondition(), false, session);
                if (NBlankable.isBlank(repository)) {
                    effId = NIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor, session);
                    for (NRepository repo : wu.filterRepositoriesDeploy(effId, null)
                            .stream()
                            .filter(x -> x.config().getDeployWeight() > 0)
                            .sorted(Comparator.comparingInt(x -> x.config().getDeployWeight()))
                            .collect(Collectors.toList())) {
                        int deployOrder = repo.config().getDeployWeight();
                        NRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                .setSession(session)
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId, repo.getName(), NTexts.of(session).ofText(content));
                        return this;
                    }
                } else {
                    NRepository repo = getSession().repos().getRepository(repository);
                    if (repo == null) {
                        throw new NRepositoryNotFoundException(getSession(), repository);
                    }
                    if (!repo.config().isEnabled()) {
                        throw new NRepositoryDisabledException(getSession(), repository);
                    }
                    effId = NIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor, session);
                    NRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setSession(session)
                            .setId(effId)
                            .setContent(contentFile)
                            .setDescriptor(descriptor)
                            .run();
                    addResult(effId, repo.getName(), NTexts.of(this.session).ofText(content));
                    return this;
                }
                throw new NRepositoryNotFoundException(getSession(), repository);
            } finally {
                if (tempFile2 != null) {
                    try {
                        Files.delete(tempFile2);
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
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

    protected NDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        checkSession();
        NSession session = getSession();
        NDescriptor mdescriptor = null;
        if (descriptor instanceof NDescriptor) {
            mdescriptor = (NDescriptor) descriptor;
            if (descSHA1 != null && !NDigest.of(session).sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("invalid content Hash"));
            }
            return mdescriptor;
        } else {
            InputStream inputStream = (InputStream) descriptor;
            NInputSource nutsStreamOrPath = NIO.of(session).createInputSource(inputStream);
            if (nutsStreamOrPath != null) {
                NInputSource d = NIO.of(session).createMultiRead(nutsStreamOrPath);
                try {
                    if (descSHA1 != null) {
                        try (InputStream is = d.getInputStream()) {
                            if (!NDigest.of(session).sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                                throw new NIllegalArgumentException(getSession(), NMsg.ofPlain("invalid content Hash"));
                            }
                        } catch (IOException ex) {
                            throw new NIOException(session, ex);
                        }
                    }
                    try (InputStream is = d.getInputStream()) {
                        return NDescriptorParser.of(session).parse(is).get(session);
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
                    }
                } finally {
                    d.disposeMultiRead();
                }
            } else {
                throw new NException(getSession(), NMsg.ofCstyle("unexpected type %s", descriptor.getClass().getName()));
            }
        }
    }

    @Override
    public NDeployCommand addIds(String... values) {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.of(s).get(session));
                }
            }
        }
        return this;
    }

    @Override
    public NDeployCommand addIds(NId... value) {
        if (value != null) {
            for (NId s : value) {
                if (s != null) {
                    ids.add(s);
                }
            }
        }
        return this;
    }

    @Override
    public NDeployCommand clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NDeployCommand addId(NId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

}
