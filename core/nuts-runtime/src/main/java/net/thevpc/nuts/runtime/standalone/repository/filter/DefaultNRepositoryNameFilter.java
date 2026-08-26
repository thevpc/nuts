package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

import java.util.*;
import java.util.regex.Pattern;

public class DefaultNRepositoryNameFilter extends AbstractRepositoryFilter{

    private final Set<String> exactRepos;
    private final Set<Pattern> wildcardRepos;

    public DefaultNRepositoryNameFilter(Collection<String> exactRepos) {
        super(NFilterOp.CUSTOM);
        this.exactRepos = new HashSet<>();
        this.wildcardRepos = new HashSet<>();
        for (String repo : (exactRepos==null?new ArrayList<String>():exactRepos)) {
            if (!NBlankable.isBlank(repo)) {
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
    public boolean acceptRepository(NRepository repository) {
        if(exactRepos.isEmpty() && wildcardRepos.isEmpty()){
            return true;
        }
        if(exactRepos.contains(repository.uuid())
                || exactRepos.contains(repository.name())){
            return true;
        }
        for (Pattern wildcardRepo : wildcardRepos) {
            if(wildcardRepo.matcher(repository.name()).matches()){
                return true;
            }
        }
        return false;
    }

    @Override
    public NRepositoryFilter simplify() {
        if(exactRepos.isEmpty() && wildcardRepos.isEmpty()){
            return NRepositoryFilter.ofAlways();
        }
        return this;
    }

    @Override
    public String toString() {
        return "NRepositoryFilter{" + "repos=" + exactRepos + " ; " + wildcardRepos + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNRepositoryNameFilter that = (DefaultNRepositoryNameFilter) o;
        return Objects.equals(exactRepos, that.exactRepos) && Objects.equals(wildcardRepos, that.wildcardRepos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), exactRepos, wildcardRepos);
    }
}
