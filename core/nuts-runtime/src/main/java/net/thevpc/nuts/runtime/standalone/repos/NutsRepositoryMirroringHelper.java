/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryExt;
import net.thevpc.nuts.runtime.core.commands.repo.NutsRepositorySupportedAction;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.filters.NutsSearchIdByDescriptor;
import net.thevpc.nuts.runtime.core.repos.NutsRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.bundles.iter.LazyIterator;
import net.thevpc.nuts.runtime.core.events.DefaultNutsContentEvent;
import net.thevpc.nuts.spi.NutsDeployRepositoryCommand;
import net.thevpc.nuts.spi.NutsPushRepositoryCommand;
import net.thevpc.nuts.spi.NutsRepositorySPI;

/**
 *
 * @author thevpc
 */
public class NutsRepositoryMirroringHelper {

    private NutsRepository repo;
    protected NutsRepositoryFolderHelper cache;

    public NutsRepositoryMirroringHelper(NutsRepository repo, NutsRepositoryFolderHelper cache) {
        this.repo = repo;
        this.cache = cache;
    }

    protected Iterator<NutsId> searchVersionsImpl_appendMirrors(Iterator<NutsId> namedNutIdIterator, NutsId id, NutsIdFilter idFilter, NutsFetchMode fetchMode, NutsSession session) {
        if (!session.isTransitive()) {
            return namedNutIdIterator;
        }
        List<Iterator<? extends NutsId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (repo.config().setSession(session).isSupportedMirroring()) {
            for (NutsRepository repo : repo.config().setSession(session).getMirrors()) {
                int sup = 0;
                try {
                    sup = NutsRepositoryUtils.getSupportSpeedLevel(repo, NutsRepositorySupportedAction.SEARCH, id, fetchMode, session.isTransitive(),session);
                } catch (Exception ex) {
                    //                errors.append(CoreStringUtils.exceptionToString(ex)).append("\n");
                }
                if (sup > 0) {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
                    list.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {
                        @Override
                        public Iterator<NutsId> iterator() {
                            return repoSPI.searchVersions().setId(id).setFilter(idFilter).setSession(session)
                                    .setFetchMode(fetchMode)
                                    .getResult();
                        }
                    }));
                }
            }
        }
        return IteratorUtils.concat(list);
    }

    protected NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsSession session) {
        Path cacheContent = cache.getLongIdLocalFile(id, session);
        NutsRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NutsRepository mirror : rconfig.setSession(session).getMirrors()) {
                try {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(mirror);
                    NutsContent c = repoSPI.fetchContent().setId(id).setDescriptor(descriptor).setLocalPath(cacheContent.toString()).setSession(session)
                            .setFetchMode(fetchMode)
                            .getResult();
                    if (c != null) {
                        if (localPath != null) {
                            getWorkspace().io().copy().setSession(session)
                                    .from(c.getFilePath()).to(localPath).setSafe(true).run();
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
        return null;
    }

    public NutsWorkspace getWorkspace() {
        return repo.getWorkspace();
    }

    protected String getIdFilename(NutsId id, NutsSession session) {
        return NutsRepositoryExt.of(repo).getIdFilename(id,session);
    }

    protected NutsDescriptor fetchDescriptorImplInMirrors(NutsId id, NutsFetchMode fetchMode, NutsSession session) {
        String idFilename = getIdFilename(id, session);
        Path versionFolder = cache.getLongIdLocalFolder(id, session);
        NutsRepositoryConfigManager rconf = repo.config().setSession(session);
        if (session.isTransitive() && rconf.isSupportedMirroring()) {
            for (NutsRepository remote : rconf.getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setSession(session).setFetchMode(fetchMode).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    Path goodFile = null;
                    goodFile = versionFolder.resolve(idFilename);
//                    String a = nutsDescriptor.getAlternative();
//                    if (CoreNutsUtils.isDefaultAlternative(a)) {
//                        goodFile = versionFolder.resolve(idFilename);
//                    } else {
//                        goodFile = versionFolder.resolve(NutsUtilStrings.trim(a)).resolve(idFilename);
//                    }
                    getWorkspace().descriptor().setSession(session).formatter(nutsDescriptor).print(goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    public Iterator<NutsId> search(Iterator<NutsId> li, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        NutsRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (!session.isTransitive() || !rconfig.isSupportedMirroring()) {
            return li;
        }
        List<Iterator<? extends NutsId>> all = new ArrayList<>();
        all.add(li);
        for (NutsRepository remote : rconfig.setSession(session).getMirrors()) {
            NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(remote);
            all.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {

                @Override
                public Iterator<NutsId> iterator() {
                    return repoSPI.search().setFilter(filter).setSession(session).setFetchMode(fetchMode).getResult();
                }

            }));
        }
        return IteratorUtils.concat(all);

    }

    public void push(NutsPushRepositoryCommand cmd) {
        NutsSession session = cmd.getSession();
        NutsWorkspaceUtils.checkSession(getWorkspace(),session);
        NutsId id = cmd.getId();
        String repository = cmd.getRepository();
        NutsSession nonTransitiveSession = session.copy().setTransitive(false);
        NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(repo);
        NutsDescriptor desc = repoSPI.fetchDescriptor().setId(id).setSession(nonTransitiveSession).setFetchMode(NutsFetchMode.LOCAL).getResult();
        NutsContent local = repoSPI.fetchContent().setId(id).setSession(nonTransitiveSession).setFetchMode(NutsFetchMode.LOCAL).getResult();
        if (local == null) {
            throw new NutsNotFoundException(session, id);
        }
        if (!repo.config().setSession(session).isSupportedMirroring()) {
            throw new NutsRepositoryNotFoundException(session, "Not Repo for pushing " + id);
        }
        NutsRepository repo = this.repo;
        if (NutsBlankable.isBlank(repository)) {
            List<NutsRepository> all = new ArrayList<>();
            for (NutsRepository remote : repo.config().setSession(session).getMirrors()) {
                int lvl = NutsRepositoryUtils.getSupportSpeedLevel(remote,NutsRepositorySupportedAction.DEPLOY, id, NutsFetchMode.LOCAL, false,session);
                if (lvl > 0) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NutsRepositoryNotFoundException(session, "Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsPushException(session, id,
                        NutsMessage.cstyle("unable to perform push for %s. At least two Repositories (%s) provides the same nuts %s",
                                id,
                                all.stream().map(NutsRepository::getName).collect(Collectors.joining(",")),
                                id
                        )
                );
            }
            repo = all.get(0);
        } else {
            repo = this.repo.config().setSession(session.copy().setTransitive(false)).getMirror(repository);
        }
        if (repo != null) {
            NutsId effId = getWorkspace().config().createContentFaceId(id.builder().setProperties("").build(), desc)
//                    .setAlternative(NutsUtilStrings.trim(desc.getAlternative()))
                    ;
            NutsDeployRepositoryCommand dep = repoSPI.deploy()
                    .setId(effId)
                    .setContent(local.getFilePath())
                    .setDescriptor(desc)
//                    .setOffline(cmd.isOffline())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session)
                    .run();
            NutsRepositoryUtils.of(repo).events().fireOnPush(new DefaultNutsContentEvent(
                    local.getLocation(), dep, session, repo));
        } else {
            throw new NutsRepositoryNotFoundException(session, repository);
        }
    }

    public NutsId searchLatestVersion(NutsId bestId, NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsSession session) {
        NutsRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NutsRepository remote : rconfig.setSession(session).getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    NutsRepositorySPI repoSPI = NutsWorkspaceUtils.of(session).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setSession(session).setFetchMode(fetchMode).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    if(filter==null || filter.acceptSearchId(new NutsSearchIdByDescriptor(nutsDescriptor),session )) {
//                        NutsId id2 = C                                oreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                        NutsWorkspaceExt dws = NutsWorkspaceExt.of(getWorkspace());
                        NutsId id2 = dws.resolveEffectiveId(nutsDescriptor, session).builder().setFaceDescriptor().build();
                        Path localNutFile = cache.getLongIdLocalFile(id2, session);
                        getWorkspace().descriptor().formatter(nutsDescriptor).print(localNutFile);
                        if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                            bestId = id2;
                        }
                    }
                }
            }
        }
        return bestId;
    }

}
