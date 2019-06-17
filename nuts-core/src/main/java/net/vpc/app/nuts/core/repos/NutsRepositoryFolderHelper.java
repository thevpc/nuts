/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import net.vpc.app.nuts.NutsAlreadyDeployedException;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsRepositoryUndeployCommand;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.FolderNutIdIterator;
import static net.vpc.app.nuts.core.repos.NutsFolderRepository.LOG;
import net.vpc.app.nuts.core.spi.NutsRepositoryExt;
import net.vpc.app.nuts.NutsDeployRepositoryCommand;
import net.vpc.app.nuts.NutsRepositoryUndeployCommand;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.DefaultNutsContentEvent;
import net.vpc.app.nuts.core.NutsPatternIdFilter;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public class NutsRepositoryFolderHelper {

    private NutsRepository repo;
    private NutsWorkspace ws;
    private Path rootPath;
    private boolean readEnabled = true;
    private boolean writeEnabled = true;

    public NutsRepositoryFolderHelper(NutsRepository repo, NutsWorkspace ws, Path rootPath) {
        this.repo = repo;
        this.ws = ws != null ? ws : repo == null ? null : repo.getWorkspace();
        this.rootPath = rootPath;
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

    public Path getIdLocalFile(NutsId id) {
        return getStoreLocation().resolve(NutsRepositoryExt.of(repo).getIdBasedir(id))
                .resolve(NutsRepositoryExt.of(repo).getIdFilename(id));
    }

    public Path getLocalVersionFolder(NutsId id) {
        return getStoreLocation().resolve(NutsRepositoryExt.of(repo).getIdBasedir(id.setVersion("")));
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        Path cacheContent = getIdLocalFile(id);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new DefaultNutsContent(cacheContent, true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    protected String getIdFilename(NutsId id) {
        if (repo == null) {
            return ws.config().getDefaultIdFilename(id);
        }
        return NutsRepositoryExt.of(repo).getIdFilename(id);
    }

    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        if (!isReadEnabled()) {
            return null;
        }
        String idFilename = getIdFilename(id);
        Path goodFile = null;
        String alt = id.getAlternative();
        String goodAlt = null;
        Path versionFolder = getLocalVersionFolder(id);
        if (NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE.equals(alt)) {
            goodFile = versionFolder.resolve(idFilename);
            if (Files.exists(goodFile)) {
                return getWorkspace().format().descriptor().read(goodFile);
            }
        } else if (!CoreStringUtils.isBlank(alt)) {
            goodAlt = alt.trim();
            goodFile = versionFolder.resolve(goodAlt).resolve(idFilename);
            if (Files.exists(goodFile)) {
                return getWorkspace().format().descriptor().read(goodFile).setAlternative(goodAlt);
            }
        } else {
            //should test all files
            NutsDescriptor best = null;
            if (Files.isDirectory(versionFolder)) {
                try (final DirectoryStream<Path> subFolders = Files.newDirectoryStream(versionFolder)) {
                    for (Path subFolder : subFolders) {
                        if (Files.isDirectory(subFolder)) {
                            NutsDescriptor choice = null;
                            try {
                                choice = loadMatchingDescriptor(subFolder.resolve(idFilename), id, session.getSession()).setAlternative(subFolder.getFileName().toString());
                            } catch (Exception ex) {
                                //
                            }
                            if (choice != null) {
                                if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, choice) < 0) {
                                    best = choice;
                                }
                            }
                        }
                    }
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
            goodFile = versionFolder.resolve(idFilename);
            if (Files.exists(goodFile)) {
                NutsDescriptor c = null;
                try {
                    c = getWorkspace().format().descriptor().read(goodFile).setAlternative("");
                } catch (Exception ex) {
                    //
                }
                if (c != null) {
                    if (best == null || CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(best, c) < 0) {
                        best = c;
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    protected NutsDescriptor loadMatchingDescriptor(Path file, NutsId id, NutsSession session) {
        if (Files.exists(file)) {
            NutsDescriptor d = Files.isRegularFile(file) ? getWorkspace().format().descriptor().read(file) : null;
            if (d != null) {
                Map<String, String> query = id.getQueryMap();
                String os = query.get("os");
                String arch = query.get("arch");
                String dist = query.get("dist");
                String platform = query.get("platform");
                if (CoreFilterUtils.matchesEnv(arch, os, dist, platform, d, ws, session)) {
                    return d;
                }
            }
        }
        return null;
    }

    public Path getLocalGroupAndArtifactFile(NutsId id) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            return null;
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            return null;
        }
        Path groupFolder = getStoreLocation().resolve(id.getGroup().replace('.', File.separatorChar));
        return groupFolder.resolve(id.getName());
    }

    public Iterator<NutsId> searchVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.setFaceDescriptor();
            Path localFile = getIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.setNamespace(repo == null ? null : repo.config().getName())).iterator();
            }
            return null;
        }
        NutsIdFilter filter2 = new NutsIdFilterAnd(filter,
                new NutsPatternIdFilter(id.getSimpleNameId())
        );
        return findInFolder(getLocalGroupAndArtifactFile(id), filter2,
                deep ? Integer.MAX_VALUE : 1,
                session);
    }

    public Iterator<NutsId> searchImpl(NutsIdFilter filter, NutsRepositorySession session) {
        if (!isReadEnabled()) {
            return null;
        }
        return findInFolder(null, filter, Integer.MAX_VALUE, session);
    }

    public Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, int maxDepth, NutsRepositorySession session) {
        if (!isReadEnabled()) {
            return null;
        }
        if (folder != null) {
            folder = rootPath.resolve(folder);
        }
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            //            return Collections.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                if (repo == null) {
                    NutsRepositoryFolderHelper.this.undeploy(new DefaultNutsRepositoryUndeployCommand(repo).id(id).setSession(session)
                    );
                } else {
                    repo.undeploy().id(id).session(session).run();
                }
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(NutsConstants.Files.DESCRIPTOR_FILE_EXTENSION);
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return getWorkspace().format().descriptor().read(pathname);
            }
        }, maxDepth);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
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
                    NutsId id2 = id.setVersion(versionFolder.getName());
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

    public boolean deploy(NutsDeployRepositoryCommand deployment) {
        if (!isWriteEnabled()) {
            return false;
        }
        NutsId id = deployment.getId();
        NutsWorkspaceUtils.checkNutsId(ws, id);
        deployDescriptor(id, deployment.getDescriptor(), deployment.getSession());
        Path pckFile = deployContent(id, deployment.getContent(), deployment.getSession());
        deployContent(id, deployment.getContent(), deployment.getSession());
        NutsRepositoryExt.of(repo).fireOnDeploy(new DefaultNutsContentEvent(pckFile, deployment, getWorkspace(), repo));
        return true;
    }

    public Path deployDescriptor(NutsId id, NutsDescriptor desc, NutsRepositorySession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        NutsWorkspaceUtils.checkNutsId(ws, id);
        Path descFile = getIdLocalFile(id.setFaceDescriptor());
        if (Files.exists(descFile) && !session.getSession().isForce()) {
            throw new NutsAlreadyDeployedException(ws, id.toString());
        }
        if (Files.exists(descFile)) {
            LOG.log(Level.FINE, "Nuts descriptor file Overridden {0}", descFile);
        }
        getWorkspace().format().descriptor().set(desc).print(descFile);
        getWorkspace().io().copy().from(new ByteArrayInputStream(getWorkspace().io().hash().sha1().source(desc).computeString().getBytes())).to(descFile.resolveSibling(descFile.getFileName() + ".sha1")).safeCopy().run();
        return descFile;
    }

    public Path deployContent(NutsId id, Object content, NutsRepositorySession session) {
        if (!isWriteEnabled()) {
            return null;
        }
        NutsWorkspaceUtils.checkNutsId(ws, id);
        Path pckFile = getIdLocalFile(id);
        if (Files.exists(pckFile) && !session.getSession().isForce()) {
            throw new NutsAlreadyDeployedException(ws, id.toString());
        }
        if (Files.exists(pckFile)) {
            LOG.log(Level.FINE, "Nuts component  file Overridden {0}", pckFile);
        }

        getWorkspace().io().copy().from(content).to(pckFile).safeCopy().run();
        getWorkspace().io().copy().from(new ByteArrayInputStream(CoreIOUtils.evalSHA1Hex(pckFile).getBytes())).to(pckFile.resolveSibling(pckFile.getFileName() + ".sha1")).safeCopy().run();
        return pckFile;
    }

    public boolean undeploy(NutsRepositoryUndeployCommand options) {
        if (!isWriteEnabled()) {
            return false;
        }
        Path localFolder = getIdLocalFile(options.getId());
        if (localFolder != null && Files.exists(localFolder)) {
            try {
                CoreIOUtils.delete(localFolder);
                return false;
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
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
                    try (PrintStream p = new PrintStream(new File(folder, ".files"))) {
                        for (String file : files) {
                            p.println(file);
                        }
                    } catch (FileNotFoundException e) {
                        throw new UncheckedIOException(e);
                    }
                    try (PrintStream p = new PrintStream(new File(folder, ".folders"))) {
                        for (String file : folders) {
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
}
