/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.CollectionUtils;
import net.vpc.common.util.IteratorList;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends AbstractNutsRepository {

    public static final Logger log = Logger.getLogger(NutsFolderRepository.class.getName());

    public NutsFolderRepository(String repositoryId, String repositoryLocation, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(new NutsRepositoryConfig(repositoryId, repositoryLocation, NutsConstants.REPOSITORY_TYPE_NUTS), workspace, parentRepository,
                workspace.getRepositoryManager().resolveRepositoryPath(StringUtils.isEmpty(repositoryLocation) ? repositoryId : repositoryLocation),
                SPEED_FAST);
        extensions.put("src", "-src.zip");
    }

    @Override
    protected int getSupportLevelCurrent(NutsId id, NutsSession session) {
        switch (session.getFetchMode()) {
            case REMOTE:
                return 0;
        }
        return super.getSupportLevelCurrent(id, session);
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) {
        File localNutFile = new File(getLocalNutDescriptorFile(id).getFile());
        //if (session.getFetchMode() != NutsFetchMode.REMOTE) {
        if (localNutFile.exists()) {
            return getWorkspace().getParseManager().parseDescriptor(localNutFile);
        }
        //}
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor(id, session);
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(nutsDescriptor, localNutFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    @Override
    protected String fetchHashImpl(NutsId id, NutsSession session) {
        File localNutFile = new File(getLocalGroupAndArtifactAndVersionFile(id, false).getFile());
//        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
        if (localNutFile.exists()) {
            return CoreSecurityUtils.evalSHA1(localNutFile);
        }
//        }
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                String hash = null;
                try {
                    hash = remote.fetchHash(id, session);
                } catch (Exception ex) {
                    //
                }
                if (hash != null) {
                    CoreNutsUtils.copy(hash, localNutFile, true);
                    return hash;
                }
            }
        }
        return null;
    }

    @Override
    protected String fetchDescriptorHashImpl(NutsId id, NutsSession session) {
        File localNutFile = new File(getLocalNutDescriptorFile(id).getFile());
//        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
        if (localNutFile.exists()) {
            return CoreSecurityUtils.evalSHA1(localNutFile);
        }
//        }
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                String hash = null;
                try {
                    hash = remote.fetchDescriptorHash(id, session);
                } catch (Exception ex) {
                    //ignore
                }
                if (hash != null) {
                    CoreNutsUtils.copy(hash, localNutFile, true);
                    return hash;
                }
            }
        }
        return null;
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsConfirmAction foundAction, NutsSession session) {
        if (foundAction == null) {
            foundAction = NutsConfirmAction.ERROR;
        }
        NutsDefinition idFile = getLocalGroupAndArtifactAndVersionFile(id, true);
        File nutDescFile = idFile.getFile() == null ? null : new File(idFile.getFile());
        if (nutDescFile == null) {
            throw new NutsIllegalArgumentException("Invalid descriptor");
        }
        boolean deployed = false;
        if (foundAction == NutsConfirmAction.ERROR && nutDescFile.exists()) {
            throw new NutsAlreadytDeployedException(id.toString());
        } else if (foundAction == NutsConfirmAction.IGNORE && nutDescFile.exists()) {
            //do nothing
        } else {
            if (nutDescFile.exists()) {
                log.log(Level.FINE, "Nuts descriptor file Overridden {0}", nutDescFile.getPath());
            }
            getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor, nutDescFile);
            CoreNutsUtils.copy(getWorkspace().getIOManager().getSHA1(descriptor), CoreIOUtils.createFile(nutDescFile.getParent(), nutDescFile.getName() + ".sha1"), true);
        }
        File localFile = CoreIOUtils.fileByPath(getLocalGroupAndArtifactAndVersionFile(id, false).getFile());
        if (foundAction == NutsConfirmAction.ERROR && localFile.exists()) {
            throw new NutsAlreadytDeployedException(id.toString());
        } else if (foundAction == NutsConfirmAction.IGNORE && localFile.exists()) {
            //do nothing
        } else {
            if (localFile.exists()) {
                log.log(Level.FINE, "Nuts component  file Overridden {0}", localFile.getPath());
            }
            CoreNutsUtils.copy(file, localFile, true);
            CoreNutsUtils.copy(CoreSecurityUtils.evalSHA1(localFile), CoreIOUtils.createFile(localFile.getParent(), localFile.getName() + ".sha1"), true);
            NutsDefinition nutsDefinition = new NutsDefinition(id, descriptor, localFile.getPath(), true, false, null, null);
            fireOnDeploy(nutsDefinition);
        }
        return idFile.getId();
    }

    @Override
    public boolean isSupportedMirroring() {
        return true;
    }

    @Override
    protected void pushImpl(NutsId id, String repoId, NutsConfirmAction foundAction, NutsSession session) {
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsDefinition local = fetch(id, nonTransitiveSession);
        if (local == null) {
            throw new NutsNotFoundException(id);
        }
        if (repoId == null) {
            List<NutsRepository> all = new ArrayList<NutsRepository>();
            for (NutsRepository remote : getMirrors()) {
                int lvl = remote.getSupportLevel(id, nonTransitiveSession);
                if (lvl > 0) {
                    all.add(remote);
                }
            }
            if (all.size() == 0) {
                throw new NutsRepositoryNotFoundException("Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsRepositoryAmbiguousException("Unable to perform push. Two Repositories provides the same nuts " + id);
            }
            all.get(0).deploy(id, local.getDescriptor(), local.getFile(), foundAction, session);
        } else {
            NutsRepository repo = getMirror(repoId);
            repo.deploy(id, local.getDescriptor(), local.getFile(), foundAction, session);
        }
        fireOnPush(local);
    }

    protected Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        if (!session.isTransitive()) {
            if (session.getFetchMode() != NutsFetchMode.REMOTE) {
                return findInFolder(new File(getStoreLocation()), filter, session);
            } else {
                return Collections.emptyIterator();
            }
        } else {
            IteratorList<NutsId> iterator = new IteratorList<NutsId>();
            if (session.getFetchMode() != NutsFetchMode.REMOTE) {
                iterator.addNonEmpty(findInFolder(new File(getStoreLocation()), filter, session));
            }
            for (NutsRepository remote : getMirrors()) {
                Iterator<NutsId> child = null;
                try {
                    child = remote.find(filter, session);
                } catch (Exception ex) {
//                    ex.printStackTrace();
                    //
                }
                if (child != null) {
                    iterator.addNonEmpty(child);
                }
            }
            return iterator;
        }
    }

    @Override
    protected NutsDefinition fetchImpl(NutsId id, NutsSession session) {
        NutsDefinition nutsDescFile = fetchComponentDesc(id, session);
        if (!CoreNutsUtils.isEffectiveId(id)) {
            id = getWorkspace().resolveEffectiveId(nutsDescFile.getDescriptor(), session);
        }
        File localFile = CoreIOUtils.fileByPath(getLocalGroupAndArtifactAndVersionFile(id, false).getFile());
        if (localFile == null) {
            throw new NutsNotFoundException(id);
        } else if (!localFile.exists()) {
            for (String location : nutsDescFile.getDescriptor().getLocations()) {
                if (!StringUtils.isEmpty(location)) {
                    try {
                        getWorkspace().getIOManager().downloadPath(location, localFile, null, session);
                        return prepareInstall(localFile, nutsDescFile, id);
                    } catch (Exception ex) {
                        //ignore
                    }
                }
            }
            StringBuilder errors = new StringBuilder();
            if (session.isTransitive()) {
                NutsSession transitiveSession = session.copy().setTransitive(true);
                for (NutsRepository repo : getMirrors()) {
                    int sup = 0;
                    try {
                        sup = repo.getSupportLevel(id, transitiveSession);
                    } catch (Exception ex) {
                        errors.append(ex.toString()).append("\n");
                    }

                    if (sup > 0) {
                        boolean ok = false;
                        try {
                            repo.copyTo(id, localFile.getPath(), session);
                            ok = true;
                        } catch (SecurityException ex) {
                            //ignore
                        } catch (NutsNotFoundException ex) {
                            errors.append(ex.toString()).append("\n");
                        }
                        if (ok) {
                            return prepareInstall(localFile, nutsDescFile, id);
                        }
                    }
                }
            }
            throw new NutsNotFoundException(id.toString(), errors.toString(), null);
        } else {
            if (session.getFetchMode() != NutsFetchMode.REMOTE) {
                return new NutsDefinition(id, nutsDescFile.getDescriptor(), localFile.getPath(), true, false, null, null);
            } else {
                throw new NutsNotFoundException(id);
            }
        }
    }

    protected NutsDefinition prepareInstall(File localFile, NutsDefinition nutsDescFile, NutsId id) {
        Boolean executableJar = CorePlatformUtils.getExecutableJar(localFile);
        NutsDescriptor desc = nutsDescFile.getDescriptor();
        if (executableJar != null && desc.isExecutable() != executableJar) {
            NutsDefinition localGroupAndArtifactAndVersionFile = getLocalGroupAndArtifactAndVersionFile(id, true);
            File dlocalFile = CoreIOUtils.fileByPath(localGroupAndArtifactAndVersionFile.getFile());
            desc = desc.setExecutable(executableJar);
            getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(desc, dlocalFile);
        }
        NutsDefinition nutsDefinition = new NutsDefinition(id, desc, localFile.getPath(), false, false, null, null);
        fireOnInstall(nutsDefinition);
        return nutsDefinition;
    }

    protected File getLocalVersionFolder(NutsId id) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsElementNotFoundException("Missing group for " + id);
        }
        File groupFolder = new File(getStoreLocation(), id.getGroup().replace('.', File.separatorChar));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsElementNotFoundException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsElementNotFoundException("Missing version for " + id.toString());
        }
        return new File(artifactFolder, id.getVersion().getValue());
    }

    protected NutsDefinition loadNutsDefinition(File file, NutsId id) {
        if (file.exists()) {
            NutsDescriptor d = file.isFile() ? getWorkspace().getParseManager().parseDescriptor(file) : null;
            if (d != null) {
                Map<String, String> query = id.getQueryMap();
                String os = (query.get("os"));
                String arch = (query.get("arch"));
                String dist = (query.get("dist"));
                String platform = (query.get("platform"));
                if (d.matchesEnv(arch, os, dist, platform)) {
                    String alternative = d.getAlternative();
                    if (StringUtils.isEmpty(alternative)) {
                        alternative = NutsConstants.QUERY_FACE_DEFAULT_VALUE;
                    }
                    return
                            new NutsDefinition(
                                    id.builder()
                                            .setAlternative(alternative)
                                            .setQuery(NutsConstants.QUERY_EMPTY_ENV, true)
                                            .build(),
                                    d,
                                    file.getPath(),
                                    true, true, null, null
                            );
                }
            }
        }
        return null;
    }

    protected NutsDefinition getLocalNutDescriptorFile(NutsId id) {
        File versionFolder = getLocalVersionFolder(id);
        String alt = id.getAlternative();
        if (NutsConstants.QUERY_FACE_DEFAULT_VALUE.equals(alt) || StringUtils.isEmpty(alt)) {
            return new NutsDefinition(
                    id.setAlternative(null), null,
                    new File(versionFolder, NutsConstants.NUTS_DESC_FILE_NAME).getPath(),
                    true, true, null, null
            );
        }
        if (!StringUtils.isEmpty(alt)) {
            File altFile = new File(versionFolder, alt);
            return new NutsDefinition(
                    id.setAlternative(alt), null,
                    new File(altFile, NutsConstants.NUTS_DESC_FILE_NAME).getPath(),
                    true, true, null, null
            );
        }
        File[] subFolders = versionFolder.listFiles();
        List<NutsDefinition> accepted = new ArrayList<>();
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                if (subFolder.isDirectory()) {
                    NutsDefinition file = loadNutsDefinition(new File(subFolder, NutsConstants.NUTS_DESC_FILE_NAME), id);
                    if (file != null) {
                        accepted.add(file);
                    }
                }
            }
        }
        NutsDefinition file = loadNutsDefinition(new File(versionFolder, NutsConstants.NUTS_DESC_FILE_NAME), id);
        if (file != null) {
            accepted.add(file);
        }
        if (!accepted.isEmpty()) {
            if (accepted.size() == 1) {
                //there is no conflict
                return accepted.get(0);
            }
            accepted.sort(new Comparator<NutsDefinition>() {
                @Override
                public int compare(NutsDefinition o1, NutsDefinition o2) {
                    return CoreNutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(o1.getDescriptor(), o2.getDescriptor());
                }
            });
            return accepted.get(0);
        }
        //check default face
        File defaultFaceFile = new File(versionFolder, NutsConstants.QUERY_FACE_DEFAULT_VALUE);
        return new NutsDefinition(
                id.setFace(NutsConstants.QUERY_FACE_DEFAULT_VALUE), null,
                new File(defaultFaceFile, NutsConstants.NUTS_DESC_FILE_NAME).getPath(), true, true, null,
                null
        );
    }

    protected NutsDefinition getLocalGroupAndArtifactAndVersionFile(NutsId id, boolean desc) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        NutsDefinition localNutDescriptorFile = getLocalNutDescriptorFile(id);
        if (desc) {
            return localNutDescriptorFile;
        }
        if (!new File(localNutDescriptorFile.getFile()).isFile()) {
            return null;
        }
        NutsDescriptor d = null;
        try {
            d = getWorkspace().getParseManager().parseDescriptor(new File(localNutDescriptorFile.getFile()));
        } catch (NutsNotFoundException ex) {
            //
        }
        if (d == null) {
            return null;
            //throw new NutsIllegalArgumentException("Invalid nuts " + id);
        }
        File file = CoreIOUtils.createFile(new File(localNutDescriptorFile.getFile()).getParent(), getQueryFilename(id, d));
        return new NutsDefinition(
                localNutDescriptorFile.getId(), null,
                file.getPath(), true, true, null,
                null
        );
    }

    protected File getLocalGroupAndArtifactFile(NutsId id) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getStoreLocation(), id.getGroup().replace('.', File.separatorChar));
        return new File(groupFolder, id.getName());
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    public String copyToImpl(NutsId id, String localPath, NutsSession session) {
        NutsDefinition file = fetch(id, session);
        if (file != null && file.getFile() != null) {
            if (new File(localPath).isDirectory()) {
                localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, FileUtils.getFileExtension(file.getFile()))).getPath();
            }
            CoreNutsUtils.copy(new File(file.getFile()), new File(localPath), true);
            return localPath;
        }
        throw new NutsNotFoundException(id);
    }

    @Override
    public String copyDescriptorToImpl(NutsId id, String localPath, NutsSession session) {
        File localDescFile = CoreIOUtils.fileByPath(getLocalGroupAndArtifactAndVersionFile(id, true).getFile());

        if (new File(localPath).isDirectory()) {
            localPath = new File(localPath, CoreNutsUtils.getNutsFileName(id, "pom")).getPath();
        }

        if (localDescFile == null) {
            throw new NutsNotFoundException(id);
        } else if (!localDescFile.exists()) {
//            StringBuilder errors = new StringBuilder();
            if (session.isTransitive()) {
                NutsSession transitiveSession = session.copy().setTransitive(true);
                for (NutsRepository repo : getMirrors()) {
                    int sup = 0;
                    try {
                        sup = repo.getSupportLevel(id, transitiveSession);
                    } catch (Exception ex) {
//                        errors.append(ex.toString()).append("\n");
                    }

                    if (sup > 0) {
                        boolean found = false;
                        try {
                            repo.copyDescriptorTo(id, localPath, session);
                            found = true;
                        } catch (NutsNotFoundException ex) {
//                            errors.append(ex).append("\n");
                        }
                        if (found) {
                            return localPath;
                        }
                    }

                }
            }
            throw new NutsNotFoundException(id);
        } else {
            if (session.getFetchMode() != NutsFetchMode.REMOTE) {
                CoreNutsUtils.copy(localDescFile, new File(localPath), true);
                return localPath;
            } else {
                throw new NutsNotFoundException(id);
            }
        }
    }

    //    public boolean isInstalled(NutsId id) throws IOException {
