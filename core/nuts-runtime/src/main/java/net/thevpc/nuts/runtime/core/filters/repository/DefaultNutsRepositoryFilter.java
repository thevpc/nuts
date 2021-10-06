package net.thevpc.nuts.runtime.core.filters.repository;

import java.util.Collection;
import java.util.HashSet;

import net.thevpc.nuts.*;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.bundles.string.GlobUtils;

public class DefaultNutsRepositoryFilter extends AbstractRepositoryFilter{

    private final Set<String> exactRepos;
    private final Set<Pattern> wildcardRepos;

    public DefaultNutsRepositoryFilter(NutsSession session,Collection<String> exactRepos) {
        super(session, NutsFilterOp.CUSTOM);
        this.exactRepos = new HashSet<>();
        this.wildcardRepos = new HashSet<>();
        for (String repo : exactRepos) {
            if (!NutsBlankable.isBlank(repo)) {
                if(repo.indexOf('*')>0) {
                    this.wildcardRepos.add(GlobUtils.ofExact(repo));
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
            return getSession().repos().filter().always();
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
