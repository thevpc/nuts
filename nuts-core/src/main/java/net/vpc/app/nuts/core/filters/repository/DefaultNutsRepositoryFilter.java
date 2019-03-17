package net.vpc.app.nuts.core.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.Objects;
import java.util.Set;

public class DefaultNutsRepositoryFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private final Set<String> repos;

    public DefaultNutsRepositoryFilter(Set<String> repos) {
        this.repos = repos;
    }

    @Override
    public boolean accept(NutsRepository repository) {
        return repos.isEmpty()
                || repos.contains(repository.getUuid())
                || repos.contains(repository.getName())
                ;
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (repos.isEmpty()) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "DefaultNutsRepositoryFilter{" + "repos=" + repos + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 41 * hash + Objects.hashCode(this.repos);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNutsRepositoryFilter other = (DefaultNutsRepositoryFilter) obj;
        if (!Objects.equals(this.repos, other.repos)) {
            return false;
        }
        return true;
    }
    
    
}
