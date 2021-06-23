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
 *
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
package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.common.SuccessFailResult;
import net.thevpc.nuts.runtime.standalone.repocommands.AbstractNutsUpdateRepositoryStatisticsCommand;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.bundles.io.CommonRootsHelper;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositoryUndeployCommand;
import net.thevpc.nuts.spi.NutsUpdateRepositoryStatisticsCommand;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import net.thevpc.nuts.runtime.bundles.string.GlobUtils;

/**
 * Created by vpc on 1/5/17.
 */
public class NutsCachedRepository extends AbstractNutsRepositoryBase {

    public NutsLogger LOG;

    protected final NutsRepositoryFolderHelper lib;
    protected final NutsRepositoryFolderHelper cache;
    private final NutsRepositoryMirroringHelper mirroring;

    public NutsCachedRepository(NutsAddRepositoryOptions options, NutsSession session, NutsRepository parent, int speed, boolean supportedMirroring, String repositoryType) {
        super(options, session, parent, speed, supportedMirroring, repositoryType);
        cache = new NutsRepositoryFolderHelper(this, workspace, Paths.get(config().setSession(session).getStoreLocation(NutsStoreLocation.CACHE)), true);
        lib = new NutsRepositoryFolderHelper(this, workspace, Paths.get(config().setSession(session).getStoreLocation(NutsStoreLocation.LIB)), false);
        mirroring = new NutsRepositoryMirroringHelper(this, cache);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.getWorkspace().log().of(NutsCachedRepository.class);
        }
        return LOG;
    }

    @Override
    public NutsDescriptor fetchDescriptorImpl(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
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
        RuntimeException mirrorsEx = null;

        SuccessFailResult<NutsDescriptor, RuntimeException> res = session.getWorkspace().concurrent().lock().source(id.builder().setFaceDescriptor().build()).call(() -> {
            try {
                NutsDescriptor success = fetchDescriptorCore(id, fetchMode, session);
                if (success != null) {
                    if (cache.isWriteEnabled()) {
                        NutsId id0 = NutsWorkspaceExt.of(getWorkspace()).resolveEffectiveId(success, session);
                        if (!id0.getLongName().equals(success.getId().getLongName())) {
                            success = success.builder().setId(id0).build();
                        }
                        cache.deployDescriptor(success.getId(), success, NutsConfirmationMode.YES, session.copy().setConfirm(NutsConfirmationMode.YES));
                    }
                    return SuccessFailResult.success(success);
                } else {
                    return SuccessFailResult.fail(new NutsNotFoundException(session, id));
                }
            } catch (RuntimeException ex) {
                return SuccessFailResult.fail(ex);
            }
        });
        if (res.getSuccess() != null) {
            return res.getSuccess();
        }
        NutsDescriptor m = null;
        try {
            m = mirroring.fetchDescriptorImplInMirrors(id, fetchMode, session);
        } catch (RuntimeException ex) {
            mirrorsEx = ex;
        }
        if (m != null) {
            return m;
        }
        if (res.getFail() != null) {
            throw res.getFail();
        }
        if (mirrorsEx != null) {
            throw mirrorsEx;
        }
        return m;
    }

    @Override
    public NutsDescriptor deployImpl(NutsDeployRepositoryCommand command) {
        return lib.deploy(command, NutsConfirmationMode.YES);
    }

    @Override
    public void pushImpl(NutsPushRepositoryCommand command) {
        mirroring.push(command);
    }

    @Override
    public final Iterator<NutsId> searchImpl(final NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        List<CommonRootsHelper.PathBase> roots = CommonRootsHelper.resolveRootPaths(filter);
        List<Iterator<NutsId>> li = new ArrayList<>();
        List<String> rootStrings = new ArrayList<>();
        for (CommonRootsHelper.PathBase root : roots) {
            li.add(lib.findInFolder(Paths.get(root.getName()), filter, root.isDeep() ? Integer.MAX_VALUE : 2, session));
            if (cache.isReadEnabled() && session.isCached()) {
                li.add(cache.findInFolder(Paths.get(root.getName()), filter, root.isDeep() ? Integer.MAX_VALUE : 2, session));
            }
            if (root.isDeep()) {
                rootStrings.add(root.getName() + "/*");
            } else {
                rootStrings.add(root.getName());
            }
        }
        Iterator<NutsId> p = null;
        try {
            p = searchCore(filter, rootStrings.toArray(new String[0]), fetchMode, session);
        } catch (NutsNotFoundException ex) {
            //ignore....
        } catch (Exception ex) {
            //ignore....
            _LOGOP(session).level(Level.SEVERE).error(ex).log("search latest versions error : {0}", ex);
        }
        if (p != null) {
            li.add(p);
        }
        return mirroring.search(IteratorBuilder.ofList(li).distinct(NutsId::getLongName).build(), filter, fetchMode, session);
    }

    @Override
    public final NutsContent fetchContentImpl(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
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

        RuntimeException mirrorsEx = null;
        NutsContent c = null;
        SuccessFailResult<NutsContent, RuntimeException> res = session.getWorkspace().concurrent().lock().source(id.builder().setFaceContent().build()).call(() -> {
            if (cache.isWriteEnabled()) {
                Path cachePath = cache.getLongNameIdLocalFile(id, session);
                NutsContent c2 = fetchContentCore(id, descriptor, cachePath.toString(), fetchMode, session);
                if (c2 != null) {
                    String localPath2 = localPath;
                    //already deployed because fetchContentImpl2 is run against cachePath
//                cache.deployContent(id, c.getPath(), session);
                    if (localPath2 != null) {
                        getWorkspace().io().copy()
                                .setSession(session)
                                .from(cachePath).to(localPath2).run();
                    } else {
                        localPath2 = cachePath.toString();
                    }
                    return SuccessFailResult.success(new NutsDefaultContent(
                            session.getWorkspace().io().path(localPath2), true, false));
                } else {
                    return SuccessFailResult.fail(new NutsNotFoundException(session, id));
                }
            } else {
                NutsContent c2 = null;
                RuntimeException impl2Ex = null;
                try {
                    c2 = fetchContentCore(id, descriptor, localPath, fetchMode, session);
                } catch (RuntimeException ex) {
                    impl2Ex = ex;
                }
                if (c2 != null) {
                    return SuccessFailResult.success(c2);
                } else if (impl2Ex != null) {
                    return SuccessFailResult.fail(impl2Ex);
                } else {
                    return SuccessFailResult.fail(new NutsNotFoundException(session, id));
                }
            }
        });

        if (res.getSuccess() != null) {
            return res.getSuccess();
        }
        try {
            c = mirroring.fetchContent(id, descriptor, localPath, fetchMode, session);
        } catch (RuntimeException ex) {
            mirrorsEx = ex;
        }
        if (c != null) {
            return c;
        }
        if (res.getFail() != null) {
            if (res.getFail() instanceof NutsNotFoundException) {
                throw res.getFail();
            }
            throw new NutsNotFoundException(session, id, res.getFail());
        }
        if (mirrorsEx != null) {
            if (mirrorsEx instanceof NutsNotFoundException) {
                throw mirrorsEx;
            }
            throw new NutsNotFoundException(session, id, mirrorsEx);
        }
        throw new NutsNotFoundException(session, id);
    }

    protected boolean isAllowedOverrideNut(NutsId id) {
        return true;
    }

    @Override
    public final void undeployImpl(NutsRepositoryUndeployCommand options) {
        lib.undeploy(options);
    }

    @Override
    public final Iterator<NutsId> searchVersionsImpl(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {

        List<Iterator<NutsId>> all = new ArrayList<>();
        if (fetchMode != NutsFetchMode.REMOTE) {
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
            p = searchVersionsCore(id, idFilter, fetchMode, session);
            if (p != null) {
                all.add(p);
            }
        } catch (NutsNotFoundException ex) {
            //ignore error
        } catch (Exception ex) {
            _LOGOP(session).level(Level.SEVERE).error(ex).log("search versions error : {0}", ex);
            //ignore....
        }
        Iterator<NutsId> namedNutIdIterator = IteratorBuilder.ofList(all).distinct(NutsId::getLongName).build();

        if (namedNutIdIterator == null) {
            namedNutIdIterator = IteratorUtils.emptyIterator();
        }
        return mirroring.searchVersionsImpl_appendMirrors(namedNutIdIterator, id, idFilter, fetchMode, session);

    }

    @Override
    public final NutsId searchLatestVersion(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
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
                c1 = searchLatestVersionCore(id, filter, fetchMode, session);
                if (bestId == null || (c1 != null && c1.getVersion().compareTo(bestId.getVersion()) > 0)) {
                    bestId = c1;
                }
            } catch (NutsNotFoundException | NutsFetchModeNotSupportedException ex) {
                //ignore
            } catch (Exception ex) {
                _LOGOP(session).level(Level.SEVERE).error(ex).log("search latest versions error : {0}", ex);
                //ignore....
            }
            return mirroring.searchLatestVersion(bestId, id, filter, fetchMode, session);
        }
        return super.searchLatestVersion(id, filter, fetchMode, session);
    }

    @Override
    public final NutsUpdateRepositoryStatisticsCommand updateStatistics() {
        return new AbstractNutsUpdateRepositoryStatisticsCommand(this) {
            @Override
            public NutsUpdateRepositoryStatisticsCommand run() {
                lib.reindexFolder(getSession());
                if (cache.isWriteEnabled()) {
                    cache.reindexFolder(getSession());
                }
                updateStatistics2(getSession());
                return this;
            }
        };
    }

    public Iterator<NutsId> searchVersionsCore(NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public NutsId searchLatestVersionCore(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public NutsDescriptor fetchDescriptorCore(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public NutsContent fetchContentCore(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public Iterator<NutsId> searchCore(final NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsSession session) {
        return null;
    }

    public void updateStatistics2(NutsSession session) {

    }

    public boolean acceptAction(NutsId id, NutsRepositorySupportedAction supportedAction, NutsFetchMode mode, NutsSession session) {
        String groups = config().setSession(session).getGroups();
        if (CoreStringUtils.isBlank(groups)) {
            return true;
        }
        return GlobUtils.ofExact(groups).matcher(id.getGroupId()).matches();
    }

    @Override
    public boolean isAcceptFetchMode(NutsFetchMode mode, NutsSession session) {
        return true;
    }

    @Override
    public boolean isRemote() {
        return true;
    }
}
