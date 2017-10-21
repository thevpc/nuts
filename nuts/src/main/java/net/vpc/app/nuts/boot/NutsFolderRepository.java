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
package net.vpc.app.nuts.boot;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends AbstractNutsRepository {

    public static final Logger log = Logger.getLogger(NutsFolderRepository.class.getName());

    public NutsFolderRepository(String repositoryId, String repositoryLocation, NutsWorkspace workspace, File root) {
        super(new NutsRepositoryConfig(repositoryId, repositoryLocation, NutsConstants.DEFAULT_REPOSITORY_TYPE), workspace, root, SPEED_FAST);
        extensions.put("src", "-src.zip");
    }


    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsSession session) throws IOException {
        File localNutFile = getLocalNutDescriptorFile(id).getFile();
        if (session.getFetchMode() != FetchMode.REMOTE) {
            if (localNutFile.exists()) {
                return NutsDescriptor.parse(localNutFile);
            }
        }
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                NutsDescriptor nutsDescriptor = remote.fetchDescriptor(id, session);
                if (nutsDescriptor != null) {
                    nutsDescriptor.write(localNutFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    @Override
    protected String fetchHashImpl(NutsId id, NutsSession session) throws IOException {
        File localNutFile = getLocalGroupAndArtifactAndVersionFile(id, false).getFile();
        if (session.getFetchMode() != FetchMode.REMOTE) {
            if (localNutFile.exists()) {
                return SecurityUtils.evalSHA1(localNutFile);
            }
        }
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                String hash = remote.fetchHash(id, session);
                if (hash != null) {
                    IOUtils.copy(hash, localNutFile, true);
                    return hash;
                }
            }
        }
        return null;
    }

    @Override
    protected String fetchDescriptorHashImpl(NutsId id, NutsSession session) throws IOException {
        File localNutFile = getLocalNutDescriptorFile(id).getFile();
        if (session.getFetchMode() != FetchMode.REMOTE) {
            if (localNutFile.exists()) {
                return SecurityUtils.evalSHA1(localNutFile);
            }
        }
        if (session.isTransitive()) {
            for (NutsRepository remote : getMirrors()) {
                String hash = remote.fetchDescriptorHash(id, session);
                if (hash != null) {
                    IOUtils.copy(hash, localNutFile, true);
                    return hash;
                }
            }
        }
        return null;
    }

    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, File file, NutsSession session) throws IOException {
        NutsFile idFile = getLocalGroupAndArtifactAndVersionFile(id, true);
        File nutDescFile = idFile.getFile();
        if (nutDescFile == null) {
            throw new IllegalArgumentException("Invalid descriptor");
        }
        boolean allowOverride = true || isAllowedOverrideNut(id);
        if (allowOverride || !nutDescFile.exists()) {
            descriptor.write(nutDescFile);
            IOUtils.copy(descriptor.getSHA1(), IOUtils.createFile(nutDescFile.getParent(), nutDescFile.getName() + ".sha1"), true);
        }
        File localFile = getLocalGroupAndArtifactAndVersionFile(id, false).getFile();
        if (allowOverride || !localFile.exists()) {
            IOUtils.copy(file, localFile, true);
            IOUtils.copy(SecurityUtils.evalSHA1(localFile), IOUtils.createFile(localFile.getParent(), localFile.getName() + ".sha1"), true);
            NutsFile nutsFile = new NutsFile(id, descriptor, localFile, true, false);
            for (NutsRepositoryListener listener : getRepositoryListeners()) {
                listener.onDeploy(this, nutsFile);
            }
            for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
                listener.onDeploy(this, nutsFile);
            }
            return idFile.getId();
        }
        return idFile.getId();
    }

    @Override
    public boolean isSupportedMirroring() {
        return true;
    }

    protected void pushImpl(NutsId id, String repoId, NutsSession session) throws IOException {
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsFile local = fetch(id, nonTransitiveSession);
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
                throw new RepositoryNotFoundException("Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsRepositoryAmbiguousException("Two Repositories provides the same nuts " + id);
            }
            all.get(0).deploy(id, local.getDescriptor(), local.getFile(), session);
            for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
                listener.onPush(this, local);
            }
        } else {
            NutsRepository repo = getMirror(repoId);
            repo.deploy(id, local.getDescriptor(), local.getFile(), session);
            for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
                listener.onPush(this, local);
            }
        }
    }

    protected Iterator<NutsId> findImpl(final NutsDescriptorFilter filter, NutsSession session) throws IOException {
        if (!session.isTransitive()) {
            if (session.getFetchMode() != FetchMode.REMOTE) {
                return findInFolder(getStoreRoot(), filter, session);
            } else {
                return Collections.emptyIterator();
            }
        } else {
            IteratorList<NutsId> iterator = new IteratorList<NutsId>();
            if (session.getFetchMode() != FetchMode.REMOTE) {
                iterator.add(findInFolder(getStoreRoot(), filter, session));
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
                    iterator.add(child);
                }
            }
            return iterator;
        }
    }

    @Override
    protected NutsId resolveIdImpl(NutsId id, NutsSession session) throws IOException {
        String versionString = id.getVersion().getValue();
        if (VersionUtils.isStaticVersionPattern(versionString)) {
            NutsFile idAndFile = getLocalGroupAndArtifactAndVersionFile(id, false);
            File localFile = idAndFile == null ? null : idAndFile.getFile();
            if (session.getFetchMode() != FetchMode.REMOTE) {
                if (localFile != null && localFile.exists()) {
                    return id.setFace(idAndFile.getId().getFace())
                            .setNamespace(getRepositoryId());
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
                        NutsId id1 = null;
                        try {
                            id1 = repo.resolveId(id, session);
                        } catch (Exception ex) {
                            errors.append(ex).append("\n");
                        }
                        if (id1 != null) {
                            return id1;
                        }
                    }
                }
            }
            throw new NutsNotFoundException(id.toString(), errors.toString(), null);
        } else {
            Iterator<NutsId> allVersions = findVersions(id, VersionUtils.createFilter(versionString), session);

            NutsId a = null;
            while (allVersions.hasNext()) {
                NutsId next = allVersions.next();
                if (a == null || next.getVersion().compareTo(a.getVersion()) > 0) {
                    a = next;
                }
            }
            if (a == null) {
                throw new NutsNotFoundException(id.toString());
            }
            return a;
        }
    }

    @Override
    protected NutsFile fetchImpl(NutsId id, NutsSession session) throws IOException {
        NutsFile nutsDescFile = fetchComponentDesc(id, session);
        if(!NutsUtils.isEffectiveId(id)) {
            id = getWorkspace().fetchEffectiveId(nutsDescFile.getDescriptor(), session);
        }
        File localFile = getLocalGroupAndArtifactAndVersionFile(id, false).getFile();
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
                        File toFile = null;
                        try {
                            toFile = repo.fetch(id, session, localFile);
                        } catch (Exception ex) {
                            errors.append(ex.toString()).append("\n");
                        }
                        if (toFile!=null) {
                            Boolean executableJar = PlatformUtils.getExecutableJar(toFile);
                            NutsDescriptor desc = nutsDescFile.getDescriptor();
                            if(executableJar!=null && desc.isExecutable()!=executableJar.booleanValue()){
                                NutsFile localGroupAndArtifactAndVersionFile = getLocalGroupAndArtifactAndVersionFile(id, true);
                                File dlocalFile = localGroupAndArtifactAndVersionFile.getFile();
                                desc=desc.setExecutable(executableJar);
                                desc.write(dlocalFile);
                            }
                            NutsFile nutsFile = new NutsFile(id, desc, toFile, false, false);
                            for (NutsRepositoryListener listener : getWorkspace().getRepositoryListeners()) {
                                listener.onInstall(this, nutsFile);
                            }
                            for (NutsRepositoryListener listener : getRepositoryListeners()) {
                                listener.onInstall(this, nutsFile);
                            }
                            return nutsFile;
                        }
                    }
                }
            }
            throw new NutsNotFoundException(id.toString(), errors.toString(), null);
        } else {
            if (session.getFetchMode() != FetchMode.REMOTE) {
                return new NutsFile(id, nutsDescFile.getDescriptor(), localFile, true, false);
            } else {
                throw new NutsNotFoundException(id);
            }
        }
    }

    protected NutsFile getLocalNutDescriptorFile(NutsId id) {
        if (StringUtils.isEmpty(id.getGroup())) {
            throw new NutsIdInvalidFormatException("Missing group for " + id);
        }
        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        if (StringUtils.isEmpty(id.getName())) {
            throw new NutsIdInvalidFormatException("Missing name for " + id.toString());
        }
        File artifactFolder = new File(groupFolder, id.getName());
        if (id.getVersion().isEmpty()) {
            throw new NutsIdInvalidFormatException("Missing version for " + id.toString());
        }
        File versionFolder = new File(artifactFolder, id.getVersion().getValue());
        String face = id.getFace();
        if (!StringUtils.isEmpty(face)) {
            File altFile = new File(versionFolder, face);
            return new NutsFile(
                    id.setFace(face),null,
                    new File(altFile, NutsConstants.NUTS_DESC_FILE),true,true
            );
        }
        Map<String, String> query = id.getQueryMap();
        String os = (query.get("os"));
        String arch = (query.get("arch"));
        String dist = (query.get("dist"));
        String platform = (query.get("platform"));
        File[] subFolders = versionFolder.listFiles();
//        class NutsIdFileAndDesc {
//            NutsFile file;
//            NutsDescriptor desc;
//
//            public NutsIdFileAndDesc(NutsFile file, NutsDescriptor desc) {
//                this.file = file;
//                this.desc = desc;
//            }
//        }
        List<NutsFile> accepted = new ArrayList<>();
        if (subFolders != null) {
            for (File subFolder : subFolders) {
                if (subFolder.isDirectory()) {
                    File file = new File(subFolder, NutsConstants.NUTS_DESC_FILE);
                    if (file.exists()) {
                        NutsDescriptor d = NutsDescriptor.parseOrNull(file);
                        if (d != null) {
                            if (d.matchesEnv(arch, os, dist, platform)) {
                                face = d.getFace();
                                if (StringUtils.isEmpty(face)) {
                                    face = NutsConstants.QUERY_FACE_DEFAULT_VALUE;
                                }
                                accepted.add(

                                                new NutsFile(
                                                        id.setFace(face)
                                                                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true),
                                                        d,
                                                        file,true,true
                                                ));
                            }
                        }
                    }
                }
            }
        }
        if (!accepted.isEmpty()) {
            if (accepted.size() == 1) {
                //there is no conflict
                return accepted.get(0);
            }
            accepted.sort(new Comparator<NutsFile>() {
                @Override
                public int compare(NutsFile o1, NutsFile o2) {
                    return NutsUtils.NUTS_DESC_ENV_SPEC_COMPARATOR.compare(o1.getDescriptor(), o2.getDescriptor());
                }
            });
            return accepted.get(0);
        }
        //check default face
        File defaultFaceFile = new File(versionFolder, NutsConstants.QUERY_FACE_DEFAULT_VALUE);
        return new NutsFile(
                id.setFace(NutsConstants.QUERY_FACE_DEFAULT_VALUE),null,
                new File(defaultFaceFile, NutsConstants.NUTS_DESC_FILE),true,true
        );
    }

    protected NutsFile getLocalGroupAndArtifactAndVersionFile(NutsId id, boolean desc) throws IOException {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        NutsFile localNutDescriptorFile = getLocalNutDescriptorFile(id);
        if (desc) {
            return localNutDescriptorFile;
        }
        NutsDescriptor d = NutsDescriptor.parse(localNutDescriptorFile.getFile());
        if (d == null) {
            return null;
            //throw new IllegalArgumentException("Invalid nuts " + id);
        }
        File file = IOUtils.createFile(localNutDescriptorFile.getFile().getParent(), getQueryFilename(id,d));
        return new NutsFile(
                localNutDescriptorFile.getId(),null,
                file,true,true
        );
    }

    protected File getLocalGroupAndArtifactFile(NutsId id) throws IOException {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getStoreRoot(), id.getGroup().replaceAll("\\.", File.separator));
        return new File(groupFolder, id.getName());
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    public File fetchImpl(NutsId id, NutsSession session, File localPath) throws IOException {
        NutsFile file = fetch(id, session);
        if (file != null && file.getFile() != null) {
            if(localPath.isDirectory()){
                localPath=new File(localPath,NutsUtils.getNutsFileName(id,IOUtils.getFileExtension(file.getFile())));
            }
            IOUtils.copy(file.getFile(), localPath, true);
            return localPath;
        }
        throw new NutsNotFoundException(id.toString());
    }

    @Override
    public boolean fetchDescriptorImpl(NutsId id, NutsSession session, File localPath) throws IOException {
        File localDescFile = getLocalGroupAndArtifactAndVersionFile(id, true).getFile();
        if (localDescFile == null) {
            return false;
        } else if (!localDescFile.exists()) {
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
                            found = repo.fetchDescriptor(id, session, localPath);
                        } catch (Exception ex) {
                            errors.append(ex).append("\n");
                        }
                        if (found) {
                            return true;
                        }
                    }

                }
            }
            return false;
        } else {
            if (session.getFetchMode() != FetchMode.REMOTE) {
                IOUtils.copy(localDescFile, localPath, true);
                return true;
            } else {
                return false;
            }
        }
    }

    //    public boolean isInstalled(NutsId id) throws IOException {
