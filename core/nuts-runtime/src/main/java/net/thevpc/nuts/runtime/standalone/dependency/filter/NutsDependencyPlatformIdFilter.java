package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.*;
import java.util.stream.Collectors;


public class NutsDependencyPlatformIdFilter extends AbstractDependencyFilter  {

    private Set<NutsId> accepted = new HashSet<>();

    public NutsDependencyPlatformIdFilter(NutsSession session) {
        super(session, NutsFilterOp.CUSTOM);
    }

    private NutsDependencyPlatformIdFilter(NutsSession session, Collection<NutsId> accepted) {
        super(session, NutsFilterOp.CUSTOM);
        this.accepted = new LinkedHashSet<>(accepted);
    }

    public NutsDependencyPlatformIdFilter add(Collection<NutsId> os) {
        LinkedHashSet<NutsId> s2 = new LinkedHashSet<>(accepted);
        s2.addAll(os);
        return new NutsDependencyPlatformIdFilter(getSession(), s2);
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        String[] current = NutsStream.of(dependency.getCondition().getPlatform(),session).filterNonBlank().toArray(String[]::new);
        if(current.length==0 || accepted.isEmpty()){
            return true;
        }
        for (NutsId nutsId : accepted) {
            if(CoreFilterUtils.matchesPlatform(nutsId.toString(),current,session)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return accepted.isEmpty() ? "true" : "os in (" + accepted.stream().map(Object::toString).collect(Collectors.joining(", ")) + ')';
    }

    @Override
    public NutsDependencyFilter simplify() {
        return accepted.isEmpty() ? NutsDependencyFilters.of(getSession()).always() : this;
    }
}
