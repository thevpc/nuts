/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt0;
import net.thevpc.nuts.runtime.standalone.util.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.terminals.DefaultWriteTypeProcessor;
import net.thevpc.nuts.runtime.standalone.wscommands.DefaultNutsArtifactPathExecutable;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.core.events.DefaultNutsContentEvent;
import net.thevpc.nuts.runtime.core.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.io.NamedByteArrayInputStream;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.bundles.io.FolderNutIdIterator;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;

import java.io.*;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author thevpc
 */
public class NutsRepositoryFolderHelper {
    private final NutsLogger LOG;

    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;
    private boolean cacheFolder;

    public NutsRepositoryFolderHelper(NutsRepository repo, NutsWorkspace ws, Path rootPath, boolean cacheFolder) {
        this.repo = repo;
        this.ws = ws != null ? ws : repo == null ? null : repo.getWorkspace();
        if (ws == null && repo == null) {
            throw new IllegalArgumentException("both workspace and repository are null");
        }
        this.rootPath = rootPath;
        this.cacheFolder = cacheFolder;
        LOG = this.ws.log().of(DefaultNutsFetchContentRepositoryCommand.class);
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

    public Path getLongNameIdLocalFolder(NutsId id) {
        CoreNutsUtils.checkId_GNV(id,getWorkspace());
        if (repo == null) {
            return getStoreLocation().resolve(getWorkspace().locations().getDefaultIdBasedir(id));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id));
    }

    public Path getLongNameIdLocalFile(NutsId id) {
        if (repo == null) {
            return getLongNameIdLocalFolder(id).resolve(getWorkspace().locations().getDefaultIdFilename(id));
        }
        return getLongNameIdLocalFolder(id).resolve(NutsRepositoryExt0.of(repo).getIdFilename(id));
    }

    public Path getShortNameIdLocalFolder(NutsId id) {
        CoreNutsUtils.checkId_GN(id,getWorkspace());
        if (repo == null) {
            return getStoreLocation().resolve(getWorkspace().locations().getDefaultIdBasedir(id.builder().setVersion("").build()));
        }
        return getStoreLocation().resolve(NutsRepositoryExt0.of(repo).getIdBasedir(id.builder().setVersion("").build()));
    }

