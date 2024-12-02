package net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;


import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.descriptor.parser.NDescriptorContentResolver;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.io.util.ZipOptions;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

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
public class DefaultNDeployCmd extends AbstractNDeployCmd {

    public DefaultNDeployCmd(NWorkspace workspace) {
        super(workspace);
    }

    private static CharacterizedDeployFile characterizeForDeploy(NInputSource contentFile, NFetchCmd options, List<String> parseOptions, NSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        CharacterizedDeployFile c = new CharacterizedDeployFile(session);
        try {
            c.setBaseFile(CoreIOUtils.toPathInputSource(contentFile, c.getTemps(), true));
            c.setContentStreamOrPath(contentFile);
            if (!Files.exists(c.getBaseFile())) {
                throw new NIllegalArgumentException(NMsg.ofC("file does not exists %s", c.getBaseFile()));
            }
            if (c.getDescriptor() == null) {
                NInputSource p = c.getContentStreamOrPath();
                if(p instanceof NPath) {
                    NPath pp=(NPath) p;
                    try {
                        c.setDescriptor(NDescriptorParser.of().parse(
                                pp.resolve(NConstants.Files.DESCRIPTOR_FILE_NAME)
                        ).get());
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            if (Files.isDirectory(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    Path ext = c.getBaseFile().resolve(NConstants.Files.DESCRIPTOR_FILE_NAME);
                    if (Files.exists(ext)) {
                        c.setDescriptor(NDescriptorParser.of().parse(ext).get());
                    } else {
                        c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions));
                    }
                }
                if (c.getDescriptor() != null) {
                    if ("zip".equals(c.getDescriptor().getPackaging())) {
                        Path zipFilePath = Paths.get(NPath.of(c.getBaseFile().toString() + ".zip").toAbsolute().toString());
                        ZipUtils.zip(c.getBaseFile().toString(), new ZipOptions(), zipFilePath.toString());
                        c.setContentStreamOrPath(NPath.of(zipFilePath));
                        c.addTemp(zipFilePath);
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofPlain("invalid Nut Folder source. expected 'zip' ext in descriptor"));
                    }
                }
            } else if (Files.isRegularFile(c.getBaseFile())) {
                if (c.getDescriptor() == null) {
                    NPath ext = NPath.of(c.getBaseFile().toString() + "." + NConstants.Files.DESCRIPTOR_FILE_NAME)
                            .toAbsolute();
                    if (ext.exists()) {
                        c.setDescriptor(NDescriptorParser.of().parse(ext).get());
                    } else {
                        c.setDescriptor(NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(c.getBaseFile(), parseOptions));
                    }
                }
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("path does not denote a valid file or folder %s", c.getContentStreamOrPath()));
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return c;
    }

    @Override
    public NDeployCmd run() {
//        checkSession();
        NSession session=workspace.currentSession();
//        NutsWorkspace ws = getSession().getWorkspace();
        if (getContent() != null || getDescriptor() != null || getSha1() != null || getDescSha1() != null) {
            runDeployFile();
        }
        if (ids.size() > 0) {
            for (NId nutsId : NSearchCmd.of()
                    .addIds(ids.toArray(new NId[0])).setLatest(true).setRepositoryFilter(fromRepository).getResultIds()) {
                NDefinition fetched = NFetchCmd.of(nutsId).setContent(true).getResultDefinition();
                if (fetched.getContent().isPresent()) {
                    runDeployFile(fetched.getContent().get(), fetched.getDescriptor(), null);
                }
            }
        }
        NAssert.requireNonBlank(result, "package to deploy");
        if (session.isTrace()) {
            switch (session.getOutputFormat().orDefault()) {
                case PLAIN: {
                    for (Result nid : result) {
                        session.out().resetLine().println(NMsg.ofC(
                                "%s deployed successfully as %s to %s",
                                nid.source,
                                nid.id,
                                NText.ofStyled(nid.repository, NTextStyle.primary3())
                        ));
                    }
                    break;
                }
                default: {
                    session.out().println(result);
                }
            }
        }
        return this;
    }

    private NDeployCmd runDeployFile() {
        return runDeployFile(getContent(), getDescriptor(), getDescSha1());
    }

    private NDeployCmd runDeployFile(NInputSource content, Object descriptor0, String descSHA1) {
        NSession session=workspace.currentSession();
        NWorkspaceExt dws = NWorkspaceExt.of();
        NWorkspaceUtils wu = NWorkspaceUtils.of(workspace);
        wu.checkReadOnly();

        Path tempFile = null;
        NInputSource contentSource = NInputSource.ofMultiRead(content);
        NDescriptor descriptor = buildDescriptor(descriptor0, descSHA1);

        CharacterizedDeployFile characterizedFile = null;
        Path contentFile2 = null;
        try {
            if (descriptor == null) {
                NFetchCmd p = NFetchCmd.of();
                characterizedFile = characterizeForDeploy(contentSource, p, getParseOptions(), session);
                NAssert.requireNonBlank(characterizedFile.getDescriptor(), "descriptor");
                descriptor = characterizedFile.getDescriptor();
            }
            String name = NWorkspace.of().getDefaultIdFilename(descriptor.getId().builder().setFaceDescriptor().build());
            tempFile = NPath.ofTempFile(name).toPath().get();
            NCp.of().from(contentSource.getInputStream()).to(tempFile).addOptions(NPathOption.SAFE).run();
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
                        descriptor2 = NDescriptorParser.of().parse(descFile).get();
                    } else {
                        descriptor2 = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile,
                                getParseOptions());
                    }
                    if (descriptor == null) {
                        descriptor = descriptor2;
                    } else {
                        if (descriptor2 != null && !descriptor2.equals(descriptor)) {
                            NDescriptorFormat.of(descriptor).print(descFile);
                        }
                    }
                    if (descriptor != null) {
                        if ("zip".equals(descriptor.getPackaging())) {
                            Path zipFilePath = Paths.get(NPath.of(contentFile.toString() + ".zip")
                                    .toAbsolute().toString());
                            try {
                                ZipUtils.zip(contentFile.toString(), new ZipOptions(), zipFilePath.toString());
                            } catch (IOException ex) {
                                throw new NIOException(ex);
                            }
                            contentFile = zipFilePath;
                            tempFile2 = contentFile;
                        } else {
                            throw new NIllegalArgumentException(NMsg.ofPlain("invalid nuts folder source; expected 'zip' ext in descriptor"));
                        }
                    }
                } else {
                    if (descriptor == null) {
                        descriptor = NDescriptorContentResolver.resolveNutsDescriptorFromFileContent(
                                contentFile, getParseOptions());
                    }
                }
                if (descriptor == null) {
                    throw new NNotFoundException(null, NMsg.ofC("artifact not found at %s", contentFile));
                }
                //remove workspace
                descriptor = descriptor.builder().setId(descriptor.getId().builder().setRepository(null).build()).build();
                if (NStringUtils.trim(descriptor.getId().getVersion().getValue()).endsWith(CoreNConstants.Versions.CHECKED_OUT_EXTENSION)) {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid version %s", descriptor.getId().getVersion()));
                }

