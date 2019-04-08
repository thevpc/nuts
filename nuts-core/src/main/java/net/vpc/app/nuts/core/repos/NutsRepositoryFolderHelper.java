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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.logging.Level;
import net.vpc.app.nuts.NutsAlreadyDeployedException;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsContentEvent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsRepositoryDeploymentOptions;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;
import static net.vpc.app.nuts.core.repos.NutsFolderRepository.log;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.CoreSecurityUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.FolderNutIdIterator;

/**
 *
 * @author vpc
 */
public class NutsRepositoryFolderHelper {

    private AbstractNutsRepository repo;
    private NutsWorkspace workspace;
    private Path rootPath;

    public NutsRepositoryFolderHelper(AbstractNutsRepository repo, Path rootPath) {
        this.repo = repo;
        this.workspace = repo.getWorkspace();
        this.rootPath = rootPath;
    }

    public Path getIdLocalFile(NutsId id) {
        if (CoreStringUtils.isBlank(id.getGroup())) {
            return null;
        }
        if (CoreStringUtils.isBlank(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        String alt = CoreStringUtils.trim(id.getAlternative());
        String defaultIdFilename = repo.getIdFilename(id);
        return (alt.isEmpty() || alt.equals(NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE)) ? getLocalVersionFolder(id).resolve(defaultIdFilename) : getLocalVersionFolder(id).resolve(alt).resolve(defaultIdFilename);
    }

    public Path getLocalVersionFolder(NutsId id) {
        return CoreIOUtils.resolveNutsDefaultPath(id, getStoreLocation());
    }

    public NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        Path cacheContent = getIdLocalFile(id);
        if (cacheContent != null && Files.exists(cacheContent)) {
            return new NutsContent(cacheContent, true, false);
        }
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    protected String getIdFilename(NutsId id) {
        if (repo == null) {
            return workspace.config().getDefaultIdFilename(id);
        }
        return repo.getIdFilename(id);
    }

    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        String idFilename = getIdFilename(id);
        Path goodFile = null;
        String alt = id.getAlternative();
        String goodAlt = null;
        Path versionFolder = getLocalVersionFolder(id);
        if (NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE.equals(alt)) {
            goodFile = versionFolder.resolve(idFilename);
            if (Files.exists(goodFile)) {
                return getWorkspace().parser().parseDescriptor(goodFile);
            }
        } else if (!CoreStringUtils.isBlank(alt)) {
            goodAlt = alt.trim();
            goodFile = versionFolder.resolve(goodAlt).resolve(idFilename);
            if (Files.exists(goodFile)) {
                return getWorkspace().parser().parseDescriptor(goodFile).setAlternative(goodAlt);
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
                                choice = loadMatchingDescriptor(subFolder.resolve(idFilename), id).setAlternative(subFolder.getFileName().toString());
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
                    c = getWorkspace().parser().parseDescriptor(goodFile).setAlternative("");
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

    protected NutsDescriptor loadMatchingDescriptor(Path file, NutsId id) {
        if (Files.exists(file)) {
            NutsDescriptor d = Files.isRegularFile(file) ? getWorkspace().parser().parseDescriptor(file) : null;
            if (d != null) {
                Map<String, String> query = id.getQueryMap();
                String os = query.get("os");
                String arch = query.get("arch");
                String dist = query.get("dist");
                String platform = query.get("platform");
                if (d.matchesEnv(arch, os, dist, platform)) {
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

    public Iterator<NutsId> findVersions(NutsId id, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (id.getVersion().isSingleValue()) {
            NutsId id1 = id.setFaceDescriptor();
            Path localFile = getIdLocalFile(id1);
            if (localFile != null && Files.isRegularFile(localFile)) {
                return Collections.singletonList(id.setNamespace(repo == null ? null : repo.config().getName())).iterator();
            }
            return null;
        }
        return findInFolder(getLocalGroupAndArtifactFile(id), filter, deep, session);
    }

    public Iterator<NutsId> findInFolder(Path folder, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        folder = rootPath.resolve(folder);
        if (folder == null || !Files.exists(folder) || !Files.isDirectory(folder)) {
            //            return Collections.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(getWorkspace(), repo == null ? null : repo.config().getName(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                if (repo == null) {
                    NutsRepositoryFolderHelper.this.undeploy(id, session);
                } else {
                    repo.undeploy(id, session);
                }
            }

            @Override
            public boolean isDescFile(Path pathname) {
                return pathname.getFileName().toString().endsWith(".nuts");
            }

            @Override
            public NutsDescriptor parseDescriptor(Path pathname, NutsRepositorySession session) throws IOException {
                return getWorkspace().parser().parseDescriptor(pathname);
            }
        }, deep);
    }

    public Path getStoreLocation() {
        return rootPath;
    }

    public NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
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

    public void deploy(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session) {
        Path descFile = getIdLocalFile(deployment.getId().setFaceDescriptor());
        Path pckFile = getIdLocalFile(deployment.getId());

        if (Files.exists(descFile) && !deployment.isForce()) {
            throw new NutsAlreadyDeployedException(deployment.toString());
        }
        if (Files.exists(pckFile) && !deployment.isForce()) {
            throw new NutsAlreadyDeployedException(deployment.toString());
        }
        if (Files.exists(descFile)) {
            log.log(Level.FINE, "Nuts descriptor file Overridden {0}", descFile);
        }
        if (Files.exists(pckFile)) {
            log.log(Level.FINE, "Nuts component  file Overridden {0}", pckFile);
        }

        getWorkspace().formatter().createDescriptorFormat().setPretty(true).format(deployment.getDescriptor(), descFile);
        getWorkspace().io().copy().from(new ByteArrayInputStream(getWorkspace().io().getSHA1(deployment.getDescriptor()).getBytes())).to(descFile.resolveSibling(descFile.getFileName() + ".sha1")).safeCopy().run();
        getWorkspace().io().copy().from(deployment.getContent()).to(pckFile).safeCopy().run();
        getWorkspace().io().copy().from(new ByteArrayInputStream(CoreSecurityUtils.evalSHA1(pckFile).getBytes())).to(pckFile.resolveSibling(pckFile.getFileName() + ".sha1")).safeCopy().run();
        if (repo instanceof AbstractNutsRepository) {
            ((AbstractNutsRepository) repo).fireOnDeploy(new NutsContentEvent(pckFile, deployment, getWorkspace(), repo));
        }

    }

    public void undeploy(NutsId id, NutsRepositorySession session) {
        Path localFolder = getIdLocalFile(id);
        if (localFolder != null && Files.exists(localFolder)) {
            try {
                CoreIOUtils.delete(localFolder);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }

    }

    public void reindexFolder() {
        reindexFolder(getStoreLocation());
    }

    private void reindexFolder(Path path) {
        File folder = path.toFile();
        File[] children = folder.listFiles();
        TreeSet<String> files = new TreeSet<>();
        TreeSet<String> folders = new TreeSet<>();
        if (children != null) {
            for (File child : children) {
                if (!child.getName().startsWith(".") && !child.getName().equals("LATEST") && !child.getName().equals("RELEASE")) {
                    if (child.isDirectory()) {
                        reindexFolder(child.toPath());
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
            throw new RuntimeException(e);
        }
        try (PrintStream p = new PrintStream(new File(folder, ".folders"))) {
            for (String file : folders) {
                p.println(file);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
