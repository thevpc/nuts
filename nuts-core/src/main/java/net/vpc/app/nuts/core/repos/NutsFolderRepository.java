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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsFolderRepository extends AbstractNutsRepository {

    public static final Logger log = Logger.getLogger(NutsFolderRepository.class.getName());

    private NutsRepositoryFolderHelper lib;
    protected NutsRepositoryFolderHelper cache;
    private NutsRepositoryMirroringHelper mirroring;

    public NutsFolderRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parentRepository) {
        super(options, workspace, parentRepository, SPEED_FAST, true, NutsConstants.RepoTypes.NUTS);
        extensions.put("src", "-src.zip");
        cache = new NutsRepositoryFolderHelper(this, config().getStoreLocation(NutsStoreLocation.CACHE));
        lib = new NutsRepositoryFolderHelper(this, config().getStoreLocation(NutsStoreLocation.LIB));
        mirroring = new NutsRepositoryMirroringHelper(this, cache);
    }

    @Override
    protected NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            NutsDescriptor c = lib.fetchDescriptorImpl(id, session);
            if (c != null) {
                return c;
            }
            c = cache.fetchDescriptorImpl(id, session);
            if (c != null) {
                return c;
            }
        }
        return mirroring.fetchDescriptorImplInMirrors(id, session);
    }

    @Override
    protected void deployImpl(NutsRepositoryDeploymentOptions deployment, NutsRepositorySession session) {
        lib.deploy(deployment, session);
    }

    @Override
    protected void pushImpl(NutsId id, NutsPushCommand options, NutsRepositorySession session) {
        mirroring.push(id, options, session);
    }

    @Override
    protected Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);
        List<Iterator<NutsId>> li = new ArrayList<>();
        for (CommonRootsHelper.PathBase root : roots) {
            li.add(lib.findInFolder(Paths.get(root.getName()), filter, root.isDeep(), session));
            li.add(cache.findInFolder(Paths.get(root.getName()), filter, root.isDeep(), session));
        }
        return mirroring.find(IteratorUtils.concat(li), filter, session);
    }

    @Override
    protected NutsContent fetchContentImpl(NutsId id, Path localPath, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            NutsContent c = lib.fetchContentImpl(id, localPath, session);
            if (c != null) {
                return c;
            }
            c = cache.fetchContentImpl(id, localPath, session);
            if (c != null) {
                return c;
            }
        }
        NutsContent c = mirroring.fetchContent(id, localPath, session);
        if (c != null) {
            return c;
        }
        throw new NutsNotFoundException(id);
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    @Override
    protected void undeployImpl(NutsId id, NutsRepositorySession session) {
        lib.undeploy(id, session);
    }

    @Override
    protected Iterator<NutsId> findVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        Iterator<NutsId> namedNutIdIterator = null;
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            try {
                List<Iterator<NutsId>> all = new ArrayList<>();
                all.add(lib.findVersions(id, idFilter, true, session));
                all.add(cache.findVersions(id, idFilter, true, session));
                namedNutIdIterator = IteratorUtils.concat(all);
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }
        return mirroring.findVersionsImpl_appendMirrors(namedNutIdIterator, id, idFilter, session);

    }

//    @Override
//    public Path getComponentsLocation() {
//        return lib.getStoreLocation();
//    }
    @Override
    protected NutsId findLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (id.getVersion().isEmpty() && filter == null) {
            NutsId bestId = lib.findLatestVersion(id, filter, session);
            NutsId c1 = cache.findLatestVersion(id, filter, session);
            if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                bestId = c1;
            }
            return mirroring.findLatestVersion(bestId, id, filter, session);
        }
        return super.findLatestVersion(id, filter, session);
    }

    @Override
    public void updateStatistics() {
        lib.reindexFolder();
        cache.reindexFolder();
    }

}
