package net.vpc.app.nuts.runtime.filters.repository;

import java.util.Collection;
import java.util.HashSet;

import net.vpc.app.nuts.NutsFilterOp;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

public class DefaultNutsRepositoryFilter extends AbstractNutsFilter implements NutsRepositoryFilter, Simplifiable<NutsRepositoryFilter> {

    private final Set<String> exactRepos;
    private final Set<Pattern> wildcardRepos;

    public DefaultNutsRepositoryFilter(NutsWorkspace ws,Collection<String> exactRepos) {
        super(ws, NutsFilterOp.CUSTOM);
        this.exactRepos = new HashSet<>();
        this.wildcardRepos = new HashSet<>();
        for (String repo : exactRepos) {
            if (!CoreStringUtils.isBlank(repo)) {
                if(repo.indexOf('*')>0) {
                    this.wildcardRepos.add(Pattern.compile(CoreStringUtils.simpexpToRegexp(repo)));
                }else if(repo.length()>2 && repo.startsWith("/") && repo.endsWith("/")){
                    this.wildcardRepos.add(Pattern.compile(repo.substring(1,repo.length()-1)));
                }else {
                    this.exactRepos.add(repo);
                }
            }
        }
    }

    @Override
    public boolean acceptRepository(NutsRepository repository) {
        if(exactRepos.isEmpty() && wildcardRepos.isEmpty()){
            return true;
        }
        if(exactRepos.contains(repository.getUuid())
                || exactRepos.contains(repository.getName())){
            return true;
        }
        for (Pattern wildcardRepo : wildcardRepos) {
            if(wildcardRepo.matcher(repository.getName()).matches()){
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsRepositoryFilter simplify() {
        if(exactRepos.isEmpty() && wildcardRepos.isEmpty()){
            return getWorkspace().repos().filter().always();
        }
        return this;
    }

    @Override
    public String toString() {
        return "DefaultNutsRepositoryFilter{" + "repos=" + exactRepos + " ; " + wildcardRepos + '}';
    }

    @Override
    public int hashCode() {
        int hash = getClass().getName().hashCode();
        hash = 41 * hash + Objects.hashCode(this.exactRepos);
        hash = 41 * hash + Objects.hashCode(this.wildcardRepos);
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
        if (!Objects.equals(this.exactRepos, other.exactRepos)) {
            return false;
        }
        if (!Objects.equals(this.wildcardRepos, other.wildcardRepos)) {
            return false;
        }
        return true;
    }

}
