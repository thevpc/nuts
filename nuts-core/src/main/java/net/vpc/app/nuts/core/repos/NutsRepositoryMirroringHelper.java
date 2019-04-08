/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.repos;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsContent;
import net.vpc.app.nuts.NutsContentEvent;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsNotFoundException;
import net.vpc.app.nuts.NutsPushCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryAmbiguousException;
import net.vpc.app.nuts.NutsRepositoryDeploymentOptions;
import net.vpc.app.nuts.NutsRepositoryNotFoundException;
import net.vpc.app.nuts.NutsRepositorySession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.DefaultNutsRepositoryDeploymentOptions;
import net.vpc.app.nuts.core.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.bundledlibs.util.IteratorUtils;
import net.vpc.app.nuts.core.util.bundledlibs.util.LazyIterator;

/**
 *
 * @author vpc
 */
public class NutsRepositoryMirroringHelper {

    private AbstractNutsRepository repo;
    protected NutsRepositoryFolderHelper cache;

    public NutsRepositoryMirroringHelper(AbstractNutsRepository repo, NutsRepositoryFolderHelper cache) {
        this.repo = repo;
        this.cache = cache;
    }

    protected Iterator<NutsId> findVersionsImpl_appendMirrors(Iterator<NutsId> namedNutIdIterator, NutsId id, NutsIdFilter idFilter, NutsRepositorySession session) {
        if (!session.isTransitive()) {
            return namedNutIdIterator;
        }
        List<Iterator<NutsId>> list = new ArrayList<>();
        list.add(namedNutIdIterator);
        if (repo.config().isSupportedMirroring()) {
            for (NutsRepository repo : repo.config().getMirrors()) {
                int sup = 0;
                try {
                    sup = repo.config().getFindSupportLevel(id, session.getFetchMode(), session.isTransitive());
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

    protected NutsContent fetchContent(NutsId id, Path localPath, NutsRepositorySession session) {
        Path cacheContent = cache.getIdLocalFile(id);
        if (session.isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository mirror : repo.config().getMirrors()) {
                try {
                    NutsContent c = mirror.fetchContent(id, cacheContent, session);
                    if (c != null) {
                        if (localPath != null) {
                            getWorkspace().io().copy().from(c.getPath()).to(localPath).safeCopy().run();
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
        return repo.getIdFilename(id);
    }

    protected NutsDescriptor fetchDescriptorImplInMirrors(NutsId id, NutsRepositorySession session) {
        String idFilename = getIdFilename(id);
        Path versionFolder = cache.getLocalVersionFolder(id);
        if (session.isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository remote : repo.config().getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor(id, session);
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
                    String a = nutsDescriptor.getAlternative();
                    Path goodFile = null;
                    if (CoreStringUtils.isBlank(a) || a.equals(NutsConstants.QueryKeys.ALTERNATIVE_DEFAULT_VALUE)) {
                        goodFile = versionFolder.resolve(idFilename);
                    } else {
                        goodFile = versionFolder.resolve(CoreStringUtils.trim(a)).resolve(idFilename);
                    }
                    getWorkspace().formatter().createDescriptorFormat().setPretty(true).format(nutsDescriptor, goodFile);
                    return nutsDescriptor;
                }
            }
        }
        return null;
    }

    public Iterator<NutsId> find(Iterator<NutsId> li, NutsIdFilter filter, NutsRepositorySession session) {
        if (!session.isTransitive() || !repo.config().isSupportedMirroring()) {
            return li;
        }
        List<Iterator<NutsId>> all = new ArrayList<>();
        all.add(li);
        for (NutsRepository remote : repo.config().getMirrors()) {
            all.add(IteratorUtils.safeIgnore(new LazyIterator<NutsId>() {

                @Override
                public Iterator<NutsId> iterator() {
                    return remote.find(filter, session);
                }

            }));
        }
        return IteratorUtils.concat(all);

    }

    public void push(NutsId id, NutsPushCommand options, NutsRepositorySession session) {
        NutsRepositorySession nonTransitiveSession = session.copy().setTransitive(false);
        NutsDescriptor desc = repo.fetchDescriptor(id, nonTransitiveSession);
        NutsContent local = repo.fetchContent(id, null, nonTransitiveSession);
        if (local == null) {
            throw new NutsNotFoundException(id);
        }
        if (!repo.config().isSupportedMirroring()) {
            throw new NutsRepositoryNotFoundException("Not Repo for pushing " + id);
        }
        NutsRepository repo = null;
        if (options.getRepository() == null) {
            List<NutsRepository> all = new ArrayList<>();
            for (NutsRepository remote : repo.config().getMirrors()) {
                int lvl = remote.config().getDeploymentSupportLevel(id, session.getFetchMode() != NutsFetchMode.REMOTE, false);
                if (lvl > 0) {
                    all.add(remote);
                }
            }
            if (all.isEmpty()) {
                throw new NutsRepositoryNotFoundException("Not Repo for pushing " + id);
            } else if (all.size() > 1) {
                throw new NutsRepositoryAmbiguousException("Unable to perform push. Two Repositories provides the same nuts " + id);
            }
            repo = all.get(0);
        } else {
            repo = repo.config().getMirror(options.getRepository());
        }
        if (repo != null) {
            NutsId effId = getWorkspace().config().createComponentFaceId(id.unsetQuery(), desc).setAlternative(CoreStringUtils.trim(desc.getAlternative()));
            NutsRepositoryDeploymentOptions dep = new DefaultNutsRepositoryDeploymentOptions()
                    .setId(effId)
                    .setContent(local.getPath())
                    .setDescriptor(desc)
                    .setRepository(repo.config().getName())
                    .setTrace(options.isTrace())
                    .setForce(options.isForce())
                    .setTransitive(true)
                    .setOffline(options.isOffline());
            repo.deploy(dep, session);
            ((AbstractNutsRepository) repo).fireOnPush(new NutsContentEvent(local.getPath(), dep, getWorkspace(), repo));
        } else {
            throw new NutsRepositoryNotFoundException(options.getRepository());
        }
    }

    public NutsId findLatestVersion(NutsId bestId, NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if (session.isTransitive() && repo.config().isSupportedMirroring()) {
            for (NutsRepository remote : repo.config().getMirrors()) {
                NutsDescriptor nutsDescriptor = null;
                try {
                    nutsDescriptor = remote.fetchDescriptor(id, session);
                } catch (Exception ex) {
                    //ignore
                }
                if (nutsDescriptor != null) {
//                        NutsId id2 = C                                oreNutsUtils.createComponentFaceId(getWorkspace().resolveEffectiveId(nutsDescriptor,session),nutsDescriptor,null);
                    NutsWorkspaceExt dws = NutsWorkspaceExt.of(getWorkspace());
                    NutsId id2 = dws.resolveEffectiveId(nutsDescriptor, getWorkspace().fetch()
                            .setCached(session.isCached())
                            .setSession(session.getSession())
                            .setTransitive(session.isTransitive())
                            .setIndexed(session.isIndexed())).setFaceDescriptor();
                    Path localNutFile = cache.getIdLocalFile(id2);
                    getWorkspace().formatter().createDescriptorFormat().setPretty(true).format(nutsDescriptor, localNutFile);
                    if (bestId == null || id2.getVersion().compareTo(bestId.getVersion()) > 0) {
                        bestId = id2;
                    }
                }
            }
        }
        return bestId;
    }

}
