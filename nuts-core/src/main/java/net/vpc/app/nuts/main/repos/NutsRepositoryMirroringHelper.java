/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.main.repos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsPushException;
import net.vpc.app.nuts.NutsRepositoryNotFoundException;
import net.vpc.app.nuts.NutsRepositorySupportedAction;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.repos.NutsRepositoryExt;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.runtime.util.NutsRepositoryUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.iter.IteratorUtils;
import net.vpc.app.nuts.runtime.util.common.LazyIterator;
import net.vpc.app.nuts.NutsDeployRepositoryCommand;
import net.vpc.app.nuts.NutsPushRepositoryCommand;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.runtime.DefaultNutsContentEvent;

/**
 *
 * @author vpc
 */
public class NutsRepositoryMirroringHelper {

    private NutsRepository repo;
    protected NutsRepositoryFolderHelper cache;

    public NutsRepositoryMirroringHelper(NutsRepository repo, NutsRepositoryFolderHelper cache) {
        this.repo = repo;
        this.cache = cache;
    }

    protected Iterator<NutsId> searchVersionsImpl_appendMirrors(Iterator<NutsId> namedNutIdIterator, NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        if (!session.getSession().isTransitive()) {
            return namedNutIdIterator;
        }
        List<Iterator<NutsId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (repo.config().isSupportedMirroring()) {
            for (NutsRepository repo : repo.config().getMirrors()) {
                int sup = 0;
                try {
                    sup = repo.config().getSupportLevel(NutsRepositorySupportedAction.SEARCH, id, session.getFetchMode(), session.getSession().isTransitive());
                } catch (Exception ex) {
                    //                errors.append(ex.toString()).append("\n");
                }
                if (sup > 0) {
                    list.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {
                        @Override
                        public Iterator<NutsId> iterator() {
                            return repo.searchVersions().setId(id).setFilter(idFilter).setSession(session).getResult();
                        }
                    }));
                }
            }
        }
        return IteratorUtils.concat(list);
    }

    protected NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        Path cacheContent = cache.getLongNameIdLocalFile(id);
        if (session.getSession().isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repo.config().getMirrors()) {
                try {
                    NutsContent c = mirror.fetchContent().setId(id).setDescriptor(descriptor).setLocalPath(cacheContent).setSession(session).getResult();
                    if (c != null) {
                        if (localPath != null) {
                            getWorkspace().io().copy().session(session.getSession())
                                    .from(c.getPath()).to(localPath).safe().run();
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

    protected String getIdFilename(NutsId id) {
        return NutsRepositoryExt.of(repo).getIdFilename(id);
    }

    protected NutsDescriptor fetchDescriptorImplInMirrors(NutsId id, NutsRepositorySession session) {
        String idFilename = getIdFilename(id);
        Path versionFolder = cache.getLongNameIdLocalFolder(id);
        if (session.getSession().isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository remote : repo.config().getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor().setId(id).setSession(session).getResult();
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
//                        goodFile = versionFolder.resolve(CoreStringUtils.trim(a)).resolve(idFilename);
//                    }
                    getWorkspace().descriptor().value(nutsDescriptor).print(goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    public Iterator<NutsId> search(Iterator<NutsId> li, NutsIdFilter filter, NutsRepositorySession session) {
        if (!session.getSession().isTransitive() || !repo.config().isSupportedMirroring()) {
            return li;
        }
        List<Iterator<NutsId>> all = new ArrayList<>();
        all.add(li);
        for (NutsRepository remote : repo.config().getMirrors()) {
            all.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {

                @Override
                public Iterator<NutsId> iterator() {
                    return remote.search().setFilter(filter).setSession(session).getResult();
                }

            }));
        }
        return IteratorUtils.concat(all);

    }

    public void push(NutsPushRepositoryCommand cmd) {
        NutsRepositorySession session = cmd.getSession();
        NutsWorkspaceUtils.of(getWorkspace()).checkSession( session);
        NutsId id = cmd.getId();
        String repository = cmd.getRepository();
        NutsRepositorySession nonTransitiveSession = session.copy();
        nonTransitiveSession.setSession(nonTransitiveSession.getSession().setTransitive(false));
        NutsDescriptor desc = repo.fetchDescriptor().setId(id).setSession(nonTransitiveSession).getResult();
        NutsContent local = repo.fetchContent().setId(id).setSession(nonTransitiveSession).getResult();
        if (local == null) {
            throw new NutsNotFoundException(repo.getWorkspace(), id);
        }
        if (!repo.config().isSupportedMirroring()) {
            throw new NutsRepositoryNotFoundException(repo.getWorkspace(), "Not Repo for pushing " + id);
        }
        NutsRepository repo = null;
        if (CoreStringUtils.isBlank(repository)) {
            List<NutsRepository> all = new ArrayList<>();
            for (NutsRepository remote : repo.config().getMirrors()) {
                int lvl = remote.config().getSupportLevel(NutsRepositorySupportedAction.DEPLOY, id, session.getFetchMode(), false);
                if (lvl > 0) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NutsRepositoryNotFoundException(repo.getWorkspace(), "Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsPushException(repo.getWorkspace(), id,
                        "Unable to perform push for " + id + ". Alteast Two Repositories ("
                        + all.stream().map(x -> x.config().name()).collect(Collectors.joining(","))
                        + ") provides the same nuts " + id
                );
            }
            repo = all.get(0);
        } else {
            repo = this.repo.config().getMirror(repository, false);
        }
        if (repo != null) {
            NutsId effId = getWorkspace().config().createContentFaceId(id.builder().setProperties("").build(), desc)
//                    .setAlternative(CoreStringUtils.trim(desc.getAlternative()))
                    ;
            NutsDeployRepositoryCommand dep = repo.deploy()
                    .setId(effId)
                    .setContent(local.getPath())
                    .setDescriptor(desc)
//                    .setOffline(cmd.isOffline())
                    .setSession(session)
                    .run();
            NutsRepositoryUtils.of(repo).events().fireOnPush(new DefaultNutsContentEvent(local.getPath(), dep, session.getSession(), repo));
        } else {
            throw new NutsRepositoryNotFoundException(repo.getWorkspace(), repository);
        }
    }

    public NutsId searchLatestVersion(NutsId bestId, NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (session.getSession().isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository remote : repo.config().getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor().setId(id).setSession(session).getResult();
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
//                        NutsId id2 = C                                oreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                    NutsWorkspaceExt dws = NutsWorkspaceExt.of(getWorkspace());
                    NutsId id2 = dws.resolveEffectiveId(nutsDescriptor,session.getSession()).builder().setFaceDescriptor().build();
                    Path localNutFile = cache.getLongNameIdLocalFile(id2);
                    getWorkspace().descriptor().value(nutsDescriptor).print(localNutFile);
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

}
