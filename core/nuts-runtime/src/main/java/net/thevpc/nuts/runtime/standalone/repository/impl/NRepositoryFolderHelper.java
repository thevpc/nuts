/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;


import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.event.DefaultNContentEvent;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultWriteTypeProcessor;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNFetchContentRepositoryCmd;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.DefaultNRepositoryUndeployCmd;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.CharacterizedExecFile;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmd;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.io.NDigest;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NMsg;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

/**
 * @author thevpc
 */
public class NRepositoryFolderHelper {
    private final NRepository repo;
    private final NPath rootPath;
    private final boolean cacheFolder;
    private NLog LOG;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;
    private final String kind;
    private final NObjectElement extraInfoElements;
    private final NWorkspace workspace;

    public NRepositoryFolderHelper(NRepository repo, NWorkspace workspace, NPath rootPath, boolean cacheFolder, String kind, NObjectElement extraInfoElements) {
        this.repo = repo;
        this.workspace = workspace;
        this.kind = kind;
        this.extraInfoElements = extraInfoElements;
        if (workspace == null && repo == null) {
            throw new IllegalArgumentException("both workspace and repository are null");
        }

        this.rootPath = rootPath;
        this.cacheFolder = cacheFolder;
    }

    private NSession currentSession() {
        if(repo!=null) {
            return repo.getWorkspace().currentSession();
        }
        return workspace.currentSession();
    }

    public boolean isReadEnabled() {
        return readEnabled;
    }

    public void setReadEnabled(boolean readEnabled) {
        this.readEnabled = readEnabled;
    }

    public boolean isWriteEnabled() {
        return writeEnabled;
    }

    public void setWriteEnabled(boolean writeEnabled) {
        this.writeEnabled = writeEnabled;
    }

    public NPath getLongIdLocalFolder(NId id) {
        CoreNIdUtils.checkLongId(id);
        if (repo == null) {
            return getStoreLocation().resolve(NWorkspace.of().getDefaultIdBasedir(id));
        }
        return getStoreLocation().resolve(NRepositoryExt0.of(repo).getIdBasedir(id));
    }

    public NPath getLongIdLocalFile(NId id) {
        if (repo == null) {
            return getLongIdLocalFolder(id).resolve(NWorkspace.of().getDefaultIdFilename(id));
        }
        return getLongIdLocalFolder(id).resolve(NRepositoryExt0.of(repo).getIdFilename(id));
    }

    public NPath getShortIdLocalFolder(NId id) {
        CoreNIdUtils.checkShortId(id);
        if (repo == null) {
            return getStoreLocation().resolve(NWorkspace.of().getDefaultIdBasedir(id.builder().setVersion("").build()));
        }
        return getStoreLocation().resolve(NRepositoryExt0.of(repo).getIdBasedir(id.builder().setVersion("").build()));
    }

    public NPath fetchContentImpl(NId id) {
        NPath cacheContent = getLongIdLocalFile(id.builder().setFaceContent().build());
        if (cacheContent != null && pathExists(cacheContent)) {
            return cacheContent.setUserCache(cacheFolder).setUserTemporary(false);
        }
        return null;
    }

    public NWorkspace getWorkspace() {
        return repo.getWorkspace();
    }


    protected String getIdFilename(NId id) {
        if (repo == null) {
            return NWorkspace.of().getDefaultIdFilename(id);
        }
        return NRepositoryExt0.of(repo).getIdFilename(id);
    }

    public NPath getGoodPath(NId id) {
        String idFilename = getIdFilename(id);
        NPath versionFolder = getLongIdLocalFolder(id);
        return versionFolder.resolve(idFilename);
    }

    public NDescriptor fetchDescriptorImpl(NId id) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id.builder().setFaceDescriptor().build());
        NPath goodFile = null;
        NPath versionFolder = getLongIdLocalFolder(id);
        goodFile = versionFolder.resolve(idFilename);
        if (pathExists(goodFile)) {
            return NDescriptorParser.of().parse(goodFile).get();
        }
