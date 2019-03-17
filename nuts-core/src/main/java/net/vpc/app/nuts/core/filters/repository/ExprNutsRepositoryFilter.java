package net.vpc.app.nuts.core.filters.repository;

import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.Simplifiable;
import net.vpc.common.strings.StringUtils;

import java.util.Objects;
import java.util.regex.Pattern;

public class ExprNutsRepositoryFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private String repos;
    private Pattern reposPattern;

    public ExprNutsRepositoryFilter(String repos) {
        this.repos = repos;
        if (StringUtils.isEmpty(repos)) {
            reposPattern = Pattern.compile(".*");
            this.repos = "";
        } else {
            reposPattern = Pattern.compile(CoreStringUtils.simpexpToRegexp(repos));
        }
    }

    @Override
    public boolean accept(NutsRepository repository) {
        return repos.isEmpty()
                || reposPattern.matcher(StringUtils.trim(repository.getName())).matches()
                || reposPattern.matcher(StringUtils.trim(repository.getUuid())).matches();
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if (StringUtils.isEmpty(repos)) {
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