    public NutsContent fetchContentImpl(NutsId id, String localPath, NutsSession session) {
        Path cacheContent = getLongNameIdLocalFile(id.builder().setFaceContent().build());
        if (cacheContent != null && pathExists(cacheContent, session)) {
            return new NutsDefaultContent(cacheContent.toString(), true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected String getIdFilename(NutsId id) {
        if (repo == null) {
            return ws.locations().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt0.of(repo).getIdFilename(id);
    }

    public Path getGoodPath(NutsId id) {
        String idFilename = getIdFilename(id);
        Path versionFolder = getLongNameIdLocalFolder(id);
        return versionFolder.resolve(idFilename);
    }

    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id.builder().setFaceDescriptor().build());
        Path goodFile = null;
        Path versionFolder = getLongNameIdLocalFolder(id);
        goodFile = versionFolder.resolve(idFilename);
        if (pathExists(goodFile, session)) {
            return getWorkspace().descriptor().parser().parse(goodFile);
        }
//        String alt = id.getAlternative();
//        String goodAlt = null;
//        if (CoreNutsUtils.isDefaultAlternative(alt)) {
//            goodFile = versionFolder.resolve(idFilename);
//            if (Files.exists(goodFile)) {
//                return getWorkspace().descriptor().parse(goodFile);
//            }
//        } else if (!CoreStringUtils.isBlank(alt)) {
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
//                    throw new UncheckedIOException(ex);
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

    protected NutsDescriptor loadMatchingDescriptor(Path file, NutsId id, NutsSession session) {
        if (pathExists(file, session)) {
            NutsDescriptor d = Files.isRegularFile(file) ? getWorkspace().descriptor().parser().parse(file) : null;
            if (d != null) {
                Map<String, String> query = id.getProperties();
                String os = query.get("os");
                String arch = query.get("arch");
                String dist = query.get("dist");
                String platform = query.get("platform");
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, d, session)) {
                    return d;
                }
            }
        }
        return null;
    }

    public Path getLocalGroupAndArtifactFile(NutsId id) {
        NutsWorkspaceUtils.of(getWorkspace()).checkSimpleNameNutsId(id);
        Path groupFolder = getStoreLocation().resolve(id.getGroupId().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getArtifactId());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.builder().setFaceDescriptor().build();
            Path localFile = getLongNameIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.builder().setNamespace(repo == null ? null : repo.getName()).build()).iterator();
            }
            return null;
        }
        NutsIdFilter filter2 = ws.id().filter().all(filter,
                ws.id().filter().byName(id.getShortName())
        );
        return findInFolder(getLocalGroupAndArtifactFile(id), filter2,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> searchImpl(NutsIdFilter filter, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        return findInFolder(null, filter, Integer.MAX_VALUE, session);
    }

    public Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (folder != null) {
            folder = rootPath.resolve(folder);
        } else {
            folder = rootPath;
        }
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                if (repo == null) {
                    NutsRepositoryFolderHelper.this.undeploy(new DefaultNutsRepositoryUndeployCommand(ws)
                            .setFetchMode(NutsFetchMode.LOCAL)
                            .setId(id).setSession(session));
                } else {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(ws).repoSPI(repo);
                    repoSPI.undeploy().setId(id).setSession(session)
                            //.setFetchMode(NutsFetchMode.LOCAL)
                            .run();
                }
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsSession session) throws IOException {
                if (cacheFolder && CoreIOUtils.isObsoletePath(session, pathname)) {
                    //this is invalid cache!
                    return null;
                } else {
                    return getWorkspace().descriptor().parser().parse(pathname);
                }
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        if (!isReadEnabled()) {
            return null;
        }
        NutsId bestId = null;
        File file = getLocalGroupAndArtifactFile(id).toFile();
        if (file.exists()) {
            File[] versionFolders = file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return pathname.isDirectory();
                }
            });
            if (versionFolders != null) {
                for (File versionFolder : versionFolders) {
                    if (pathExists(versionFolder.toPath(), session)) {
                        NutsId id2 = id.builder().setVersion(versionFolder.getName()).build();
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            bestId = id2;
                        }
                    } else {
                        CoreIOUtils.delete(session,versionFolder.toPath());
                    }
                }
            }
        }
        return bestId;
    }

    public NutsDescriptor deploy(NutsDeployRepositoryCommand deployment, NutsConfirmationMode writeType) {
        if (!isWriteEnabled()) {
            throw new NutsIllegalArgumentException(getWorkspace(),"read-only repository");
        }
        if (deployment.getContent() == null) {
            throw new NutsIllegalArgumentException(getWorkspace(),"invalid deployment; missing content for "+deployment.getId());
        }
        NutsDescriptor descriptor = deployment.getDescriptor();
        NutsInput inputSource = ws.io().input().setTypeName("artifact content").setMultiRead(true).of(deployment.getContent());
        if (descriptor == null) {
            try (final DefaultNutsArtifactPathExecutable.CharacterizedExecFile c = DefaultNutsArtifactPathExecutable.characterizeForExec(inputSource,
                    deployment.getSession(), null)) {
                if (c.descriptor == null) {
                    throw new NutsNotFoundException(ws, "", "unable to resolve a valid descriptor for " + deployment.getContent(), null);
                }
                descriptor = c.descriptor;
            }
        }
        NutsId id = deployment.getId();
        if (id == null) {
            id = descriptor.getId();
        }

        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);

        if (isDeployed(id, descriptor, deployment.getSession())) {
            NutsId finalId = id;
            if (!DefaultWriteTypeProcessor
                    .of(writeType, deployment.getSession())
                    .ask("override deployment for %s?", id)
                    .withLog(LOG, "nuts deployment overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, finalId.toString()))
                    .process()) {
                return descriptor;
            }
        }
        switch (writeType) {
            case ERROR:
            case ASK: {
                writeType = NutsConfirmationMode.NO;
                break;
            }
        }

        deployDescriptor(id, descriptor, writeType, deployment.getSession());
        Path pckFile = deployContent(id, inputSource, descriptor, writeType, deployment.getSession());
        if (repo != null) {
            NutsRepositoryUtils.of(repo).events().fireOnDeploy(new DefaultNutsContentEvent(pckFile.toString(), deployment, deployment.getSession(), repo));
        }
        return descriptor.builder().setId(id.getLongNameId()).build();
    }

    public Path deployDescriptor(NutsId id, NutsDescriptor desc, NutsConfirmationMode writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            throw new IllegalArgumentException("read only repository");
        }
        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);
        Path descFile = getLongNameIdLocalFile(id.builder().setFaceDescriptor().build());
        if (Files.exists(descFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("override descriptor file for %s?", id)
                    .withLog(LOG, "nuts descriptor file overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, id.toString()))
                    .process()) {
                return descFile;
            }
        }
        return ws.concurrent().lock().source(descFile).call(() -> {

            getWorkspace().descriptor().formatter(desc).setSession(session).print(descFile);
            getWorkspace().io().copy().setSession(session)
                    .from(
                            getWorkspace().io().input().setName("sha1(" + desc.getId() + ")")
                                    .setTypeName("descriptor hash")
                                    .of(getWorkspace().io().hash().sha1().source(desc).setSession(session).computeString().getBytes())
                    ).to(descFile.resolveSibling(descFile.getFileName() + ".sha1")).setSafe(true).run();
            return descFile;
        });
    }

    public boolean isDeployed(NutsId id, NutsDescriptor descriptor, NutsSession session) {
        Path pckFile = getLongNameIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (!Files.exists(pckFile) || (cacheFolder && CoreIOUtils.isObsoletePath(session, pckFile))) {
            return false;
        }
        Path descFile = getLongNameIdLocalFile(id.builder().setFaceDescriptor().build());
        if (!Files.exists(descFile) || (cacheFolder && CoreIOUtils.isObsoletePath(session, descFile))) {
            return false;
        }
        return true;
    }

    public Path deployContent(NutsId id, Object content, NutsDescriptor descriptor, NutsConfirmationMode writeType, NutsSession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        NutsWorkspaceUtils.of(getWorkspace()).checkNutsId(id);
        Path pckFile = getLongNameIdLocalFile(id.builder().setFaceContent().setPackaging(descriptor.getPackaging()).build());
        if (Files.exists(pckFile)) {
            if (!DefaultWriteTypeProcessor
                    .of(writeType, session)
                    .ask("override content file for %s?", id)
                    .withLog(LOG, "nuts content file overridden {0}", id)
                    .onError(() -> new NutsAlreadyDeployedException(ws, id.toString()))
                    .process()) {
                return pckFile;
            }
        }
        return ws.concurrent().lock().source(pckFile).call(() -> {
            getWorkspace().io().copy().setSession(session).from(content).to(pckFile).setSafe(true).run();
            getWorkspace().io().copy().setSession(session).from(new NamedByteArrayInputStream(
                            CoreIOUtils.evalSHA1Hex(pckFile).getBytes(),
                            "sha1(" + id + ")"
                    )
            ).to(pckFile.resolveSibling(pckFile.getFileName() + ".sha1")).setSafe(true).run();
            return pckFile;
        });
    }

    public boolean undeploy(NutsRepositoryUndeployCommand options) {
        if (!isWriteEnabled()) {
            return false;
        }
        Path localFolder = getLongNameIdLocalFile(options.getId().builder().setFaceContent().build());
        if (localFolder != null && Files.exists(localFolder)) {
            if (ws.concurrent().lock().source(localFolder).call(() -> {
                CoreIOUtils.delete(options.getSession(), localFolder);
                return false;
            })) {
                if (repo != null) {
                    NutsRepositoryUtils.of(repo).events().fireOnUndeploy(new DefaultNutsContentEvent(localFolder.toString(), options, options.getSession(), repo));
                    return true;
                }
            }
        }
        return true;
    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation());
    }

    private boolean reindexFolder(Path path) {
        if (!isWriteEnabled()) {
            return false;
        }
        try {
            Files.walkFileTree(path, new FileVisitor<Path>() {
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
                            //&& !DefaultNutsVersion.isBlank(child.getName())
                            if (!child.getName().startsWith(".")) {
                                if (child.isDirectory()) {
                                    folders.add(child.getName());
                                } else if (child.isFile()) {
                                    files.add(child.getName());
                                }
                            }
                        }
                    }
                    try (PrintStream p = new PrintStream(new File(folder, CoreNutsConstants.Files.DOT_FILES))) {
                        p.println("#version=" + ws.getApiVersion());
                        for (String file : folders) {
                            p.println(file + "/");
                        }
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new UncheckedIOException(e);
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        return true;
    }

    private boolean pathExists(Path p, NutsSession session) {
        return Files.exists(p) &&
                !(cacheFolder && CoreIOUtils.isObsoletePath(session, p));
    }
}