//        String alt = id.getAlternative();
//        String goodAlt = null;
//        if (CoreNutsUtils.isDefaultAlternative(alt)) {
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile);
//            }
//        } else if (!NutsBlankable.isBlank(alt)) {
//            goodAlt = alt.trim();
//            goodFile = versionFolder.resolve(goodAlt).resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile).setAlternative(goodAlt);
//            }
//        } else {
//            //should test all files
//            NutsDescriptor best = null;
//            if (Files.isDirectory(versionFolder)) {
//                try (final DirectoryStream<Path> subFolders = Files.newDirectoryStream(versionFolder)) {
//                    for (Path subFolder : subFolders) {
//                        if (Files.isDirectory(subFolder)) {
//                            NutsDescriptor choice = null;
//                            try {
//                                choice = loadMatchingDescriptor(subFolder.resolve(idFilename), id, session).setAlternative(subFolder.getFileName().toString());
//                            } catch (Exception ex) {
//                                //
//                            }
//                            if (choice != null) {
//                                if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, choice) < 0) {
//                                    best = choice;
//                                }
//                            }
//                        }
//                    }
//                } catch (IOException ex) {
//                    throw new NutsIOException(session,ex);
//                }
//            }
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                NutsDescriptor c = null;
//                try {
//                    c = getWorkspace().descriptor().parse(goodFile).setAlternative("");
//                } catch (Exception ex) {
//                    //
//                }
//                if (c != null) {
//                    if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, c) < 0) {
//                        best = c;
//                    }
//                }
//            }
//            if (best != null) {
//                return best;
//            }
//        }
        return null;
    }

    protected NDescriptor loadMatchingDescriptor(NPath file, NId id) {
        if (pathExists(file)) {
            NDescriptor d = file.isRegularFile() ? NDescriptorParser.of().parse(file).get() : null;
            if (d != null) {
                Map<String, String> query = id.getProperties();
                String os = query.get(NConstants.IdProperties.OS);
                String arch = query.get(NConstants.IdProperties.ARCH);
                String dist = query.get(NConstants.IdProperties.OS_DIST);
                String platform = query.get(NConstants.IdProperties.PLATFORM);
                String de = query.get(NConstants.IdProperties.DESKTOP);
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, de, d.getCondition())) {
                    return d;
                }
            }
        }
        return null;
    }

    public NPath getRelativeLocalGroupAndArtifactFile(NId id) {
        CoreNIdUtils.checkShortId(id);
        return NPath.of(
                ExtraApiUtils.resolveIdPath(id.getShortId())
        );
    }

    public NPath getLocalGroupAndArtifactFile(NId id) {
        CoreNIdUtils.checkShortId(id);
        return getStoreLocation().resolve(ExtraApiUtils.resolveIdPath(id.getShortId()));
    }

    public NIterator<NId> searchVersions(NId id, final NIdFilter filter, boolean deep) {
        if (!isReadEnabled()) {
            return null;
        }
        String singleVersion =
                id.getVersion().isLatestVersion() ? null :
                        id.getVersion().isReleaseVersion() ? null :
                                id.getVersion().asSingleValue().orNull();
        if (singleVersion != null) {
            return NIteratorBuilder.ofSupplier(
                    () -> {
                        if (NConstants.Versions.LATEST.equals(singleVersion) || NConstants.Versions.RELEASE.equals(singleVersion)) {
                            NId found = searchLatestVersion(id, filter);
                            return (found != null ? Arrays.asList(found).iterator() : Collections.emptyIterator());
                        }
                        NId id1 = id.builder().setVersion(singleVersion).setFaceDescriptor().build();
                        NPath localFile = getLongIdLocalFile(id1);
                        if (localFile != null && localFile.isRegularFile()) {
                            return Collections.singletonList(id.builder().setRepository(repo == null ? null : repo.getName()).build()).iterator();
                        }
                        return NIteratorBuilder.emptyIterator();
                    },
                    () -> NElements.of()
                            .ofObjectBuilder()
                            .name("SearchSingleVersion")
                            .set("repository", repo == null ? null : repo.getName())
                            .set("id", id.toString())
                            .set("root", getStoreLocation().toString())
                            .addAll(extraInfoElements.children())
                            .build()
            ).build();
        }
        NIdFilter filter2 = NIdFilters.of().all(filter,
                NIdFilters.of().byName(id.getShortName())
        );
        return findInFolder(getRelativeLocalGroupAndArtifactFile(id), filter2,
                deep ? Integer.MAX_VALUE : 1
        );
    }

    public NIterator<NId> searchImpl(NIdFilter filter) {
        if (!isReadEnabled()) {
            return null;
        }
        return findInFolder(null, filter, Integer.MAX_VALUE);
    }

    public NIterator<NId> findInFolder(NPath folder, final NIdFilter filter, int maxDepth) {
        if (!isReadEnabled()) {
            return null;
        }
        return new NIdPathIterator(repo, rootPath, folder, filter, new NIdPathIteratorBase() {
            @Override
            public NWorkspace getWorkspace() {
                return repo.getWorkspace();
            }
            @Override
            public void undeploy(NId id) throws NExecutionException {
                if (repo == null) {
                    NRepositoryFolderHelper.this.undeploy(new DefaultNRepositoryUndeployCmd(workspace)
                            .setFetchMode(NFetchMode.LOCAL)
                            .setId(id));
                } else {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(workspace).repoSPI(repo);
                    repoSPI.undeploy().setId(id)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .run();
                }
            }

            @Override
            public boolean isDescFile(NPath pathname) {
                return pathname.getName().endsWith(NConstants.Files.DESCRIPTOR_FILE_EXTENSION);
            }

            @Override
            public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NPath rootURL)  {
                if (cacheFolder && CoreIOUtils.isObsoletePath(pathname)) {
                    //this is invalid cache!
                    return null;
                } else {
                    return NDescriptorParser.of().parse(pathname).get();
                }
            }
        }, maxDepth, kind, extraInfoElements, true
        );
    }

    public NPath getStoreLocation() {
        return rootPath;
    }

    public NId searchLatestVersion(NId id, NIdFilter filter) {
        if (!isReadEnabled()) {
            return null;
        }
        NId bestId = null;
        NPath file = getLocalGroupAndArtifactFile(id);
        if (file.exists()) {
            NPath[] versionFolders = file.stream().filter(NPath::isDirectory)
                    .withDesc(NEDesc.of("idDirectory"))
                    .toArray(NPath[]::new);
            if (versionFolders != null) {
                for (NPath versionFolder : versionFolders) {
                    if (pathExists(versionFolder)) {
                        NId id2 = id.builder().setVersion(versionFolder.getName()).build();
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            if (filter == null || filter.acceptId(id2)) {
                                bestId = id2;
                            }
                        }
                    } else {
                        versionFolder.deleteTree();
                    }
                }
            }
        }
        return bestId;
    }

    public NDescriptor deploy(NDeployRepositoryCmd deployment, NConfirmationMode writeType) {
        if (!isWriteEnabled()) {
            throw new NIllegalArgumentException(NMsg.ofPlain("read-only repository"));
        }
        NDescriptor descriptor = deployment.getDescriptor();
        NId id = deployment.getId();
        if (id == null) {
            id = descriptor.getId();
        }
        CoreNIdUtils.checkLongId(id);
        NInputSource inputSource = null;
        if (deployment.getContent() == null) {
            if (!descriptor.isNoContent()) {
                NAssert.requireNonNull(deployment.getContent(), () -> NMsg.ofC("invalid deployment; missing content for %s", deployment.getId()));
            }
        } else {
            inputSource = NInputSource.ofMultiRead(deployment.getContent());
            inputSource.getMetaData().setKind("package content");
            if (descriptor == null) {
                try (final CharacterizedExecFile c = DefaultNExecCmd.characterizeForExec(inputSource, null)) {
//                    NutsUtils.requireNonNull(c.getDescriptor(),session,s->NMsg.ofC("invalid deployment; missing descriptor for %s", deployment.getContent()));
                    if (c.getDescriptor() == null) {
                        throw new NNotFoundException(null,
                                NMsg.ofC("unable to resolve a valid descriptor for %s", deployment.getContent()), null);
                    }
                    descriptor = c.getDescriptor();
                }
            }
        }

        if (isDeployed(id, descriptor)) {
            NId finalId = id;
            if (!DefaultWriteTypeProcessor
                    .of(writeType)
                    .ask(NMsg.ofC("override deployment for %s?", id))
                    .withLog(_LOG(), NMsg.ofC("nuts deployment overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(finalId))
                    .process()) {
                return descriptor;
            }
        }
        switch (writeType) {
            case ERROR:
            case ASK: {
                writeType = NConfirmationMode.NO;
                break;
            }
        }

        deployDescriptor(id, descriptor, writeType);
        NPath pckFile = inputSource == null ? null : deployContent(id, inputSource, descriptor, writeType);
        if (repo != null) {
            NRepositoryHelper.of(repo).events().fireOnDeploy(new DefaultNContentEvent(
                    pckFile, deployment,  repo.getWorkspace().currentSession(), repo));
        }
        return descriptor.builder().setId(id.getLongId()).build();
    }

    protected NLog _LOG() {
        return NLog.of(DefaultNFetchContentRepositoryCmd.class);
    }

    public NPath deployDescriptor(NId id, NDescriptor desc, NConfirmationMode writeType) {
        if (!isWriteEnabled()) {
            throw new NIllegalArgumentException(NMsg.ofPlain("read only repository"));
        }
        CoreNIdUtils.checkLongId(id);
        NPath descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build());
        if (descFile.exists()) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType)
                    .ask(NMsg.ofC("override descriptor file for %s?", id))
                    .withLog(_LOG(), NMsg.ofC("nuts descriptor file overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(id))
                    .process()) {
                return descFile;
            }
        }
        return NLock.ofId(id).callWith(() -> {

            NDescriptorFormat.ofPlain(desc).print(descFile);
            byte[] bytes = NDigest.of().sha1().setSource(desc).computeString().getBytes();
            NCp.of()
                    .from(NInputSource.of(
                                    bytes
                                    , new DefaultNContentMetadata(
                                            NMsg.ofC("sha1://%s", desc.getId()),
                                            (long) bytes.length,
                                            CoreIOUtils.MIME_TYPE_SHA1,
                                            StandardCharsets.UTF_8.name(), "descriptor hash"
                                    )
                            )
                    ).to(descFile.resolveSibling(descFile.getName() + ".sha1")).addOptions(NPathOption.SAFE).run();
            return descFile;
        });
    }

    public boolean isDeployed(NId id, NDescriptor descriptor) {
        NPath pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (!pckFile.exists() || (cacheFolder && CoreIOUtils.isObsoletePath(pckFile))) {
            return false;
        }
        NPath descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build());
        return descFile.exists() && (!cacheFolder || !CoreIOUtils.isObsoletePath(descFile));
    }

    public NPath deployContent(NId id, NInputSource content, NDescriptor descriptor, NConfirmationMode writeType) {
        if (!isWriteEnabled()) {
            return null;
        }
        CoreNIdUtils.checkLongId(id);
        NPath pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (pckFile.exists()) {
            if (content instanceof NPath) {
                if (((NPath) content).equals(pckFile)) {
                    //do nothing
                    return pckFile;
                }
            }
            if (!DefaultWriteTypeProcessor
                    .of(writeType)
                    .ask(NMsg.ofC("override content file for %s?", id))
                    .withLog(_LOG(), NMsg.ofC("nuts content file overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(id))
                    .process()) {
                return pckFile;
            }
        }
        return NLock.ofId(id).callWith(() -> {
            NCp.of().from(content)
                    .to(pckFile).addOptions(NPathOption.SAFE).run();
            NCp.of().from(
                    CoreIOUtils.createBytesStream(NDigestUtils.evalSHA1Hex(pckFile).getBytes(),
                            NMsg.ofC("sha1://%s", id),
                            CoreIOUtils.MIME_TYPE_SHA1,
                            StandardCharsets.UTF_8.name(), null
                    )
            ).to(pckFile.resolveSibling(pckFile.getName() + ".sha1")).addOptions(NPathOption.SAFE).run();
            return pckFile;
        });
    }

    public boolean undeploy(NRepositoryUndeployCmd command) {
        if (!isWriteEnabled()) {
            return false;
        }
        NPath localFolder = getLongIdLocalFile(command.getId().builder().setFaceContent().build());
        if (localFolder != null && localFolder.exists()) {
            if (NLock.of(localFolder).callWith(() -> {
                localFolder.deleteTree();
                return false;
            })) {
                if (repo != null) {
                    NRepositoryHelper.of(repo).events().fireOnUndeploy(new DefaultNContentEvent(
                            localFolder, command, repo.getWorkspace().currentSession(), repo));
                    return true;
                }
            }
        }
        return true;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation());
    }

    private boolean reindexFolder(NPath path) {
        if (!isWriteEnabled()) {
            return false;
        }
        try {
            Path start = path.toPath().get();

            Files.walkFileTree(start, new FileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (dir.toString().startsWith(start.resolve(".git").toString() + "/")
                            || dir.toString().equals(start.resolve(".git").toString())
                    ) {
                        return FileVisitResult.CONTINUE;
                    }
                    File folder = dir.toFile();
                    File[] children = folder.listFiles();
                    TreeSet<String> files = new TreeSet<>();
                    TreeSet<String> folders = new TreeSet<>();
                    if (children != null) {
                        for (File child : children) {
                            //&& !DefaultNVersion.isBlank(child.getName())
                            if (!child.getName().startsWith(".")) {
                                if (child.isDirectory()) {
                                    folders.add(child.getName());
                                } else if (child.isFile()) {
                                    files.add(child.getName());
                                }
                            }
                        }
                    }
                    try (PrintStream p = new PrintStream(new File(folder, CoreNConstants.Files.DOT_FILES))) {
                        p.println("#version=" + workspace.getApiVersion());
                        for (String file : folders) {
                            p.println(file + "/");
                        }
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new NIOException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return true;
    }

    private boolean pathExists(NPath p) {
        return p.exists() &&
                !(cacheFolder && CoreIOUtils.isObsoletePath(p));
    }
}
