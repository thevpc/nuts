/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.event.DefaultNContentEvent;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdByDescriptor;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.lib.common.iter.IteratorBuilder;
import net.thevpc.nuts.lib.common.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NDeployRepositoryCmd;
import net.thevpc.nuts.spi.NPushRepositoryCmd;
import net.thevpc.nuts.spi.NRepositorySPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NRepositoryMirroringHelper {

    private final NRepository repo;
    protected NRepositoryFolderHelper cache;

    public NRepositoryMirroringHelper(NRepository repo, NRepositoryFolderHelper cache) {
        this.repo = repo;
        this.cache = cache;
    }

    protected NIterator<NId> searchVersionsImpl_appendMirrors(NIterator<NId> namedNutIdIterator, NId id, NIdFilter idFilter, NFetchMode fetchMode) {
        NSession session = repo.getWorkspace().currentSession();
        if (!session.isTransitive()) {
            return namedNutIdIterator;
        }
        List<NIterator<? extends NId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (repo.config().isSupportedMirroring()) {
            for (NRepository repo : repo.config().getMirrors()) {
                NSpeedQualifier sup = NSpeedQualifier.UNAVAILABLE;
                try {
                    sup = NRepositoryHelper.getSupportSpeedLevel(repo, NRepositorySupportedAction.SEARCH, id, fetchMode, session.isTransitive(), session);
                } catch (Exception ex) {
                    //                errors.append(CoreStringUtils.exceptionToString(ex)).append("\n");
                }
                if (sup != NSpeedQualifier.UNAVAILABLE) {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(repo);
                    list.add(
                            IteratorBuilder.of(repoSPI.searchVersions().setId(id).setFilter(idFilter)
                                            .setFetchMode(fetchMode)
                                            .getResult())
                                    .named("searchInMirror(" + repo.getName() + ")")
                                    .safeIgnore()
                                    .build()
                    );
                }
            }
        }
        return IteratorUtils.concat(list);
    }

    protected NPath fetchContent(NId id, NDescriptor descriptor, NFetchMode fetchMode) {
        NPath cacheContent = cache.getLongIdLocalFile(id);
        NRepositoryConfigManager rconfig = repo.config();
        NWorkspace workspace = repo.getWorkspace();
        NSession session = workspace.currentSession();
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NRepository mirror : rconfig.getMirrors()) {
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(workspace).repoSPI(mirror);
                    NPath c = repoSPI.fetchContent().setId(id).setDescriptor(descriptor)
                            .setFetchMode(fetchMode)
                            .getResult();
                    if (c != null) {
                        return c;
                    }
                } catch (NNotFoundException ex) {
                    //ignore!
                }
            }
        }
        return null;
    }

    public NWorkspace getWorkspace() {
        return repo.getWorkspace();
    }

    protected String getIdFilename(NId id) {
        return NRepositoryExt.of(repo).getIdFilename(id);
    }

    protected NDescriptor fetchDescriptorImplInMirrors(NId id, NFetchMode fetchMode) {
        String idFilename = getIdFilename(id);
        NWorkspace workspace = repo.getWorkspace();
        NSession session = workspace.currentSession();
        NPath versionFolder = cache.getLongIdLocalFolder(id, session);
        NRepositoryConfigManager rconf = repo.config();
        if (session.isTransitive() && rconf.isSupportedMirroring()) {
            for (NRepository remote : rconf.getMirrors()) {
                NDescriptor nutsDescriptor = null;
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(workspace).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setFetchMode(fetchMode).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    NPath goodFile = null;
                    goodFile = versionFolder.resolve(idFilename);
//                    String a = nutsDescriptor.getAlternative();
//                    if (CoreNutsUtils.isDefaultAlternative(a)) {
//                        goodFile = versionFolder.resolve(idFilename);
//                    } else {
//                        goodFile = versionFolder.resolve(NutsUtilStrings.trim(a)).resolve(idFilename);
//                    }
                    nutsDescriptor.formatter().print(goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    public NIterator<NId> search(NIterator<NId> li, NIdFilter filter, NFetchMode fetchMode) {
        NRepositoryConfigManager rconfig = repo.config();
        NSession session = repo.getWorkspace().currentSession();
        if (!session.isTransitive() || !rconfig.isSupportedMirroring()) {
            return li;
        }
        List<NIterator<? extends NId>> all = new ArrayList<>();
        all.add(li);
        for (NRepository remote : rconfig.getMirrors()) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(remote);
            all.add(IteratorUtils.safeIgnore(
                    repoSPI.search().setFilter(filter).setFetchMode(fetchMode).getResult()
            ));
        }
        return IteratorUtils.concat(all);

    }

    public void push(NPushRepositoryCmd cmd) {
        NId id = cmd.getId();
        String repository = cmd.getRepository();
        NSession session = getWorkspace().currentSession();
        NSession nonTransitiveSession = session.copy().setTransitive(false);

        NDescriptor desc = nonTransitiveSession.callWith(() -> NWorkspaceUtils.of(getWorkspace()).repoSPI(repo).fetchDescriptor().setId(id).setFetchMode(NFetchMode.LOCAL).getResult());
        NPath local = nonTransitiveSession.callWith(() -> NWorkspaceUtils.of(getWorkspace()).repoSPI(repo).fetchContent().setId(id).setFetchMode(NFetchMode.LOCAL).getResult());
        if (local == null) {
            throw new NNotFoundException(id);
        }
        if (!repo.config().isSupportedMirroring()) {
            throw new NPushException(id, NMsg.ofC("unable to push %s. no repository found.", id == null ? "<null>" : id));
        }
        NRepository repo = this.repo;
        if (NBlankable.isBlank(repository)) {
            List<NRepository> all = new ArrayList<>();
            for (NRepository remote : repo.config().getMirrors()) {
                NSpeedQualifier lvl = NRepositoryHelper.getSupportSpeedLevel(remote, NRepositorySupportedAction.DEPLOY, id, NFetchMode.LOCAL, false, session);
                if (lvl != NSpeedQualifier.UNAVAILABLE) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NPushException(id, NMsg.ofC("unable to push %s. no repository found.", id == null ? "<null>" : id));
            } else if (all.size() > 1) {
                throw new NPushException(id,
                        NMsg.ofC("unable to perform push for %s. at least two Repositories (%s) provides the same nuts %s",
                                id,
                                all.stream().map(NRepository::getName).collect(Collectors.joining(",")),
                                id
                        )
                );
            }
            repo = all.get(0);
        } else {
            repo = nonTransitiveSession.callWith(() -> {
                return this.repo.config().getMirror(repository);
            });
        }
        if (repo != null) {
            NId effId = CoreNIdUtils.createContentFaceId(id.builder().setPropertiesQuery("").build(), desc)
//                    .setAlternative(NutsUtilStrings.trim(desc.getAlternative()))
                    ;
            NDeployRepositoryCmd dep = NWorkspaceUtils.of(getWorkspace()).repoSPI(repo).deploy()
                    .setId(effId)
                    .setContent(local)
                    .setDescriptor(desc)
//                    .setOffline(cmd.isOffline())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .run();
            NRepositoryHelper.of(repo).events().fireOnPush(new DefaultNContentEvent(local, dep, session, repo));
        } else {
            throw new NRepositoryNotFoundException(repository);
        }
    }

    public NId searchLatestVersion(NId bestId, NId id, NIdFilter filter, NFetchMode fetchMode) {
        NRepositoryConfigManager rconfig = repo.config();
        NSession session = repo.getWorkspace().currentSession();
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NRepository remote : rconfig.getMirrors()) {
                NDescriptor nutsDescriptor = null;
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(getWorkspace()).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setFetchMode(fetchMode).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    if (filter == null || filter.acceptSearchId(new NSearchIdByDescriptor(nutsDescriptor))) {
//                        NutsId id2 = C                                oreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                        NWorkspaceExt dws = NWorkspaceExt.of();
                        NId id2 = dws.resolveEffectiveId(nutsDescriptor).builder().setFaceDescriptor().build();
                        NPath localNutFile = cache.getLongIdLocalFile(id2);
                        nutsDescriptor.formatter().print(localNutFile);
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
