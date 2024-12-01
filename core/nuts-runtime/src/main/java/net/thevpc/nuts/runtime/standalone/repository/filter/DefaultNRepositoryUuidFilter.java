package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;

public class DefaultNRepositoryUuidFilter extends AbstractRepositoryFilter{

    private final Set<String> exactRepos;

    public DefaultNRepositoryUuidFilter(NWorkspace workspace, Collection<String> exactRepos) {
        super(workspace, NFilterOp.CUSTOM);
        this.exactRepos = new HashSet<>(
                exactRepos==null?new ArrayList<>() :
                        exactRepos.stream().map(x-> NStringUtils.trimToNull(x))
                                .filter(x->x!=null).collect(Collectors.toList())
        );
    }

    @Override
    public boolean acceptRepository(NRepository repository) {
        return exactRepos.contains(repository.getUuid());
    }

    @Override
    public NRepositoryFilter simplify() {
        if(exactRepos.isEmpty()){
            return NRepositoryFilters.of().always();
        }
        return this;
    }

    @Override
    public String toString() {
        return "DefaultNRepositoryUuidFilter{" + "repos=" + exactRepos  + '}';
    }

    @Override
    public int hashCode() {
        int hash = getClass().getName().hashCode();
        hash = 41 * hash + Objects.hashCode(this.exactRepos);
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
        final DefaultNRepositoryUuidFilter other = (DefaultNRepositoryUuidFilter) obj;
        if (!Objects.equals(this.exactRepos, other.exactRepos)) {
            return false;
        }
        return true;
    }

}
