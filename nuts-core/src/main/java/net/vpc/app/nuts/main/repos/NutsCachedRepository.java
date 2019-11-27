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
package net.vpc.app.nuts.main.repos;

import net.vpc.app.nuts.runtime.repocommands.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.CommonRootsHelper;
import net.vpc.app.nuts.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;

import net.vpc.app.nuts.NutsDefaultContent;
import net.vpc.app.nuts.main.wscommands.DefaultNutsFetchCommand;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.iter.IteratorBuilder;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsCachedRepository extends AbstractNutsRepositoryBase {

    public final NutsLogger LOG;

    protected final NutsRepositoryFolderHelper lib;
    protected final NutsRepositoryFolderHelper cache;
    private final NutsRepositoryMirroringHelper mirroring;

    public NutsCachedRepository(NutsCreateRepositoryOptions options, NutsWorkspace workspace, NutsRepository parent, int speed, boolean supportedMirroring, String repositoryType) {
        super(options, workspace, parent, speed, supportedMirroring, repositoryType);
        LOG=workspace.log().of(DefaultNutsRepoConfigManager.class);
        cache = new NutsRepositoryFolderHelper(this, workspace, config().getStoreLocation(NutsStoreLocation.CACHE));
        lib = new NutsRepositoryFolderHelper(this, workspace, config().getStoreLocation(NutsStoreLocation.LIB));
        mirroring = new NutsRepositoryMirroringHelper(this, cache);
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            NutsDescriptor libDesc = lib.fetchDescriptorImpl(id, session);
            if (libDesc != null) {
                return libDesc;
            }
            if (cache.isReadEnabled()) {
                NutsDescriptor cacheDesc = cache.fetchDescriptorImpl(id, session);
                if (cacheDesc != null) {
                    return cacheDesc;
                }
            }
        }
        RuntimeException impl2Ex=null;
        RuntimeException mirrorsEx=null;
        NutsDescriptor c = null;
        try {
            c = fetchDescriptorImpl2(id, session);
        }catch (RuntimeException ex){
            impl2Ex=ex;
        }
        if (c != null) {
            if (cache.isWriteEnabled()) {
                NutsId id0 = NutsWorkspaceExt.of(getWorkspace()).resolveEffectiveId(c, new DefaultNutsFetchCommand(getWorkspace()).session(session.getSession()));
                if (!id0.getLongName().equals(c.getId().getLongName())) {
                    c = c.builder().setId(id0).build();
                }
                cache.deployDescriptor(c.getId(), c, session);
            }
            return c;
        }
        NutsDescriptor m = null;
        try {
            m=mirroring.fetchDescriptorImplInMirrors(id, session);
        }catch (RuntimeException ex){
            mirrorsEx=ex;
        }
        if(m!=null){
            return m;
        }
        if(impl2Ex!=null){
            throw impl2Ex;
        }
        if(mirrorsEx!=null){
            throw mirrorsEx;
        }
        return m;
    }

    @Override
    public void deployImpl(NutsDeployRepositoryCommand command) {
        lib.deploy(command);
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        mirroring.push(command);
    }

    @Override
    public final Iterator<NutsId> searchImpl(final NutsIdFilter filter, NutsRepositorySession session) {
        List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);
        List<Iterator<NutsId>> li = new ArrayList<>();
        List<String> rootStrings=new ArrayList<>();
        for (CommonRootsHelper.PathBase root : roots) {
            li.add(lib.findInFolder(Paths.get(root.getName()), filter, root.isDeep() ? Integer.MAX_VALUE : 2, session));
            if (cache.isReadEnabled()) {
                li.add(cache.findInFolder(Paths.get(root.getName()), filter, root.isDeep() ? Integer.MAX_VALUE : 2, session));
            }
            if(root.isDeep()){
                rootStrings.add(root.getName()+"/*");
            }else{
                rootStrings.add(root.getName());
            }
        }
        Iterator<NutsId> p = null;
        try {
            p = searchImpl2(filter, rootStrings.toArray(new String[0]), session);
        } catch (NutsNotFoundException ex) {
            //ignore....
        } catch (Exception ex) {
            //ignore....
            LOG.log(Level.SEVERE, "Search latest versions error : " + ex.toString(), ex);
        }
        if (p != null) {
            li.add(p);
        }
        return mirroring.search(IteratorBuilder.ofList(li).distinct(NutsId::getLongName).build(), filter, session);
    }

    @Override
    public final NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            NutsContent c = lib.fetchContentImpl(id, localPath, session);
            if (c != null) {
                return c;
            }
            if (cache.isReadEnabled()) {
                c = cache.fetchContentImpl(id, localPath, session);
                if (c != null) {
                    return c;
                }
            }
        }
        RuntimeException impl2Ex=null;
        RuntimeException mirrorsEx=null;
        NutsContent c = null;
        if (cache.isWriteEnabled()) {
            Path cachePath = cache.getLongNameIdLocalFile(id);
            c = fetchContentImpl2(id, descriptor, cachePath, session);
            if (c != null) {
                //already deployed because fetchContentImpl2 is run against cachePath
//                cache.deployContent(id, c.getPath(), session);
                if (localPath != null) {
                    getWorkspace().io().copy()
                            .session(session.getSession())
                            .from(cachePath).to(localPath).run();
                } else {
                    localPath = cachePath;
                }
                return new NutsDefaultContent(localPath, true, false);
            }
        } else {
            try {
                c = fetchContentImpl2(id, descriptor, localPath, session);
            }catch (RuntimeException ex){
                impl2Ex=ex;
            }
            if (c != null) {
                return c;
            }
        }
        try {
            c = mirroring.fetchContent(id, descriptor, localPath, session);
        }catch (RuntimeException ex){
            mirrorsEx=ex;
        }
        if (c != null) {
            return c;
        }
        if(impl2Ex!=null){
            if(impl2Ex instanceof NutsNotFoundException){
                throw impl2Ex;
            }
            throw new NutsNotFoundException(getWorkspace(), id,impl2Ex);
        }
        if(mirrorsEx!=null){
            if(mirrorsEx instanceof NutsNotFoundException){
                throw mirrorsEx;
            }
            throw new NutsNotFoundException(getWorkspace(), id,mirrorsEx);
        }
        throw new NutsNotFoundException(getWorkspace(), id);
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    @Override
    public final void undeployImpl(NutsRepositoryUndeployCommand options) {
        lib.undeploy(options);
    }

    @Override
    public final Iterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {

        List<Iterator<NutsId>> all = new ArrayList<>();
        if (session.getFetchMode() != NutsFetchMode.REMOTE) {
            try {
                all.add(lib.searchVersions(id, idFilter, true, session));
                if (cache.isReadEnabled()) {
                    all.add(cache.searchVersions(id, idFilter, true, session));
                }
                Iterator<NutsId> p = null;
//                try {
//                    p = searchVersionsImpl2(id, idFilter, session);
//                    if (p != null) {
//                        all.add(p);
//                    }
//                } catch (Exception ex) {
//                    //ignore....
//                }
            } catch (NutsNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }

        try {
            Iterator<NutsId> p = null;
            p = searchVersionsImpl2(id, idFilter, session);
            if (p != null) {
                all.add(p);
            }
        } catch (NutsNotFoundException ex) {
            //ignore error
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Search versions error : " + ex.toString(), ex);
            //ignore....
        }
        Iterator<NutsId> namedNutIdIterator = IteratorBuilder.ofList(all).distinct(NutsId::getLongName).build();

        if (namedNutIdIterator == null) {
            namedNutIdIterator = IteratorUtils.emptyIterator();
        }
        return mirroring.searchVersionsImpl_appendMirrors(namedNutIdIterator, id, idFilter, session);

    }

    @Override
    public final NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (id.getVersion().isBlank() && filter == null) {
            NutsId bestId = lib.searchLatestVersion(id, filter, session);
            NutsId c1 = null;
            if (cache.isReadEnabled()) {
                c1 = cache.searchLatestVersion(id, filter, session);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            }
            try {
                c1 = searchLatestVersion2(id, filter, session);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            } catch (NutsNotFoundException|NutsFetchModeNotSupportedException ex) {
                //ignore
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, "Search latest versions error : " + ex.toString(), ex);
                //ignore....
            }
            return mirroring.searchLatestVersion(bestId, id, filter, session);
        }
        return super.searchLatestVersion(id, filter, session);
    }

    @Override
    public final NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                lib.reindexFolder();
                if (cache.isWriteEnabled()) {
                    cache.reindexFolder();
                }
                updateStatistics2();
                return this;
            }
        };
    }

    public Iterator<NutsId> searchVersionsImpl2(NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        return null;
    }

    public NutsId searchLatestVersion2(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        return null;
    }

    public NutsDescriptor fetchDescriptorImpl2(NutsId id, NutsRepositorySession session) {
        return null;
    }

    public NutsContent fetchContentImpl2(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        return null;
    }

    public Iterator<NutsId> searchImpl2(final NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        return null;
    }

    public void updateStatistics2() {

    }
    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode){
        String groups = config().getGroups();
        if (CoreStringUtils.isBlank(groups)) {
            return true;
        }
        return id.getGroupId().matches(CoreStringUtils.simpexpToRegexp(groups));
    }

}
