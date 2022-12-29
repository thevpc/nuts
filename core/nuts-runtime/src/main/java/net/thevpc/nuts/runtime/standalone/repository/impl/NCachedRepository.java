/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.util.CommonRootsByPathHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.util.CommonRootsByIdHelper;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats.AbstractNUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NDeployRepositoryCommand;
import net.thevpc.nuts.spi.NPushRepositoryCommand;
import net.thevpc.nuts.spi.NRepositoryUndeployCommand;
import net.thevpc.nuts.spi.NUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by vpc on 1/5/17.
 */
public class NCachedRepository extends AbstractNRepositoryBase {

    protected final NRepositoryFolderHelper lib;
    protected final NRepositoryFolderHelper cache;
    private final NRepositoryMirroringHelper mirroring;
    public NLogger LOG;

    public NCachedRepository(NAddRepositoryOptions options, NSession session, NRepository parent, NSpeedQualifier speed, boolean supportedMirroring, String repositoryType, boolean supportsDeploy) {
        super(options, session, parent, speed, supportedMirroring, repositoryType,supportsDeploy);
        cache = new NRepositoryFolderHelper(this, session, config().setSession(session).getStoreLocation(NStoreLocation.CACHE), true,
                "cache", NElements.of(session).ofObject().set("repoKind", "cache").build()
        );
        lib = new NRepositoryFolderHelper(this, session, config().setSession(session).getStoreLocation(NStoreLocation.LIB), false,
                "lib", NElements.of(session).ofObject().set("repoKind", "lib").build()
        );
        mirroring = new NRepositoryMirroringHelper(this, cache);
    }

