package net.thevpc.nuts.runtime.standalone.repository.filter;

import java.util.*;

import net.thevpc.nuts.*;

import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.spi.NRepositoryDB;
import net.thevpc.nuts.spi.NRepositorySelectorList;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;

public class DefaultNRepositoryFilter extends AbstractRepositoryFilter{

    private final Set<String> exactRepos;
    private final Set<Pattern> wildcardRepos;

    public DefaultNRepositoryFilter(NSession session, Collection<String> exactRepos) {
        super(session, NFilterOp.CUSTOM);
        this.exactRepos = new HashSet<>();
        this.wildcardRepos = new HashSet<>();
        NRepositorySelectorList li=new NRepositorySelectorList();
        NRepositoryDB db = NRepositoryDB.of(session);
        for (String exactRepo : exactRepos) {
            li=li.merge(NRepositorySelectorList.of(exactRepo, db,session).get());
        }
        NRepositoryLocation[] input = NRepositories.of(session).getRepositories().stream()
                .map(x -> x.config().getLocation().setName(x.getName()))
                .toArray(NRepositoryLocation[]::new);
        String[] names = Arrays.stream(li.resolve(input,db)).map(NRepositoryLocation::getName).toArray(String[]::new);
        for (String repo : names) {
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
    public NRepositoryFilter simplify() {
        if(exactRepos.isEmpty() && wildcardRepos.isEmpty()){
            return NRepositories.of(getSession()).filter().always();
        }
        return this;
    }

    @Override
    public String toString() {
        return "NRepositoryFilter{" + "repos=" + exactRepos + " ; " + wildcardRepos + '}';
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
        final DefaultNRepositoryFilter other = (DefaultNRepositoryFilter) obj;
        if (!Objects.equals(this.exactRepos, other.exactRepos)) {
            return false;
        }
        if (!Objects.equals(this.wildcardRepos, other.wildcardRepos)) {
            return false;
        }
        return true;
    }

}
