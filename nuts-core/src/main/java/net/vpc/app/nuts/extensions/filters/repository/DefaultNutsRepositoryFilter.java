package net.vpc.app.nuts.extensions.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.Set;

public class DefaultNutsRepositoryFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private final Set<String> repos;

    public DefaultNutsRepositoryFilter(Set<String> repos) {
        this.repos = repos;
    }

    @Override
    public boolean accept(NutsRepository repository) {
        return repos.isEmpty() || repos.contains(repository.getRepositoryId());
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (repos.isEmpty()) {
            return null;
        }
        return this;
    }
}
