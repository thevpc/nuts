package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyOsDistIdFilter extends AbstractDependencyFilter  {

    private Set<NId> accepted = new HashSet<>();

    public NDependencyOsDistIdFilter(NWorkspace workspace) {
        super(workspace, NFilterOp.CUSTOM);
    }

    private NDependencyOsDistIdFilter(NWorkspace workspace, Collection<NId> accepted) {
        super(workspace, NFilterOp.CUSTOM);
        this.accepted = new LinkedHashSet<>(accepted);
    }

    public NDependencyOsDistIdFilter add(Collection<NId> os) {
        LinkedHashSet<NId> s2 = new LinkedHashSet<>(accepted);
        s2.addAll(os);
        return new NDependencyOsDistIdFilter(workspace, s2);
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency) {
        List<String> current = NStream.of(dependency.getCondition().getOsDist()).filterNonBlank().toList();
        if(current.size()==0 || accepted.isEmpty()){
            return true;
        }
        for (NId nutsId : accepted) {
            if(CoreFilterUtils.matchesOsDist(nutsId.toString(),current)){
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return
                CoreStringUtils.trueOrEqOrIn("os",
                        accepted.stream().map(x -> x.toString()).collect(Collectors.toList())
                )
                ;
    }

    @Override
    public NDependencyFilter simplify() {
        return accepted.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}