    protected NLoggerOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLogger _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLogger.of(NCachedRepository.class, session);
        }
        return LOG;
    }

    @Override
    public void pushImpl(NPushRepositoryCommand command) {
        mirroring.push(command);
    }

    @Override
    public NDescriptor deployImpl(NDeployRepositoryCommand command) {
        return lib.deploy(command, command.getSession().getConfirm());
    }

    @Override
    public final void undeployImpl(NRepositoryUndeployCommand options) {
        lib.undeploy(options);
    }

    @Override
    public NDescriptor fetchDescriptorImpl(NId id, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            NDescriptor libDesc = lib.fetchDescriptorImpl(id, session);
            if (libDesc != null) {
                return libDesc;
            }
            if (cache.isReadEnabled()) {if(session.isCached()){
                NDescriptor cacheDesc = cache.fetchDescriptorImpl(id, session);
                if (cacheDesc != null) {
                    return cacheDesc;
                }}
            }
        }
        RuntimeException mirrorsEx = null;

        NOptional<NDescriptor> res = NLocks.of(session).setSource(id.builder().setFaceDescriptor().build()).call(() -> {
            try {
                NDescriptor success = fetchDescriptorCore(id, fetchMode, session);
                if (success != null) {
                    if (cache.isWriteEnabled()) {
                        NId id0 = NWorkspaceExt.of(getWorkspace()).resolveEffectiveId(success, session);
                        if (!id0.getLongName().equals(success.getId().getLongName())) {
                            success = success.builder().setId(id0).build();
                        }
                        cache.deployDescriptor(success.getId(), success, NConfirmationMode.YES, session.copy().setConfirm(NConfirmationMode.YES));
                    }
                    return NOptional.of(success);
                } else {
                    return NOptional.ofError(session1 -> NMsg.ofCstyle("nuts descriptor not found %s",id), new NNotFoundException(session, id));
                }
            } catch (RuntimeException ex) {
                return NOptional.ofError(session1 -> NMsg.ofCstyle("nuts descriptor not found %s",id), ex);
            }
        });
        if (res.isPresent()) {
            return res.get(session);
        }
        NDescriptor m = null;
        try {
            m = mirroring.fetchDescriptorImplInMirrors(id, fetchMode, session);
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
    public final NIterator<NId> searchVersionsImpl(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {

        List<NIterator<? extends NId>> all = new ArrayList<>();
        if (fetchMode != NFetchMode.REMOTE) {
            all.add(IteratorBuilder.of(
                                    lib.searchVersions(id, idFilter, true, session),
                            session).named("searchVersionInLib(" + getName() + ")")
                            .build()

            );
        }
        if (fetchMode != NFetchMode.REMOTE) {
            try {
                if (cache.isReadEnabled()) {
                    all.add(
                            IteratorBuilder.of(
                                    cache.searchVersions(id, idFilter, true, session),
                                    session).named("searchVersionInCache(" + getName() + ")").build());
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
            p = searchVersionsCore(id, idFilter, fetchMode, session);
            if (p != null) {
                all.add(
                        IteratorBuilder.of(p, session).named("searchVersionInCore(" + getName() + ")").build());
            }
        } catch (NNotFoundException ex) {
            //ignore error
        } catch (Exception ex) {
            _LOGOP(session).level(Level.FINEST).verb(NLoggerVerb.FAIL).error(ex)
                    .log(NMsg.ofJstyle("search versions error : {0}", ex));
            //ignore....
        }
        NIterator<NId> namedNutIdIterator = IteratorBuilder.ofConcat(all, session).distinct(
                NFunction.of(NId::getLongName, "getLongName")).build();

        if (namedNutIdIterator == null) {
            namedNutIdIterator = IteratorBuilder.emptyIterator();
        }
        return IteratorBuilder.of(
                mirroring.searchVersionsImpl_appendMirrors(namedNutIdIterator, id, idFilter, fetchMode, session),
                session).named("searchVersion(" + getName() + ")").build();

    }

    @Override
    public final NPath fetchContentImpl(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        if (fetchMode != NFetchMode.REMOTE) {
            NPath c = lib.fetchContentImpl(id, localPath, session);
            if (c != null) {
                return c;
            }
        }
        if (cache.isReadEnabled() && session.isCached()) {
            NPath c = cache.fetchContentImpl(id, localPath, session);
            if (c != null) {
                return c;
            }
        }

        RuntimeException mirrorsEx = null;
        NPath c = null;
        NOptional<NPath> res = NLocks.of(session).setSource(id.builder().setFaceContent().build())
                .call(() -> {
            if (cache.isWriteEnabled()) {
                NPath cachePath = cache.getLongIdLocalFile(id, session);
                NPath c2 = fetchContentCore(id, descriptor, cachePath.toString(), fetchMode, session);
                if (c2 != null) {
                    String localPath2 = localPath;
                    //already deployed because fetchContentImpl2 is run against cachePath
//                cache.deployContent(id, c.getPath(), session);
                    if (localPath2 != null) {
                        NCp.of(session)
                                .from(cachePath).to(NPath.of(localPath2,session)).run();
                    } else {
                        localPath2 = cachePath.toString();
                    }
                    return NOptional.of(NPath.of(localPath2, session).setUserCache(true).setUserTemporary(false));
                } else {
                    return NOptional.ofError(session1 -> NMsg.ofCstyle("nuts content not found %s",id),new NNotFoundException(session, id));
                }
            } else {
                NPath c2 = null;
                RuntimeException impl2Ex = null;
                try {
                    c2 = fetchContentCore(id, descriptor, localPath, fetchMode, session);
                } catch (RuntimeException ex) {
                    impl2Ex = ex;
                }
                if (c2 != null) {
                    return NOptional.of(c2);
                } else if (impl2Ex != null) {
                    return NOptional.ofError(session1 -> NMsg.ofCstyle("nuts content not found %s",id),impl2Ex);
                } else {
                    return NOptional.ofError(session1 -> NMsg.ofCstyle("nuts content not found %s",id),new NNotFoundException(session, id));
                }
            }
        });

        if (res.isPresent()) {
            return res.get(session);
        }
        try {
            c = mirroring.fetchContent(id, descriptor, localPath, fetchMode, session);
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
            throw new NNotFoundException(session, id, res.getError());
        }
        if (mirrorsEx != null) {
            if (mirrorsEx instanceof NNotFoundException) {
                throw mirrorsEx;
            }
            throw new NNotFoundException(session, id, mirrorsEx);
        }
        throw new NNotFoundException(session, id);
    }

    @Override
    public final NIterator<NId> searchImpl(final NIdFilter filter, NFetchMode fetchMode, NSession session) {
        List<NPath> basePaths = CommonRootsByPathHelper.resolveRootPaths(filter, session);
        List<NId> baseIds = CommonRootsByIdHelper.resolveRootPaths(filter, session);
        List<NIterator<? extends NId>> li = new ArrayList<>();
        for (NPath basePath : basePaths) {
            if (fetchMode != NFetchMode.REMOTE) {
                if (basePath.getName().equals("*")) {
                    li.add(lib.findInFolder(basePath.getParent(), filter, Integer.MAX_VALUE, session));
                } else {
                    li.add(lib.findInFolder(basePath, filter, 2, session));
                }
            }
            if (cache.isReadEnabled() && session.isCached()) {
                if (basePath.getName().equals("*")) {
                    li.add(cache.findInFolder(basePath.getParent(), filter, Integer.MAX_VALUE, session));
                } else {
                    li.add(cache.findInFolder(basePath, filter, 2, session));
                }
            }
        }
        NIterator<NId> p = null;
        try {
            p = searchCore(filter, basePaths.toArray(new NPath[0]), baseIds.toArray(new NId[0]), fetchMode, session);
        } catch (NNotFoundException ex) {
            //ignore....
        } catch (Exception ex) {
            //ignore....
            _LOGOP(session).level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJstyle("search latest versions error : {0}", ex));
        }
        if (p != null) {
            li.add(p);
        }
        return mirroring.search(IteratorBuilder.ofConcat(li, session).distinct(
                NFunction.of(NId::getLongName, "getLongName")
        ).build(), filter, fetchMode, session);
    }

    protected boolean isAllowedOverrideNut(NId id) {
        return true;
    }

    public NIterator<NId> searchVersionsCore(NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public NId searchLatestVersionCore(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public NDescriptor fetchDescriptorCore(NId id, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public NPath fetchContentCore(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public NIterator<NId> searchCore(final NIdFilter filter, NPath[] basePaths, NId[] baseIds, NFetchMode fetchMode, NSession session) {
        return null;
    }

    public void updateStatistics2(NSession session) {

    }

    public boolean acceptAction(NId id, NRepositorySupportedAction supportedAction, NFetchMode mode, NSession session) {
        String groups = config().setSession(session).getGroups();
        if (NBlankable.isBlank(groups)) {
            return true;
        }
        return GlobUtils.ofExact(groups).matcher(id.getGroupId()).matches();
    }

    @Override
    public final NId searchLatestVersion(NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        if (id.getVersion().isBlank() && filter == null) {
            NId bestId = lib.searchLatestVersion(id, filter, session);
            NId c1 = null;
            if (cache.isReadEnabled()) {
                c1 = cache.searchLatestVersion(id, filter, session);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            }
            try {
                c1 = searchLatestVersionCore(id, filter, fetchMode, session);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            } catch (NNotFoundException | NFetchModeNotSupportedException ex) {
                //ignore
            } catch (Exception ex) {
                _LOGOP(session).level(Level.SEVERE).error(ex)
                        .log(NMsg.ofJstyle("search latest versions error : {0}", ex));
                //ignore....
            }
            return mirroring.searchLatestVersion(bestId, id, filter, fetchMode, session);
        }
        return super.searchLatestVersion(id, filter, fetchMode, session);
    }

    @Override
    public final NUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NUpdateRepositoryStatisticsCommand run() {
                lib.reindexFolder(getSession());
                if (cache.isWriteEnabled()) {
                    cache.reindexFolder(getSession());
                }
                updateStatistics2(getSession());
                return this;
            }
        };
    }

    @Override
    public boolean isAcceptFetchMode(NFetchMode mode, NSession session) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
