package net.vpc.app.nuts.extensions.filters.repository;

import java.util.Objects;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.regex.Pattern;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

public class ExprNutsRepositoryFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private String repos;
    private Pattern reposPattern;

    public ExprNutsRepositoryFilter(String repos) {
        this.repos = repos;
        if (CoreStringUtils.isEmpty(repos)) {
            reposPattern = Pattern.compile(".*");
            this.repos = "";
        } else {
            reposPattern = Pattern.compile(CoreStringUtils.simpexpToRegexp(repos));
        }
    }

    @Override
    public boolean accept(NutsRepository repository) {
        return repos.isEmpty() || reposPattern.matcher(CoreStringUtils.trim(repository.getRepositoryId())).matches();
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (CoreStringUtils.isEmpty(repos)) {
            return null;
        }
        return this;
    }

    @Override
    public String toString() {
        return "ExprNutsRepositoryFilter{" + "repos=" + repos + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 97 * hash + Objects.hashCode(this.repos);
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
        final ExprNutsRepositoryFilter other = (ExprNutsRepositoryFilter) obj;
        if (!Objects.equals(this.repos, other.repos)) {
            return false;
        }
        return true;
    }
    
}