                NId effId = dws.resolveEffectiveId(descriptor);
                CorePlatformUtils.checkAcceptCondition(descriptor.getCondition(), false, session);
                if (NBlankable.isBlank(repository)) {
                    effId = CoreNIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor);
                    for (NRepository repo : wu.filterRepositoriesDeploy(effId, null)
                            .stream()
                            .filter(x -> x.config().getDeployWeight() > 0)
                            .sorted(Comparator.comparingInt(x -> x.config().getDeployWeight()))
                            .collect(Collectors.toList())) {
                        int deployOrder = repo.config().getDeployWeight();
                        NRepositorySPI repoSPI = wu.repoSPI(repo);
                        repoSPI.deploy()
                                //.setFetchMode(NutsFetchMode.LOCAL)
                                .setId(effId).setContent(contentFile).setDescriptor(descriptor)
                                .run();
                        addResult(effId, repo.getName(), NText.of(content));
                        return this;
                    }
                } else {
                    NRepository repo = workspace.findRepository(repository).get();
                    if (!repo.isEnabled()) {
                        throw new NRepositoryDisabledException(repository);
                    }
                    effId = CoreNIdUtils.createContentFaceId(effId.builder().setPropertiesQuery("").build(), descriptor);
                    NRepositorySPI repoSPI = wu.repoSPI(repo);
                    repoSPI.deploy()
                            .setId(effId)
                            .setContent(contentFile)
                            .setDescriptor(descriptor)
                            .run();
                    addResult(effId, repo.getName(), NText.of(content));
                    return this;
                }
                throw new NRepositoryNotFoundException(repository);
            } finally {
                if (tempFile2 != null) {
                    try {
                        Files.delete(tempFile2);
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                }
            }
        } finally {
            if (characterizedFile != null) {
                characterizedFile.close();
            }
            if (tempFile != null) {
                NIOUtils.delete(tempFile);
            }
        }

    }

    protected NDescriptor buildDescriptor(Object descriptor, String descSHA1) {
        if (descriptor == null) {
            return null;
        }
        NDescriptor mdescriptor = null;
        if (descriptor instanceof NDescriptor) {
            mdescriptor = (NDescriptor) descriptor;
            if (descSHA1 != null && !NDigest.of().sha1().setSource(mdescriptor).computeString().equalsIgnoreCase(descSHA1)) {
                throw new NIllegalArgumentException(NMsg.ofPlain("invalid content Hash"));
            }
            return mdescriptor;
        } else {
            InputStream inputStream = (InputStream) descriptor;
            NInputSource nutsStreamOrPath = NInputSource.of(inputStream);
            if (nutsStreamOrPath != null) {
                NInputSource d = NInputSource.ofMultiRead(nutsStreamOrPath);
                try {
                    if (descSHA1 != null) {
                        try (InputStream is = d.getInputStream()) {
                            if (!NDigest.of().sha1().setSource(is).computeString().equalsIgnoreCase(descSHA1)) {
                                throw new NIllegalArgumentException(NMsg.ofPlain("invalid content Hash"));
                            }
                        } catch (IOException ex) {
                            throw new NIOException(ex);
                        }
                    }
                    try (InputStream is = d.getInputStream()) {
                        return NDescriptorParser.of().parse(is).get();
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                } finally {
                    d.dispose();
                }
            } else {
                throw new NException(NMsg.ofC("unexpected type %s", descriptor.getClass().getName()));
            }
        }
    }

    @Override
    public NDeployCmd addIds(String... values) {
        if (values != null) {
            for (String s : values) {
                if (!NBlankable.isBlank(s)) {
                    ids.add(NId.get(s).get());
                }
            }
        }
        return this;
    }

    @Override
    public NDeployCmd addIds(NId... value) {
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
    public NDeployCmd clearIds() {
        ids.clear();
        return this;
    }

    @Override
    public NDeployCmd addId(NId id) {
        if (id != null) {
            addId(id.toString());
        }
        return this;
    }

}
