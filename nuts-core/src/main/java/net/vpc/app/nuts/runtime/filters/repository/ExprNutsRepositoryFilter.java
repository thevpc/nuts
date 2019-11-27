package net.vpc.app.nuts.runtime.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Objects;
import java.util.regex.Pattern;

public class ExprNutsRepositoryFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private String repos;
    private Pattern reposPattern;

    public ExprNutsRepositoryFilter(String repos) {
        this.repos = repos;
        if (CoreStringUtils.isBlank(repos)) {
            reposPattern = Pattern.compile(".*");
            this.repos = "";
        } else {
            reposPattern = Pattern.compile(CoreStringUtils.simpexpToRegexp(repos));
        }
    }

    @Override
    public boolean accept(NutsRepository repository) {
        return repos.isEmpty()
                || reposPattern.matcher(CoreStringUtils.trim(repository.config().getName())).matches()
                || reposPattern.matcher(CoreStringUtils.trim(repository.getUuid())).matches();
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (CoreStringUtils.isBlank(repos)) {
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
