/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.main.repos;

import java.util.LinkedHashMap;
import java.util.Map;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsRepository;
import net.thevpc.nuts.NutsRepositoryRef;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;

/**
 *
 * @author thevpc
 */
public class NutsRepositoryRegistryHelper {

    private Map<String, RepoAndRef> repositoriesByName = new LinkedHashMap<>();
    private Map<String, RepoAndRef> repositoriesByUuid = new LinkedHashMap<>();
    private NutsWorkspace ws;

    public NutsRepositoryRegistryHelper(NutsWorkspace ws) {
        this.ws = ws;
    }

    public NutsRepository[] getRepositories() {
        return repositoriesByUuid.values().stream().map(x -> x.repo).filter(x -> x != null)
                .toArray(NutsRepository[]::new);
    }

    public NutsRepositoryRef[] getRepositoryRefs() {
        return repositoriesByUuid.values().stream().map(x -> x.ref).filter(x -> x != null)
                .toArray(NutsRepositoryRef[]::new);
    }

    public void addRepository(NutsRepositoryRef repositoryRef, NutsRepository repository) {
        if (repositoryRef == null && repository == null) {
            return;
        }
        String uuid = repository != null ? repository.getUuid() : null;
        String name = repository != null ? repository.getName() : repositoryRef != null ? repositoryRef.getName() : null;
        if (name == null) {
            return;
        }
        RepoAndRef ii = null;
        if (uuid != null) {
            ii = repositoriesByUuid.get(uuid);
            if (ii != null) {
                throw new NutsIllegalArgumentException(ws, "repository with the same uuid already exists " + ii.repo.getUuid() + "/" + ii.repo.getName());
            }
        }
        ii = repositoriesByName.get(name);
        if (ii != null) {
            throw new NutsIllegalArgumentException(ws, "repository with the same name already exists " + ii.repo.getUuid() + "/" + ii.repo.getName());
        }
        if (!name.matches("[a-zA-Z][.a-zA-Z0-9_-]*")) {
            throw new NutsIllegalArgumentException(ws, "invalid repository name " + name);
        }
        RepoAndRef rr = new RepoAndRef(repository);
        rr.ref = repositoryRef;
        repositoriesByName.put(name, rr);
        if (uuid != null) {
            repositoriesByUuid.put(uuid, rr);
        }
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
    public NutsRepository removeRepository(String repository) {
        final NutsRepository r = findRepository(repository);
        if (r != null) {
            repositoriesByName.remove(r.getName());
            repositoriesByUuid.remove(r.getUuid());
            return r;
        }
        return null;
    }

    private RepoAndRef findRepositoryAndRefById(String repositoryNameOrId) {
        if (!CoreStringUtils.isBlank(repositoryNameOrId)) {
            RepoAndRef y = repositoriesByUuid.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
        }
        return null;
    }

    private RepoAndRef findRepositoryAndRef(String repositoryNameOrId) {
        if (!CoreStringUtils.isBlank(repositoryNameOrId)) {
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
        if (!CoreStringUtils.isBlank(repositoryNameOrId)) {
            RepoAndRef y = repositoriesByName.get(repositoryNameOrId);
            if (y != null) {
                return y;
            }
        }
        return null;
    }

    public NutsRepository findRepositoryById(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRefById(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NutsRepository findRepositoryByName(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRefByName(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NutsRepository findRepository(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRef(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.repo;
    }

    public NutsRepositoryRef findRepositoryRef(String repositoryNameOrId) {
        final RepoAndRef rr = findRepositoryAndRef(repositoryNameOrId);
        if (rr == null) {
            return null;
        }
        return rr.ref;
    }

    private static class RepoAndRef {

        NutsRepositoryRef ref;
        NutsRepository repo;

        public RepoAndRef(NutsRepository repo) {
            this.repo = repo;
        }

    }
}
