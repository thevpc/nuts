package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyPlatformIdFilter extends AbstractDependencyFilter  {

    private Set<NId> accepted = new HashSet<>();

    public NDependencyPlatformIdFilter(NSession session) {
        super(session, NFilterOp.CUSTOM);
    }

    private NDependencyPlatformIdFilter(NSession session, Collection<NId> accepted) {
        super(session, NFilterOp.CUSTOM);
        this.accepted = new LinkedHashSet<>(accepted);
    }

    public NDependencyPlatformIdFilter add(Collection<NId> os) {
        LinkedHashSet<NId> s2 = new LinkedHashSet<>(accepted);
        s2.addAll(os);
        return new NDependencyPlatformIdFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
        List<String> current = NStream.of(dependency.getCondition().getPlatform(),session).filterNonBlank().toList();
        if(current.size()==0 || accepted.isEmpty()){
            return true;
        }
        for (NId nutsId : accepted) {
            if(CoreFilterUtils.matchesPlatform(nutsId.toString(),current,session)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrEqOrIn("platform",
                        accepted.stream().map(x -> x.toString()).collect(Collectors.toList())
                );
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of(getSession()).always() : this;
    }
}