//        NutsDefinition file = fetch(id, false);
//        return (file != null && file.getFile() != null);
//    }
//
    protected void undeployImpl(NutsId id, NutsSession session) {
        File localFolder = CoreIOUtils.fileByPath(getLocalGroupAndArtifactAndVersionFile(id, false).getFile());
        if (localFolder != null && localFolder.exists()) {
            File[] files = localFolder.listFiles();
            if (files != null) {
                for (File file : files) {
                    file.delete();
                }
            }
            localFolder.delete();
        }
    }

    protected List<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsSession session) {
        List<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            try {
                if (id.getVersion().isSingleValue()) {
                    NutsDefinition localGroupAndArtifactAndVersionFile = getLocalGroupAndArtifactAndVersionFile(id, true);
                    File localFile = CoreIOUtils.fileByPath(localGroupAndArtifactAndVersionFile.getFile());
                    if (localFile != null && localFile.isFile()) {
                        return new ArrayList<>(Collections.singletonList(id.setNamespace(getRepositoryId())));
                    }
                    return Collections.emptyList();
                }
                namedNutIdIterator = CollectionUtils.toList(findInFolder(getLocalGroupAndArtifactFile(id), idFilter, session));
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
        if (!session.isTransitive()) {
            if (namedNutIdIterator == null) {
                return Collections.emptyList();
            }
            return namedNutIdIterator;
        }
        List<NutsId> list = new ArrayList<>();
        if (namedNutIdIterator != null) {
            list.addAll(namedNutIdIterator);
        }
        for (NutsRepository repo : getMirrors()) {
            int sup = 0;
            try {
                sup = repo.getSupportLevel(id, session);
            } catch (Exception ex) {
//                errors.append(ex.toString()).append("\n");
            }

            if (sup > 0) {
                List<NutsId> vers = null;
                try {
                    vers = repo.findVersions(id, idFilter, session);
                } catch (NutsNotFoundException ex) {
//                    errors.append(ex).append(" \n");
                }
                if (vers != null) {
                    list.addAll(vers);
                }
            }
        }
        return list;
    }

    protected Iterator<NutsId> findInFolder(File folder, final NutsIdFilter filter, NutsSession session) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return Collections.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), getRepositoryId(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) {
                NutsFolderRepository.this.undeploy(id, session);
            }

            public boolean isDescFile(File pathname) {
                return pathname.getName().equals(NutsConstants.NUTS_DESC_FILE_NAME);
            }

            @Override
            public NutsDescriptor parseDescriptor(File pathname, NutsSession session) throws IOException {
                return getWorkspace().getParseManager().parseDescriptor(pathname);
            }
        });
    }

    protected NutsDefinition fetchComponentDesc(NutsId id, NutsSession session) {
        checkSession(session);
        NutsDefinition localGroupAndArtifactAndVersionFile = getLocalGroupAndArtifactAndVersionFile(id, true);
        File localFile = CoreIOUtils.fileByPath(localGroupAndArtifactAndVersionFile.getFile());
        if (localFile == null) {
            throw new NutsNotFoundException(id);
        } else if (!localFile.exists()) {
            StringBuilder errors = new StringBuilder();
            if (session.isTransitive()) {
                NutsSession transitiveSession = session.copy().setTransitive(true);
                for (NutsRepository repo : getMirrors()) {
                    int sup = 0;
                    try {
                        sup = repo.getSupportLevel(id, transitiveSession);
                    } catch (Exception ex) {
                        errors.append(ex.toString()).append("\n");
                    }

                    if (sup > 0) {
                        boolean found = false;
                        try {
                            repo.copyDescriptorTo(id, localFile.getPath(), session);
                            found = true;
                        } catch (Exception ex) {
                            errors.append(ex.toString()).append("\n");
                        }
                        if (found) {
                            NutsDescriptor desc = getWorkspace().getParseManager().parseDescriptor(localFile);
                            NutsId ed = getWorkspace().resolveEffectiveId(desc, session);
                            return new NutsDefinition(ed, desc, localFile.getPath(), false, false, null, null);
                        }
                    }
                }
            }
            throw new NutsNotFoundException(id.toString(), errors.toString(), null);
        } else {
            //if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            NutsDescriptor desc = getWorkspace().getParseManager().parseDescriptor(localFile);
            NutsId ed = getWorkspace().resolveEffectiveId(desc, session);
            return new NutsDefinition(ed, desc, localFile.getPath(), true, false, null, null);
            //
            //throw new NutsNotFoundException(id.toString());
        }
    }

    public String getStoreLocation() {
        String n = getConfigManager().getComponentsLocation();
        if (StringUtils.isEmpty(n)) {
            n = NutsConstants.FOLDER_NAME_LIB;
        }
        n = n.trim();
        return FileUtils.getAbsolutePath(new File(getConfigManager().getStoreLocation()), n);
    }

    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsSession session) {
        if (id.getVersion().isEmpty() && filter == null) {
            File file = getLocalGroupAndArtifactFile(id);
            NutsId bestId = null;
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
            if (session.isTransitive()) {
                for (NutsRepository remote : getMirrors()) {
                    NutsDescriptor nutsDescriptor = null;
                    try {
                        nutsDescriptor = remote.fetchDescriptor(id, session);
                    } catch (Exception ex) {
                        //ignore
                    }
                    if (nutsDescriptor != null) {
                        NutsId id2 = nutsDescriptor.getId();
                        File localNutFile = new File(getLocalNutDescriptorFile(id2).getFile());
                        getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(nutsDescriptor, localNutFile);
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            bestId = id2;
                        }
                    }
                }
            }
            return bestId;
        }
        return super.findLatestVersion(id, filter, session);
    }

    public void reindexFolder() {
        reindexFolder(new File(getStoreLocation()));
    }

    private void reindexFolder(File folder) {
        File[] children = folder.listFiles();
        TreeSet<String> files = new TreeSet<>();
        TreeSet<String> folders = new TreeSet<>();
        if (children != null) {
            for (File child : children) {
                if (!child.getName().startsWith(".") && !child.getName().equals("LATEST") && !child.getName().equals("RELEASE")) {
                    if (child.isDirectory()) {
                        reindexFolder(child);
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
