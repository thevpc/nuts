package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.stream.Collectors;


public class NDependencyPlatformIdFilter extends AbstractDependencyFilter  {

    private Set<NId> accepted = new HashSet<>();

    public NDependencyPlatformIdFilter() {
        super(NFilterOp.CUSTOM);
    }

    private NDependencyPlatformIdFilter(Collection<NId> accepted) {
        super(NFilterOp.CUSTOM);
        this.accepted = new LinkedHashSet<>(accepted);
    }

    public NDependencyPlatformIdFilter add(Collection<NId> oses) {
        LinkedHashSet<NId> s2 = new LinkedHashSet<>(accepted);
        NCollections.addAllNonNull(s2, oses);
        return new NDependencyPlatformIdFilter(s2);
    }

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        List<String> current = NStream.ofIterable(dependency.getCondition().getPlatform()).filterNonBlank().toList();
        if(current.size()==0 || accepted.isEmpty()){
            return true;
        }
        for (NId nutsId : accepted) {
            if(CoreFilterUtils.matchesPlatform(nutsId.toString(),current)){
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
        return accepted.isEmpty() ? NDependencyFilters.of().always() : this;
    }
}
