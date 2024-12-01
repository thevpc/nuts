/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceConfigMain;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NPredicates;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class NRepositoryRegistryHelper {

    private Map<String, RepoAndRef> repositoriesByName = new LinkedHashMap<>();
    private Map<String, RepoAndRef> repositoriesByUuid = new LinkedHashMap<>();
    private NWorkspace workspace;

    public NRepositoryRegistryHelper(NWorkspace workspace) {
        this.workspace = workspace;
    }

    public NRepository[] getRepositories() {
        return repositoriesByUuid.values().stream().map(x -> x.repo).filter(NPredicates.nonNull())
                .toArray(NRepository[]::new);
    }

    public NRepositoryRef[] getRepositoryRefs() {
        return repositoriesByUuid.values().stream().map(x -> x.ref).filter(NPredicates.nonNull())
                .toArray(NRepositoryRef[]::new);
    }

    public void addRepository(NRepository repository) {
        if (repository == null) {
            return;
        }
        NRepositoryRef repositoryRef = repository.config().getRepositoryRef();
        String uuid = repository.getUuid();
        String name = repository.getName();
        if (name == null) {
            return;
        }
        RepoAndRef ii = null;
        if (uuid != null) {
            ii = repositoriesByUuid.get(uuid);
            if (ii != null) {
                throw new NIllegalArgumentException(
                        NMsg.ofC("repository with the same uuid already exists % / %s", ii.repo.getUuid(), ii.repo.getName())
                );
            }
        }
        ii = repositoriesByName.get(name);
        if (ii != null) {
            throw new NIllegalArgumentException(NMsg.ofC("repository with the same name already exists %s / %s", ii.repo.getUuid(), ii.repo.getName()));
        }
        if (!name.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid repository name %s", name));
        }
        RepoAndRef rr = new RepoAndRef(repository);
        rr.ref = repositoryRef;
        repositoriesByName.put(name, rr);
        if (uuid != null) {
            repositoriesByUuid.put(uuid, rr);
        }
        NWorkspaceConfigMain m = ((NWorkspaceExt) workspace).getModel().configModel.getStoreModelMain();
        List<NRepositoryRef> repositoriesRefs = m.getRepositories();
        if (repositoriesRefs == null) {
            repositoriesRefs = new ArrayList<>();
            m.setRepositories(repositoriesRefs);
        }
        repositoriesRefs.add(repositoryRef);
        m.setRepositories(repositoriesRefs);
    }

    //    public void addRepositoryRef(NutsRepositoryRef repositoryRef) {
//        RepoAndRef ii = repositoriesByName.get(repositoryRef.getName());
//        if (ii != null) {
//            throw new NutsIllegalArgumentException(ws, "Repository with the same name already exists " + ii.repo.getUuid() + "/" + ii.repo.config().name());
//        }
//        if (!repositoryRef.getName().matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
//            throw new NutsIllegalArgumentException(ws, "Invalid repository name " + repositoryRef.getName());
//        }
//        RepoAndRef rr = new RepoAndRef(null);
//        rr.ref = repositoryRef;
//        repositoriesByName.put(repositoryRef.getName(), rr);
//    }
//
//    public void addRepository(NutsRepository repository) {
//        RepoAndRef ii = repositoriesByUuid.get(repository.config().uuid());
//        if (ii != null) {
//            throw new NutsIllegalArgumentException(ws, "Repository with the same uuid already exists " + ii.repo.getUuid() + "/" + ii.repo.config().name());
//        }
//        ii = repositoriesByName.get(repository.config().name());
//        if (ii != null && ii.repo != null) {
//            throw new NutsIllegalArgumentException(ws, "Repository with the same name already exists " + ii.repo.getUuid() + "/" + ii.repo.config().name());
//        }
//        if (!repository.config().name().matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
//            throw new NutsIllegalArgumentException(ws, "Invalid repository name " + repository.config().name());
//        }
//        if (ii != null) {
//            ii.repo = repository;
//        } else {
//            RepoAndRef rr = new RepoAndRef(repository);
//            repositoriesByName.put(repository.config().name(), rr);
//            repositoriesByUuid.put(repository.config().uuid(), rr);
//        }
//    }
    public NRepository removeRepository(String repository) {
        final NRepository r = findRepository(repository);
        if (r != null) {
            repositoriesByName.remove(r.getName());
            repositoriesByUuid.remove(r.getUuid());
            NWorkspaceConfigMain m = NWorkspaceExt.of(workspace).getModel().configModel.getStoreModelMain();
            List<NRepositoryRef> repositoriesRefs = m.getRepositories();
            if (repositoriesRefs != null) {
                repositoriesRefs.removeIf(x -> x.getName().equals(r.getName()));
            }
            return r;
        }
        return null;
    }

    private RepoAndRef findRepositoryAndRefById(String repositoryNameOrId) {
        if (!NBlankable.isBlank(repositoryNameOrId)) {
            RepoAndRef y = repositoriesByUuid.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
        }
        return null;
    }

    private RepoAndRef findRepositoryAndRef(String repositoryNameOrId) {
        if (!NBlankable.isBlank(repositoryNameOrId)) {
            RepoAndRef y = repositoriesByUuid.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
            y = repositoriesByName.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
        }
        return null;
    }

    private RepoAndRef findRepositoryAndRefByName(String repositoryNameOrId) {
        if (!NBlankable.isBlank(repositoryNameOrId)) {
            RepoAndRef y = repositoriesByName.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
        }
        return null;
    }

    public NRepository findRepositoryById(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRefById(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NRepository findRepositoryByName(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRefByName(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NRepository findRepository(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRef(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NRepositoryRef findRepositoryRef(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRef(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.ref;
    }

    private static class RepoAndRef {

        NRepositoryRef ref;
        NRepository repo;

        public RepoAndRef(NRepository repo) {
            this.repo = repo;
        }

    }
}
