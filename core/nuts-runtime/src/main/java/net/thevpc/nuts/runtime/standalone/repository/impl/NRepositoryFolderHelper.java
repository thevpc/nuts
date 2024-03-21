/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.event.DefaultNContentEvent;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultWriteTypeProcessor;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIterator;
import net.thevpc.nuts.runtime.standalone.repository.NIdPathIteratorBase;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.fetch.DefaultNFetchContentRepositoryCommand;
import net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy.DefaultNRepositoryUndeployCommand;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.CharacterizedExecFile;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommand;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.security.NDigest;
import net.thevpc.nuts.spi.NDeployRepositoryCommand;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.spi.NRepositoryUndeployCommand;
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
    private final NSession session;
    private final NPath rootPath;
    private final boolean cacheFolder;
    private NLog LOG;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;
    private final String kind;
    private final NObjectElement extraInfoElements;

    public NRepositoryFolderHelper(NRepository repo, NSession session, NPath rootPath, boolean cacheFolder, String kind, NObjectElement extraInfoElements) {
        this.repo = repo;
        this.kind = kind;
        this.session = session;
        this.extraInfoElements = extraInfoElements;
        if (session == null && repo == null) {
            throw new IllegalArgumentException("both workspace and repository are null");
        }
        this.rootPath = rootPath;
        this.cacheFolder = cacheFolder;
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

    public NPath getLongIdLocalFolder(NId id, NSession session) {
        CoreNIdUtils.checkLongId(id, session);
        if (repo == null) {
            return getStoreLocation().resolve(NLocations.of(session).getDefaultIdBasedir(id));
        }
        return getStoreLocation().resolve(NRepositoryExt0.of(repo).getIdBasedir(id, session));
    }

    public NPath getLongIdLocalFile(NId id, NSession session) {
        if (repo == null) {
            return getLongIdLocalFolder(id, session).resolve(NLocations.of(session).getDefaultIdFilename(id));
        }
        return getLongIdLocalFolder(id, session).resolve(NRepositoryExt0.of(repo).getIdFilename(id, session));
    }

    public NPath getShortIdLocalFolder(NId id, NSession session) {
        CoreNIdUtils.checkShortId(id, session);
        if (repo == null) {
            return getStoreLocation().resolve(NLocations.of(session).getDefaultIdBasedir(id.builder().setVersion("").build()));
        }
        return getStoreLocation().resolve(NRepositoryExt0.of(repo).getIdBasedir(id.builder().setVersion("").build(), session));
    }

    public NPath fetchContentImpl(NId id, NSession session) {
        NPath cacheContent = getLongIdLocalFile(id.builder().setFaceContent().build(), session);
        if (cacheContent != null && pathExists(cacheContent, session)) {
            return cacheContent.setUserCache(cacheFolder).setUserTemporary(false);
        }
        return null;
    }

    public NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }


    protected String getIdFilename(NId id, NSession session) {
        if (repo == null) {
            return NLocations.of(session).getDefaultIdFilename(id);
        }
        return NRepositoryExt0.of(repo).getIdFilename(id, session);
    }

    public NPath getGoodPath(NId id, NSession session) {
        String idFilename = getIdFilename(id, session);
        NPath versionFolder = getLongIdLocalFolder(id, session);
        return versionFolder.resolve(idFilename);
    }

    public NDescriptor fetchDescriptorImpl(NId id, NSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id.builder().setFaceDescriptor().build(), session);
        NPath goodFile = null;
        NPath versionFolder = getLongIdLocalFolder(id, session);
        goodFile = versionFolder.resolve(idFilename);
        if (pathExists(goodFile, session)) {
            return NDescriptorParser.of(session).parse(goodFile).get(session);
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

    protected NDescriptor loadMatchingDescriptor(NPath file, NId id, NSession session) {
        if (pathExists(file, session)) {
            NDescriptor d = file.isRegularFile() ? NDescriptorParser.of(session).parse(file).get(session) : null;
            if (d != null) {
                Map<String, String> query = id.getProperties();
                String os = query.get(NConstants.IdProperties.OS);
                String arch = query.get(NConstants.IdProperties.ARCH);
                String dist = query.get(NConstants.IdProperties.OS_DIST);
                String platform = query.get(NConstants.IdProperties.PLATFORM);
                String de = query.get(NConstants.IdProperties.DESKTOP);
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, de, d.getCondition(), session)) {
                    return d;
                }
            }
        }
        return null;
    }

    public NPath getRelativeLocalGroupAndArtifactFile(NId id, NSession session) {
        CoreNIdUtils.checkShortId(id, session);
        return NPath.of(
                net.thevpc.nuts.util.NIdUtils.resolveIdPath(id.getShortId())
                , session);
    }

    public NPath getLocalGroupAndArtifactFile(NId id, NSession session) {
        CoreNIdUtils.checkShortId(id, session);
        return getStoreLocation().resolve(net.thevpc.nuts.util.NIdUtils.resolveIdPath(id.getShortId()));
    }

    public NIterator<NId> searchVersions(NId id, final NIdFilter filter, boolean deep, NSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String singleVersion =
                id.getVersion().isLatestVersion() ? null :
                        id.getVersion().isReleaseVersion() ? null :
                                id.getVersion().asSingleValue().orNull();
        if (singleVersion != null) {
            return IteratorBuilder.ofSupplier(
                    () -> {
                        if (NConstants.Versions.LATEST.equals(singleVersion) || NConstants.Versions.RELEASE.equals(singleVersion)) {
                            NId found = searchLatestVersion(id, filter, session);
                            return (found != null ? Arrays.asList(found).iterator() : Collections.emptyIterator());
                        }
                        NId id1 = id.builder().setVersion(singleVersion).setFaceDescriptor().build();
                        NPath localFile = getLongIdLocalFile(id1, session);
                        if (localFile != null && localFile.isRegularFile()) {
                            return Collections.singletonList(id.builder().setRepository(repo == null ? null : repo.getName()).build()).iterator();
                        }
                        return IteratorBuilder.emptyIterator();
                    },
                    e -> NElements.of(e)
                            .ofObject()
                            .set("type", "searchSingleVersion")
                            .set("repository", repo == null ? null : repo.getName())
                            .set("id", id.toString())
                            .set("root", getStoreLocation().toString())
                            .addAll(extraInfoElements)
                            .build(),
                    session).build();
        }
        NIdFilter filter2 = NIdFilters.of(session).all(filter,
                NIdFilters.of(session).byName(id.getShortName())
        );
        return findInFolder(getRelativeLocalGroupAndArtifactFile(id, session), filter2,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public NIterator<NId> searchImpl(NIdFilter filter, NSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        return findInFolder(null, filter, Integer.MAX_VALUE, session);
    }

    public NIterator<NId> findInFolder(NPath folder, final NIdFilter filter, int maxDepth, NSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        return new NIdPathIterator(repo, rootPath, folder, filter, session, new NIdPathIteratorBase() {
            @Override
            public void undeploy(NId id, NSession session) throws NExecutionException {
                if (repo == null) {
                    NRepositoryFolderHelper.this.undeploy(new DefaultNRepositoryUndeployCommand(session.getWorkspace())
                            .setFetchMode(NFetchMode.LOCAL)
                            .setId(id).setSession(session));
                } else {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
                    repoSPI.undeploy().setId(id).setSession(session)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .run();
                }
            }

            @Override
            public boolean isDescFile(NPath pathname) {
                return pathname.getName().endsWith(NConstants.Files.DESCRIPTOR_FILE_EXTENSION);
            }

            @Override
            public NDescriptor parseDescriptor(NPath pathname, InputStream in, NFetchMode fetchMode, NRepository repository, NSession session, NPath rootURL) throws IOException {
                if (cacheFolder && CoreIOUtils.isObsoletePath(session, pathname)) {
                    //this is invalid cache!
                    return null;
                } else {
                    return NDescriptorParser.of(session).parse(pathname).get(session);
                }
            }
        }, maxDepth, kind, extraInfoElements, true
        );
    }

    public NPath getStoreLocation() {
        return rootPath;
    }

    public NId searchLatestVersion(NId id, NIdFilter filter, NSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        NId bestId = null;
        NPath file = getLocalGroupAndArtifactFile(id, session);
        if (file.exists()) {
            NPath[] versionFolders = file.stream().filter(NPath::isDirectory, "idDirectory").toArray(NPath[]::new);
            if (versionFolders != null) {
                for (NPath versionFolder : versionFolders) {
                    if (pathExists(versionFolder, session)) {
                        NId id2 = id.builder().setVersion(versionFolder.getName()).build();
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            if (filter == null || filter.acceptId(id2, session)) {
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

    public NDescriptor deploy(NDeployRepositoryCommand deployment, NConfirmationMode writeType) {
        NSession session = deployment.getSession();
        if (!isWriteEnabled()) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("read-only repository"));
        }
        NDescriptor descriptor = deployment.getDescriptor();
        NId id = deployment.getId();
        if (id == null) {
            id = descriptor.getId();
        }
        CoreNIdUtils.checkLongId(id, session);
        NInputSource inputSource = null;
        if (deployment.getContent() == null) {
            if (!NDescriptorUtils.isNoContent(descriptor)) {
                NAssert.requireNonNull(deployment.getContent(), () -> NMsg.ofC("invalid deployment; missing content for %s", deployment.getId()), session);
            }
        } else {
            inputSource = NIO.of(session).ofMultiRead(deployment.getContent());
            inputSource.getMetaData().setKind("package content");
            if (descriptor == null) {
                try (final CharacterizedExecFile c = DefaultNExecCommand.characterizeForExec(inputSource, session, null)) {
//                    NutsUtils.requireNonNull(c.getDescriptor(),session,s->NMsg.ofC("invalid deployment; missing descriptor for %s", deployment.getContent()));
                    if (c.getDescriptor() == null) {
                        throw new NNotFoundException(session, null,
                                NMsg.ofC("unable to resolve a valid descriptor for %s", deployment.getContent()), null);
                    }
                    descriptor = c.getDescriptor();
                }
            }
        }

        if (isDeployed(id, descriptor, session)) {
            NId finalId = id;
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask(NMsg.ofC("override deployment for %s?", id))
                    .withLog(_LOG(session), NMsg.ofC("nuts deployment overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(session, finalId))
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

        deployDescriptor(id, descriptor, writeType, session);
        NPath pckFile = inputSource == null ? null : deployContent(id, inputSource, descriptor, writeType, session);
        if (repo != null) {
            NRepositoryHelper.of(repo).events().fireOnDeploy(new DefaultNContentEvent(
                    pckFile, deployment, session, repo));
        }
        return descriptor.builder().setId(id.getLongId()).build();
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(DefaultNFetchContentRepositoryCommand.class, session);
        }
        return LOG;
    }

    public NPath deployDescriptor(NId id, NDescriptor desc, NConfirmationMode writeType, NSession session) {
        if (!isWriteEnabled()) {
            throw new NIllegalArgumentException(session, NMsg.ofPlain("read only repository"));
        }
        CoreNIdUtils.checkLongId(id, session);
        NPath descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build(), session);
        if (descFile.exists()) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask(NMsg.ofC("override descriptor file for %s?", id))
                    .withLog(_LOG(session), NMsg.ofC("nuts descriptor file overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(session, id))
                    .process()) {
                return descFile;
            }
        }
        return NLocks.of(session).setSource(descFile).call(() -> {

            desc.formatter(session).setNtf(false).print(descFile);
            byte[] bytes = NDigest.of(session).sha1().setSource(desc).computeString().getBytes();
            NCp.of(session)
                    .from(NIO.of(session).ofInputSource(
                                    new ByteArrayInputStream(bytes)
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

    public boolean isDeployed(NId id, NDescriptor descriptor, NSession session) {
        NPath pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build(), session);
        if (!pckFile.exists() || (cacheFolder && CoreIOUtils.isObsoletePath(session, pckFile))) {
            return false;
        }
        NPath descFile = getLongIdLocalFile(id.builder().setFaceDescriptor().build(), session);
        return descFile.exists() && (!cacheFolder || !CoreIOUtils.isObsoletePath(session, descFile));
    }

    public NPath deployContent(NId id, NInputSource content, NDescriptor descriptor, NConfirmationMode writeType, NSession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        CoreNIdUtils.checkLongId(id, session);
        NPath pckFile = getLongIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build(), session);
        if (pckFile.exists()) {
            if (content instanceof NPath) {
                if (((NPath) content).equals(pckFile)) {
                    //do nothing
                    return pckFile;
                }
            }
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask(NMsg.ofC("override content file for %s?", id))
                    .withLog(_LOG(session), NMsg.ofC("nuts content file overridden %s", id))
                    .onError(() -> new NAlreadyDeployedException(session, id))
                    .process()) {
                return pckFile;
            }
        }
        return NLocks.of(session).setSource(pckFile).call(() -> {
            NCp.of(session).from(content)
                    .to(pckFile).addOptions(NPathOption.SAFE).run();
            NCp.of(session).from(
                    CoreIOUtils.createBytesStream(NDigestUtils.evalSHA1Hex(pckFile, session).getBytes(),
                            NMsg.ofC("sha1://%s", id),
                            CoreIOUtils.MIME_TYPE_SHA1,
                            StandardCharsets.UTF_8.name(), null,
                            session
                    )
            ).to(pckFile.resolveSibling(pckFile.getName() + ".sha1")).addOptions(NPathOption.SAFE).run();
            return pckFile;
        });
    }

    public boolean undeploy(NRepositoryUndeployCommand command) {
        if (!isWriteEnabled()) {
            return false;
        }
        NPath localFolder = getLongIdLocalFile(command.getId().builder().setFaceContent().build(), command.getSession());
        if (localFolder != null && localFolder.exists()) {
            if (NLocks.of(command.getSession()).setSource(localFolder).call(() -> {
                localFolder.deleteTree();
                return false;
            })) {
                if (repo != null) {
                    NRepositoryHelper.of(repo).events().fireOnUndeploy(new DefaultNContentEvent(
                            localFolder, command, command.getSession(), repo));
                    return true;
                }
            }
        }
        return true;
    }

    public void reindexFolder(NSession session) {
        reindexFolder(getStoreLocation(), session);
    }

    private boolean reindexFolder(NPath path, NSession session) {
        if (!isWriteEnabled()) {
            return false;
        }
        try {
            Files.walkFileTree(path.toPath().get(), new FileVisitor<Path>() {
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
                        p.println("#version=" + session.getWorkspace().getApiVersion());
                        for (String file : folders) {
                            p.println(file + "/");
                        }
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new NIOException(session, e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
        return true;
    }

    private boolean pathExists(NPath p, NSession session) {
        return p.exists() &&
                !(cacheFolder && CoreIOUtils.isObsoletePath(session, p));
    }
}