//        NutsFile file = fetch(id, false);
//        return (file != null && file.getFile() != null);
//    }
//
    protected void undeployImpl(NutsId id, NutsSession session) throws IOException {
        File localFolder = getLocalGroupAndArtifactAndVersionFile(id, false).getFile();
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

    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException {
        Iterator<NutsId> namedNutIdIterator = null;
        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != FetchMode.REMOTE) {
            try {
                namedNutIdIterator = findInFolder(getLocalGroupAndArtifactFile(id), new NutsDescriptorFilterAndEnvNutsDescriptorFilter(versionFilter, id.getQueryMap()), session);
            } catch (Exception ex) {
                errors.append(ex).append(" \n");
            }
        }
        if (!session.isTransitive()) {
            if (namedNutIdIterator == null) {
                return Collections.emptyIterator();
            }
            return namedNutIdIterator;
        }
        IteratorList<NutsId> list = new IteratorList<>();
        if (namedNutIdIterator != null) {
            list.add(namedNutIdIterator);
        }
        for (NutsRepository repo : getMirrors()) {
            int sup = 0;
            try {
                sup = repo.getSupportLevel(id, session);
            } catch (Exception ex) {
                errors.append(ex.toString()).append("\n");
            }

            if (sup > 0) {
                Iterator<NutsId> vers = null;
                try {
                    vers = repo.findVersions(id, versionFilter, session);
                } catch (Exception ex) {
                    errors.append(ex).append(" \n");
                }
                if (vers != null) {
                    list.add(vers);
                }
            }
        }
        return list;
    }

    protected Iterator<NutsId> findInFolder(File folder, final NutsDescriptorFilter filter, NutsSession session) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
            return Collections.emptyIterator();
        }
        return new FolderNutIdIterator(getWorkspace(), getRepositoryId(), folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsSession session) throws IOException {
                NutsFolderRepository.this.undeploy(id, session);
            }

            public boolean isDescFile(File pathname) {
                return pathname.getName().equals(NutsConstants.NUTS_DESC_FILE);
            }

            @Override
            public NutsDescriptor parseDescriptor(File pathname, NutsSession session) throws IOException {
                return NutsDescriptor.parse(pathname);
            }
        });
    }

    protected NutsFile fetchComponentDesc(NutsId id, NutsSession session) throws IOException {
        if (session == null) {
            throw new IllegalArgumentException("Missing Session");
        }
        NutsFile localGroupAndArtifactAndVersionFile = getLocalGroupAndArtifactAndVersionFile(id, true);
        File localFile = localGroupAndArtifactAndVersionFile.getFile();
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
                            found = repo.fetchDescriptor(id, session, localFile);
                        } catch (Exception ex) {
                            errors.append(ex.toString()).append("\n");
                        }
                        if (found) {
                            NutsDescriptor desc = NutsDescriptor.parse(localFile);
                            NutsId ed = getWorkspace().fetchEffectiveId(desc, session);
                            return new NutsFile(ed, desc, localFile, false, false);
                        }
                    }
                }
            }
            throw new NutsNotFoundException(id.toString(), errors.toString(), null);
        } else {
            if (session.getFetchMode() != FetchMode.REMOTE) {
                NutsDescriptor desc = NutsDescriptor.parse(localFile);
                NutsId ed = getWorkspace().fetchEffectiveId(desc, session);
                return new NutsFile(ed, desc, localFile, true, false);
            }
            throw new NutsNotFoundException(id.toString());
        }
    }

    private File getStoreRoot() {
        return new File(getRoot(), NutsConstants.DEFAULT_COMPONENTS_ROOT);
    }

}
