/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;
import net.vpc.common.io.IOUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsQueryOptions;
import net.vpc.common.util.IteratorUtils;
import net.vpc.common.util.LazyIterator;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends AbstractNutsRepository {

    public static final Logger log = Logger.getLogger(NutsFolderRepository.class.getName());

    public NutsFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FAST);
        extensions.put("src", "-src.zip");
    }

    @Override
    protected int getSupportLevelCurrent(NutsId id, NutsFetchMode mode) {
        switch (mode) {
            case REMOTE:
                return 0;
        }
        return super.getSupportLevelCurrent(id, mode);
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        String idFilename = getIdFilename(id);
        File compVersionFolder = getLocalVersionFolder(id, false);
        File cacheVersionFolder = getLocalVersionFolder(id, true);
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            File goodFile = null;
            String alt = id.getAlternative();
            String goodAlt = null;
            for (File versionFolder : new File[]{compVersionFolder, cacheVersionFolder}) {
                if (NutsConstants.ALTERNATIVE_DEFAULT_VALUE.equals(alt)) {
                    goodFile = new File(versionFolder, idFilename);
                    if (goodFile.exists()) {
                        return getWorkspace().getParseManager().parseDescriptor(goodFile);
                    }
                } else if (!StringUtils.isEmpty(alt)) {
                    goodAlt = alt.trim();
                    goodFile = new File(versionFolder + File.separator + goodAlt, idFilename);
                    if (goodFile.exists()) {
                        return getWorkspace().getParseManager().parseDescriptor(goodFile).setAlternative(goodAlt);
                    }
                } else {
                    //should test all files
                    File[] subFolders = versionFolder.listFiles();
                    NutsDescriptor best = null;
                    if (subFolders != null) {
                        for (File subFolder : subFolders) {
                            if (subFolder.isDirectory()) {
                                NutsDescriptor choice = null;
                                try {
                                    choice = loadMatchingDescriptor(new File(subFolder, idFilename), id).setAlternative(subFolder.getName());
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
                    }
                    goodFile = new File(versionFolder, idFilename);
                    if (goodFile.exists()) {
                        NutsDescriptor c = null;
                        try {
                            c = getWorkspace().getParseManager().parseDescriptor(goodFile).setAlternative("");
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
            }
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NutsRepository remote : getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor(id, session);
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    String a = nutsDescriptor.getAlternative();
                    File goodFile = null;
                    if (StringUtils.isEmpty(a) || a.equals(NutsConstants.ALTERNATIVE_DEFAULT_VALUE)) {
                        goodFile = new File(cacheVersionFolder, idFilename);
                    } else {
                        goodFile = new File(cacheVersionFolder + File.separator + StringUtils.trim(a), idFilename);
                    }
                    getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(nutsDescriptor, goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    @Override
    protected NutsId deployImpl(NutsId id, NutsDescriptor descriptor, String file, NutsDeployOptions options, NutsRepositorySession session) {
        if (options == null) {
            options = new NutsDeployOptions();
        }
        String alt = StringUtils.trim(descriptor.getAlternative());
        NutsId idContent = getWorkspace().getConfigManager().createComponentFaceId(id, descriptor).setAlternative(alt);
        File descFile = getIdLocalFile(idContent.setFaceDescriptor(), false);
        File pckFile = getIdLocalFile(idContent, false);

        boolean deployed = false;
        if (descFile.exists() && !options.isForce()) {
            throw new NutsAlreadyDeployedException(id.toString());
        }
        if (pckFile.exists() && !options.isForce()) {
            throw new NutsAlreadyDeployedException(id.toString());
        }
        if (descFile.exists()) {
            log.log(Level.FINE, "Nuts descriptor file Overridden {0}", descFile.getPath());
        }
        if (pckFile.exists()) {
            log.log(Level.FINE, "Nuts component  file Overridden {0}", pckFile.getPath());
        }

        getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor, descFile);
        CoreNutsUtils.copy(getWorkspace().getIOManager().getSHA1(descriptor), CoreIOUtils.createFile(descFile.getParent(), descFile.getName() + ".sha1"), true);

        CoreNutsUtils.copy(file, pckFile, true);
        CoreNutsUtils.copy(CoreSecurityUtils.evalSHA1(pckFile), CoreIOUtils.createFile(pckFile.getParent(), pckFile.getName() + ".sha1"), true);
        fireOnDeploy(new NutsContentEvent(id, descriptor, pckFile.getPath(), getWorkspace(), this));

        return idContent;
    }

    @Override
    public boolean isSupportedMirroring() {
        return true;
    }

    @Override
    protected void pushImpl(NutsId id, String repoId, NutsPushOptions options, NutsRepositorySession session) {
        NutsRepositorySession nonTransitiveSession = session.copy().setTransitive(false);
        NutsDescriptor desc = fetchDescriptor(id, nonTransitiveSession);
        NutsContent local = fetchContent(id, null, nonTransitiveSession);
        if (local == null) {
            throw new NutsNotFoundException(id);
        }
        if (!isSupportedMirroring()) {
            throw new NutsRepositoryNotFoundException("Not Repo for pushing " + id);
        }
        if (repoId == null) {
            List<NutsRepository> all = new ArrayList<>();
            for (NutsRepository remote : getMirrors()) {
                int lvl = remote.getSupportLevel(id, session.getFetchMode(), false);
                if (lvl > 0) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NutsRepositoryNotFoundException("Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsRepositoryAmbiguousException("Unable to perform push. Two Repositories provides the same nuts " + id);
            }
            all.get(0).deploy(id, desc, local.getFile(),
                    CoreNutsUtils.createNutsDeployOptions(options),
                    session);
        } else {
            NutsRepository repo = getMirror(repoId);
            repo.deploy(id, desc, local.getFile(), CoreNutsUtils.createNutsDeployOptions(options),
                    session);
        }
        fireOnPush(new NutsContentEvent(id, desc, local.getFile(), getWorkspace(), this));
    }

    @Override
    protected Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);
        String storeLocation1 = getStoreLocation(false);
        String storeLocation2 = getStoreLocation(true);

        if (!session.isTransitive()) {
            if (session.getFetchMode() != NutsFetchMode.REMOTE) {
                List<Iterator<NutsId>> list = new ArrayList<>();
                for (CommonRootsHelper.PathBase pp : roots) {
                    list.add(findInFolder(new File(storeLocation1, pp.getName()), filter, pp.isDeep(), session));
                    list.add(findInFolder(new File(storeLocation2, pp.getName()), filter, pp.isDeep(), session));
                }
                return IteratorUtils.concat(list);
            } else {
//                return Collections.emptyIterator();
                return null;
            }
        } else {
            List<Iterator<NutsId>> li = new ArrayList<>();
            for (CommonRootsHelper.PathBase root : roots) {
                li.add(findInFolder(new File(storeLocation1, root.getName()), filter, root.isDeep(), session));
                li.add(findInFolder(new File(storeLocation2, root.getName()), filter, root.isDeep(), session));
            }
            if (session.isTransitive() && isSupportedMirroring()) {
                for (NutsRepository remote : getMirrors()) {
                    li.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {

                        @Override
                        public Iterator<NutsId> iterator() {
                            return remote.find(filter, session);
                        }

                    }));
                }
            }
            return IteratorUtils.concat(li);
        }
    }

    @Override
    protected NutsContent fetchContentImpl(NutsId id, String localPath, NutsRepositorySession session) {
        File compContent = getIdLocalFile(id, false);
        File cacheContent = getIdLocalFile(id, true);
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            if (compContent != null && compContent.exists()) {
                return new NutsContent(compContent.getPath(), true, false);
            }
            if (cacheContent != null && cacheContent.exists()) {
                return new NutsContent(cacheContent.getPath(), true, false);
            }
        }
        if (session.isTransitive() && isSupportedMirroring()) {
            for (NutsRepository mirror : getMirrors()) {
                try {
                    NutsContent c = mirror.fetchContent(id, cacheContent == null ? null : cacheContent.getPath(), session);
                    if (c != null) {
                        if (localPath != null) {
                            IOUtils.copy(c.getFile(), new File(localPath), true);
                        } else {
                            return c;
                        }
                        return c;
                    }
                } catch (NutsNotFoundException ex) {
                    //ignore!
                }
            }
        }
        throw new NutsNotFoundException(id);
    }

    protected File getLocalVersionFolder(NutsId id, boolean cache) {
        return CoreNutsUtils.resolveNutsDefaultPath(id, new File(getStoreLocation(cache)));
    }

    protected NutsDescriptor loadMatchingDescriptor(File file, NutsId id) {
        if (file.exists()) {
            NutsDescriptor d = file.isFile() ? getWorkspace().getParseManager().parseDescriptor(file) : null;
            if (d != null) {
                Map<String, String> query = id.getQueryMap();
                String os = (query.get("os"));
                String arch = (query.get("arch"));
                String dist = (query.get("dist"));
                String platform = (query.get("platform"));
                if (d.matchesEnv(arch, os, dist, platform)) {
                    return d;
                }
            }
        }
        return null;
    }

    protected File getIdLocalFile(NutsId id, boolean cache) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        if (id.getVersion().isEmpty()) {
            return null;
        }
        String alt = StringUtils.trim(id.getAlternative());
//        if (desc) {
//            return (alt.isEmpty() || alt.equals(NutsConstants.ALTERNATIVE_DEFAULT_VALUE)) ?
//                    new File(getLocalVersionFolder(id), NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME)
//                    : new File(getLocalVersionFolder(id) + File.separator + alt, NutsConstants.NUTS_WORKSPACE_CONFIG_FILE_NAME);
//        } else {
        return (alt.isEmpty() || alt.equals(NutsConstants.ALTERNATIVE_DEFAULT_VALUE))
                ? new File(getLocalVersionFolder(id, cache), getIdFilename(id))
                : new File(getLocalVersionFolder(id, cache) + File.separator + alt, getIdFilename(id));
//        }
    }

    protected File getLocalGroupAndArtifactFile(NutsId id, boolean cache) {
        if (StringUtils.isEmpty(id.getGroup())) {
            return null;
        }
        if (StringUtils.isEmpty(id.getName())) {
            return null;
        }
        File groupFolder = new File(getStoreLocation(cache), id.getGroup().replace('.', File.separatorChar));
        return new File(groupFolder, id.getName());
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    protected void undeployImpl(NutsId id, NutsRepositorySession session) {
        File localFolder = getIdLocalFile(id, false);
        if (localFolder != null && localFolder.exists()) {
            if (localFolder.isDirectory()) {
                File[] files = localFolder.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
            }
            localFolder.delete();
        }
    }

    @Override
    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        Iterator<NutsId> namedNutIdIterator = null;
//        StringBuilder errors = new StringBuilder();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            try {
                if (id.getVersion().isSingleValue()) {
                    NutsId id1 = id.setFaceDescriptor();
                    File localFile = getIdLocalFile(id1, false);
                    if (localFile != null && localFile.isFile()) {
                        return Collections.singletonList(id.setNamespace(getName())).iterator();
                    }
                    localFile = getIdLocalFile(id1, true);
                    if (localFile != null && localFile.isFile()) {
                        return Collections.singletonList(id.setNamespace(getName())).iterator();
                    }
//                    return Collections.emptyIterator();
                    return null;
                }
                List<Iterator<NutsId>> all = new ArrayList<>();
                all.add(findInFolder(getLocalGroupAndArtifactFile(id, false), idFilter, true, session));
                all.add(findInFolder(getLocalGroupAndArtifactFile(id, true), idFilter, true, session));
                namedNutIdIterator = IteratorUtils.concat(all);
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
        if (!session.isTransitive()) {
//            if (namedNutIdIterator == null) {
//                return Collections.emptyIterator();
//            }
            return namedNutIdIterator;
        }
        List<Iterator<NutsId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (isSupportedMirroring()) {
            for (NutsRepository repo : getMirrors()) {
                int sup = 0;
                try {
                    sup = repo.getSupportLevel(id, session.getFetchMode(), session.isTransitive());
                } catch (Exception ex) {
//                errors.append(ex.toString()).append("\n");
                }

                if (sup > 0) {
                    list.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {

                        @Override
                        public Iterator<NutsId> iterator() {
                            return repo.findVersions(id, idFilter, session);
                        }
                    }));
                }
            }
        }
        return IteratorUtils.concat(list);

    }

    protected Iterator<NutsId> findInFolder(File folder, final NutsIdFilter filter, boolean deep, NutsRepositorySession session) {
        if (folder == null || !folder.exists() || !folder.isDirectory()) {
//            return Collections.emptyIterator();
            return null;
        }
        return new FolderNutIdIterator(getWorkspace(), this, folder, filter, session, new FolderNutIdIterator.FolderNutIdIteratorModel() {
            @Override
            public void undeploy(NutsId id, NutsRepositorySession session) {
                NutsFolderRepository.this.undeploy(id, session);
            }

            @Override
            public boolean isDescFile(File pathname) {
                return pathname.getName().endsWith(".nuts");
            }

            @Override
            public NutsDescriptor parseDescriptor(File pathname, NutsRepositorySession session) throws IOException {
                return getWorkspace().getParseManager().parseDescriptor(pathname);
            }
        }, deep);
    }

    public String getStoreLocation(boolean cache) {
        if (cache) {
            return getStoreLocation(NutsStoreLocation.CACHE);
        } else {
            return getStoreLocation(NutsStoreLocation.LIB);
        }
    }

    @Override
    public String getStoreLocation() {
        return getStoreLocation(false);
    }

    @Override
    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (id.getVersion().isEmpty() && filter == null) {
            NutsId bestId = null;
            for (boolean cache : new boolean[]{false, true}) {
                File file = getLocalGroupAndArtifactFile(id, cache);
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
            }
            if (session.isTransitive() && isSupportedMirroring()) {
                for (NutsRepository remote : getMirrors()) {
                    NutsDescriptor nutsDescriptor = null;
                    try {
                        nutsDescriptor = remote.fetchDescriptor(id, session);
                    } catch (Exception ex) {
                        //ignore
                    }
                    if (nutsDescriptor != null) {
//                        NutsId id2 = CoreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                        NutsId id2 = getWorkspace().resolveEffectiveId(nutsDescriptor,
                                new DefaultNutsQueryOptions()
                                        .setTransitive(session.isTransitive())
                                        .setCached(session.isCached())
                                        .setIndexed(session.isIndexed()),
                                 session.getSession()
                        ).setFaceDescriptor();
                        File localNutFile = getIdLocalFile(id2, true);
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
        reindexFolder(new File(getStoreLocation(NutsStoreLocation.LIB)));
        reindexFolder(new File(getStoreLocation(NutsStoreLocation.CACHE)));
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
