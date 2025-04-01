/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSpeedQualifier;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.concurrent.NLock;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.repository.impl.util.CommonRootsByPathHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.util.CommonRootsByIdHelper;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNUpdateRepositoryStatsCmd;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.spi.NRepositoryUndeployCmd;
import net.thevpc.nuts.spi.NUpdateRepositoryStatsCmd;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

/**
 * Created by vpc on 1/5/17.
 */
public class NCachedRepository extends AbstractNRepositoryBase {

    protected final NRepositoryFolderHelper lib;
    protected final NRepositoryFolderHelper cache;
    private final NRepositoryMirroringHelper mirroring;
    private boolean lockEnabled = true;

    public NCachedRepository(NAddRepositoryOptions options, NRepository parent, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, parent, speed, supportedMirroring, repositoryType, supportsDeploy);
        cache = new NRepositoryFolderHelper(this, config().getStoreLocation(NStoreType.CACHE).resolve(NConstants.Folders.ID), true,
                "cache", NElements.of().ofObjectBuilder().set("repoKind", "cache").build()
        );
        lib = new NRepositoryFolderHelper(this, config().getStoreLocation(NStoreType.LIB).resolve(NConstants.Folders.ID), false,
                "lib", NElements.of().ofObjectBuilder().set("repoKind", "lib").build()
        );
        mirroring = new NRepositoryMirroringHelper(this, cache);
    }

    public boolean isLockEnabled() {
        return lockEnabled;
    }

    public NCachedRepository setLockEnabled(boolean lockEnabled) {
        this.lockEnabled = lockEnabled;
        return this;
    }

    public NRepositoryFolderHelper getLib() {
        return lib;
    }

    public NRepositoryFolderHelper getCache() {
        return cache;
    }

    protected NLogOp _LOGOP() {
        return _LOG().with();
    }

    protected NLog _LOG() {
        return NLog.of(NCachedRepository.class);
    }

    @Override
    public void pushImpl(NPushRepositoryCmd command) {
        mirroring.push(command);
    }

    @Override
    public NDescriptor deployImpl(NDeployRepositoryCmd command) {
        return lib.deploy(command, getWorkspace().currentSession().getConfirm().orDefault());
    }

    @Override
    public final void undeployImpl(NRepositoryUndeployCmd options) {
        lib.undeploy(options);
    }

    @Override
    public NDescriptor fetchDescriptorImpl(NId id, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            if (lib.isReadEnabled()) {
                NDescriptor libDesc = lib.fetchDescriptorImpl(id);
                if (libDesc != null) {
                    return libDesc;
                }
            }
            if (cache.isReadEnabled()) {
                if (session.isCached()) {
                    NDescriptor cacheDesc = cache.fetchDescriptorImpl(id);
                    if (cacheDesc != null) {
                        return cacheDesc;
                    }
                }
            }
        }
        RuntimeException mirrorsEx = null;

        Callable<NOptional<NDescriptor>> nOptionalCallable = () -> {
            try {
                NDescriptor success = fetchDescriptorCore(id, fetchMode);
                if (success != null) {
                    if (cache.isWriteEnabled()) {
                        NId id0 = NWorkspaceExt.of().resolveEffectiveId(success);
                        if (!id0.getLongName().equals(success.getId().getLongName())) {
                            success = success.builder().setId(id0).build();
                        }
                        cache.deployDescriptor(success.getId(), success, NConfirmationMode.YES);
                    }
                    return NOptional.of(success);
                } else {
                    return NOptional.ofError(() -> NMsg.ofC("nuts descriptor not found %s", id), new NNotFoundException(id));
                }
            } catch (RuntimeException ex) {
                return NOptional.ofError(() -> NMsg.ofC("nuts descriptor not found %s", id), ex);
            }
        };
        NOptional<NDescriptor> res = null;
        try {
            boolean lockEnabled = isLockEnabled();
            res = lockEnabled ?
                    NLock.ofId(id.builder().setFaceDescriptor().build()).callWith(nOptionalCallable)
                    : nOptionalCallable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        if (res.isPresent()) {
            return res.get();
        }
        NDescriptor m = null;
        try {
            m = mirroring.fetchDescriptorImplInMirrors(id, fetchMode);
        } catch (RuntimeException ex) {
            mirrorsEx = ex;
        }
        if (m != null) {
            return m;
        }
        if (res.getError() != null) {
            throw (RuntimeException) res.getError();
        }
        if (mirrorsEx != null) {
            throw mirrorsEx;
        }
        return m;
    }

    @Override
    public final NIterator<NId> searchVersionsImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode) {

        List<NIterator<? extends NId>> all = new ArrayList<>();
//        NSession session = getWorkspace().currentSession();
        if (fetchMode != NFetchMode.REMOTE) {
            if (lib.isReadEnabled()) {
                all.add(NIteratorBuilder.of(
                                        lib.searchVersions(id, idFilter, true)
                                ).named(NElements.of().ofUplet("searchVersionInLib",NElements.of().ofString(getName())))
                                .build()

                );
            }
        }
        if (fetchMode != NFetchMode.REMOTE) {
            try {
                if (cache.isReadEnabled()) {
                    all.add(
                            NIteratorBuilder.of(
                                    cache.searchVersions(id, idFilter, true)
                            )
                                    .named(NElements.of().ofUplet("searchVersionInCache",NElements.of().ofString(getName())))
                                    .build());
                }
//                Iterator<NutsId> p = null;
//                try {
//                    p = searchVersionsImpl2(id, idFilter, session);
//                    if (p != null) {
//                        all.add(p);
//                    }
//                } catch (Exception ex) {
//                    //ignore....
//                }
            } catch (NNotFoundException ex) {
//                errors.append(ex).append(" \n");
            }
        }

        try {
            NIterator<NId> p = null;
            p = searchVersionsCore(id, idFilter, fetchMode);
            if (p != null) {
                all.add(
                        NIteratorBuilder.of(p)
                                .named(NElements.of().ofUplet("searchVersionInCore",NElements.of().ofString(getName())))
                                .build());
            }
        } catch (NNotFoundException ex) {
            //ignore error
        } catch (Exception ex) {
            _LOGOP().level(Level.FINEST).verb(NLogVerb.FAIL).error(ex)
                    .log(NMsg.ofJ("search versions error : {0}", ex));
            //ignore....
        }
        NIterator<NId> namedNutIdIterator = NIteratorBuilder.ofConcat(all).distinct(
                NFunction.of(NId::getLongName).withDesc(NEDesc.of("getLongName"))).build();

        if (namedNutIdIterator == null) {
            namedNutIdIterator = NIteratorBuilder.emptyIterator();
        }
        return NIteratorBuilder.of(
                mirroring.searchVersionsImpl_appendMirrors(namedNutIdIterator, id, idFilter, fetchMode)
        )
                .named(NElements.of().ofUplet("searchVersion",NElements.of().ofString(getName())))
                .build();

    }

    @Override
    public final NPath fetchContentImpl(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        if (fetchMode != NFetchMode.REMOTE) {
            NPath c = lib.fetchContentImpl(id);
            if (c != null) {
                return c;
            }
        }
        NSession session = getWorkspace().currentSession();
        if (cache.isReadEnabled() && session.isCached()) {
            NPath c = cache.fetchContentImpl(id);
            if (c != null) {
                return c;
            }
        }

        RuntimeException mirrorsEx = null;
        NPath c = null;
        Callable<NOptional<NPath>> nOptionalCallable = () -> {
            if (cache.isWriteEnabled()) {
                NPath c2 = null;
                RuntimeException impl2Ex = null;
                NPath cachePath = cache.getLongIdLocalFile(id);
                try {
                    c2 = fetchContentCore(id, descriptor, fetchMode);
                } catch (RuntimeException ex) {
                    impl2Ex = ex;
                }
                if (c2 != null) {
                    NCp.of().from(c2).to(cachePath).run();
                    return NOptional.of(cachePath.setUserCache(true).setUserTemporary(false));
                } else if (impl2Ex instanceof NNotFoundException) {
                    return NOptional.ofNamedEmpty(id.toString());
                } else if (impl2Ex != null) {
                    return NOptional.ofError(() -> NMsg.ofC("nuts content not found %s", id), impl2Ex);
                } else {
                    return NOptional.ofError(() -> NMsg.ofC("nuts content not found %s", id), new NNotFoundException(id));
                }
            } else {
                NPath c2 = null;
                RuntimeException impl2Ex = null;
                try {
                    c2 = fetchContentCore(id, descriptor, fetchMode);
                } catch (RuntimeException ex) {
                    impl2Ex = ex;
                }
                if (c2 != null) {
                    return NOptional.of(c2);
                } else if (impl2Ex instanceof NNotFoundException) {
                    return NOptional.ofNamedEmpty(id.toString());
                } else if (impl2Ex != null) {
                    return NOptional.ofError(() -> NMsg.ofC("nuts content not found %s", id), impl2Ex);
                } else {
                    return NOptional.ofError(() -> NMsg.ofC("nuts content not found %s", id), new NNotFoundException(id));
                }
            }
        };
        NOptional<NPath> res = null;
        try {
            boolean lockEnabled = isLockEnabled();
            res = lockEnabled ?
                    NLock.ofId(id.builder().setFaceContent().build()).callWith(nOptionalCallable)
                    : nOptionalCallable.call();
        } catch (Exception e) {
            res=NOptional.ofError(() -> NMsg.ofC("nuts content not found %s", id), e);
        }

        if (res.isPresent()) {
            return res.get();
        }
        try {
            c = mirroring.fetchContent(id, descriptor, fetchMode);
        } catch (RuntimeException ex) {
            mirrorsEx = ex;
        }
        if (c != null) {
            return c;
        }
        if (res.getError() != null) {
            if (res.getError() instanceof NNotFoundException) {
                throw (RuntimeException) res.getError();
            }
            throw new NNotFoundException(id, res.getError());
        }
        if (mirrorsEx != null) {
            if (mirrorsEx instanceof NNotFoundException) {
                throw mirrorsEx;
            }
            throw new NNotFoundException(id, mirrorsEx);
        }
        throw new NNotFoundException(id);
    }

    @Override
    public final NIterator<NId> searchImpl(final NIdFilter filter, NFetchMode fetchMode) {
        NSession session = getWorkspace().currentSession();
        List<NPath> basePaths = CommonRootsByPathHelper.resolveRootPaths(filter);
        List<NId> baseIds = CommonRootsByIdHelper.resolveRootPaths(filter);
        List<NIterator<? extends NId>> li = new ArrayList<>();
        for (NPath basePath : basePaths) {
            if (fetchMode != NFetchMode.REMOTE) {
                if (basePath.getName().equals("*")) {
                    li.add(lib.findInFolder(basePath.getParent(), filter, Integer.MAX_VALUE));
                } else {
                    li.add(lib.findInFolder(basePath, filter, 2));
                }
            }
            if (cache.isReadEnabled() && session.isCached()) {
                if (basePath.getName().equals("*")) {
                    li.add(cache.findInFolder(basePath.getParent(), filter, Integer.MAX_VALUE));
                } else {
                    li.add(cache.findInFolder(basePath, filter, 2));
                }
            }
        }
        NIterator<NId> p = null;
        try {
            p = searchCore(filter, basePaths.toArray(new NPath[0]), baseIds.toArray(new NId[0]), fetchMode);
        } catch (NNotFoundException ex) {
            //ignore....
        } catch (Exception ex) {
            //ignore....
            _LOGOP().level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJ("search latest versions error : {0}", ex));
        }
        if (p != null) {
            li.add(p);
        }
        return mirroring.search(NIteratorBuilder.ofConcat(li).distinct(
                NFunction.of(NId::getLongName).withDesc(NEDesc.of("getLongName"))
        ).build(), filter, fetchMode);
    }

    protected boolean isAllowedOverrideArtifact(NId id) {
        return true;
    }

    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        return null;
    }

    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode) {
        return null;
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode) {
        return null;
    }

    public NPath fetchContentCore(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        return null;
    }

    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode) {
        return null;
    }

    public void updateStatisticsImpl() {

    }

    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode) {
        String groups = config().getGroups();
        if (NBlankable.isBlank(groups)) {
            return true;
        }
        return GlobUtils.ofExact(groups).matcher(id.getGroupId()).matches();
    }

    @Override
    public final NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode) {
        if (id.getVersion().isBlank() && filter == null) {
            NId bestId = lib.searchLatestVersion(id, filter);
            NId c1 = null;
            if (cache.isReadEnabled()) {
                c1 = cache.searchLatestVersion(id, filter);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            }
            try {
                c1 = searchLatestVersionCore(id, filter, fetchMode);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            } catch (NNotFoundException | NFetchModeNotSupportedException ex) {
                //ignore
            } catch (Exception ex) {
                _LOGOP().level(Level.SEVERE).error(ex)
                        .log(NMsg.ofJ("search latest versions error : {0}", ex));
                //ignore....
            }
            return mirroring.searchLatestVersion(bestId, id, filter, fetchMode);
        }
        return super.searchLatestVersion(id, filter, fetchMode);
    }

    @Override
    public final NUpdateRepositoryStatsCmd updateStatistics() {
        return new AbstractNUpdateRepositoryStatsCmd(this) {
            @Override
            public NUpdateRepositoryStatsCmd run() {
                lib.reindexFolder();
                if (cache.isWriteEnabled()) {
                    cache.reindexFolder();
                }
                updateStatisticsImpl();
                return this;
            }
        };
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
