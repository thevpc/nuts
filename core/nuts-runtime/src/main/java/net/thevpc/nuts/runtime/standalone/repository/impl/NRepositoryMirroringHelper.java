/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.event.DefaultNContentEvent;
import net.thevpc.nuts.runtime.standalone.id.filter.NSearchIdByDescriptor;
import net.thevpc.nuts.runtime.standalone.id.util.CoreNIdUtils;
import net.thevpc.nuts.runtime.standalone.repository.NRepositoryHelper;
import net.thevpc.nuts.runtime.standalone.repository.cmd.NRepositorySupportedAction;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NDeployRepositoryCommand;
import net.thevpc.nuts.spi.NPushRepositoryCommand;
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

    protected NIterator<NId> searchVersionsImpl_appendMirrors(NIterator<NId> namedNutIdIterator, NId id, NIdFilter idFilter, NFetchMode fetchMode, NSession session) {
        if (!session.isTransitive()) {
            return namedNutIdIterator;
        }
        List<NIterator<? extends NId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (repo.config().setSession(session).isSupportedMirroring()) {
            for (NRepository repo : repo.config().setSession(session).getMirrors()) {
                NSpeedQualifier sup = NSpeedQualifier.UNAVAILABLE;
                try {
                    sup = NRepositoryHelper.getSupportSpeedLevel(repo, NRepositorySupportedAction.SEARCH, id, fetchMode, session.isTransitive(), session);
                } catch (Exception ex) {
                    //                errors.append(CoreStringUtils.exceptionToString(ex)).append("\n");
                }
                if (sup != NSpeedQualifier.UNAVAILABLE) {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
                    list.add(
                            IteratorBuilder.of(repoSPI.searchVersions().setId(id).setFilter(idFilter).setSession(session)
                                            .setFetchMode(fetchMode)
                                            .getResult(), session)
                                    .named("searchInMirror(" + repo.getName() + ")")
                                    .safeIgnore()
                                    .build()
                    );
                }
            }
        }
        return IteratorUtils.concat(list);
    }

    protected NPath fetchContent(NId id, NDescriptor descriptor, String localPath, NFetchMode fetchMode, NSession session) {
        NPath cacheContent = cache.getLongIdLocalFile(id, session);
        NRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NRepository mirror : rconfig.setSession(session).getMirrors()) {
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(mirror);
                    NPath c = repoSPI.fetchContent().setId(id).setDescriptor(descriptor).setLocalPath(cacheContent.toString()).setSession(session)
                            .setFetchMode(fetchMode)
                            .getResult();
                    if (c != null) {
                        if (localPath != null) {
                            NCp.of(session)
                                    .from(c).to(NPath.of(localPath,session)).addOptions(NPathOption.SAFE).run();
                        } else {
                            return c;
                        }
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

    protected String getIdFilename(NId id, NSession session) {
        return NRepositoryExt.of(repo).getIdFilename(id, session);
    }

    protected NDescriptor fetchDescriptorImplInMirrors(NId id, NFetchMode fetchMode, NSession session) {
        String idFilename = getIdFilename(id, session);
        NPath versionFolder = cache.getLongIdLocalFolder(id, session);
        NRepositoryConfigManager rconf = repo.config().setSession(session);
        if (session.isTransitive() && rconf.isSupportedMirroring()) {
            for (NRepository remote : rconf.getMirrors()) {
                NDescriptor nutsDescriptor = null;
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setSession(session).setFetchMode(fetchMode).getResult();
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
                    nutsDescriptor.formatter(session).print(goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    public NIterator<NId> search(NIterator<NId> li, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        NRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (!session.isTransitive() || !rconfig.isSupportedMirroring()) {
            return li;
        }
        List<NIterator<? extends NId>> all = new ArrayList<>();
        all.add(li);
        for (NRepository remote : rconfig.setSession(session).getMirrors()) {
            NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(remote);
            all.add(IteratorUtils.safeIgnore(
                    repoSPI.search().setFilter(filter).setSession(session).setFetchMode(fetchMode).getResult(), session
            ));
        }
        return IteratorUtils.concat(all);

    }

    public void push(NPushRepositoryCommand cmd) {
        NSession session = cmd.getSession();
        NSessionUtils.checkSession(getWorkspace(), session);
        NId id = cmd.getId();
        String repository = cmd.getRepository();
        NSession nonTransitiveSession = session.copy().setTransitive(false);
        NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(repo);
        NDescriptor desc = repoSPI.fetchDescriptor().setId(id).setSession(nonTransitiveSession).setFetchMode(NFetchMode.LOCAL).getResult();
        NPath local = repoSPI.fetchContent().setId(id).setSession(nonTransitiveSession).setFetchMode(NFetchMode.LOCAL).getResult();
        if (local == null) {
            throw new NNotFoundException(session, id);
        }
        if (!repo.config().setSession(session).isSupportedMirroring()) {
            throw new NPushException(session, id, NMsg.ofC("unable to push %s. no repository found.", id == null ? "<null>" : id));
        }
        NRepository repo = this.repo;
        if (NBlankable.isBlank(repository)) {
            List<NRepository> all = new ArrayList<>();
            for (NRepository remote : repo.config().setSession(session).getMirrors()) {
                NSpeedQualifier lvl = NRepositoryHelper.getSupportSpeedLevel(remote, NRepositorySupportedAction.DEPLOY, id, NFetchMode.LOCAL, false, session);
                if (lvl != NSpeedQualifier.UNAVAILABLE) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NPushException(session, id, NMsg.ofC("unable to push %s. no repository found.", id == null ? "<null>" : id));
            } else if (all.size() > 1) {
                throw new NPushException(session, id,
                        NMsg.ofC("unable to perform push for %s. at least two Repositories (%s) provides the same nuts %s",
                                id,
                                all.stream().map(NRepository::getName).collect(Collectors.joining(",")),
                                id
                        )
                );
            }
            repo = all.get(0);
        } else {
            repo = this.repo.config().setSession(session.copy().setTransitive(false)).getMirror(repository);
        }
        if (repo != null) {
            NId effId = CoreNIdUtils.createContentFaceId(id.builder().setPropertiesQuery("").build(), desc,session)
//                    .setAlternative(NutsUtilStrings.trim(desc.getAlternative()))
                    ;
            NDeployRepositoryCommand dep = repoSPI.deploy()
                    .setId(effId)
                    .setContent(local)
                    .setDescriptor(desc)
//                    .setOffline(cmd.isOffline())
                    //.setFetchMode(NutsFetchMode.LOCAL)
                    .setSession(session)
                    .run();
            NRepositoryHelper.of(repo).events().fireOnPush(new DefaultNContentEvent(
                    local, dep, session, repo));
        } else {
            throw new NRepositoryNotFoundException(session, repository);
        }
    }

    public NId searchLatestVersion(NId bestId, NId id, NIdFilter filter, NFetchMode fetchMode, NSession session) {
        NRepositoryConfigManager rconfig = repo.config().setSession(session);
        if (session.isTransitive() && rconfig.isSupportedMirroring()) {
            for (NRepository remote : rconfig.setSession(session).getMirrors()) {
                NDescriptor nutsDescriptor = null;
                try {
                    NRepositorySPI repoSPI = NWorkspaceUtils.of(session).repoSPI(remote);
                    nutsDescriptor = repoSPI.fetchDescriptor().setId(id).setSession(session).setFetchMode(fetchMode).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    if (filter == null || filter.acceptSearchId(new NSearchIdByDescriptor(nutsDescriptor), session)) {
//                        NutsId id2 = C                                oreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                        NWorkspaceExt dws = NWorkspaceExt.of(getWorkspace());
                        NId id2 = dws.resolveEffectiveId(nutsDescriptor, session).builder().setFaceDescriptor().build();
                        NPath localNutFile = cache.getLongIdLocalFile(id2, session);
                        nutsDescriptor.formatter(session).print(localNutFile);
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
