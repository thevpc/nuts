package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsDependencyFilterAnd extends AbstractDependencyFilter{

    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterAnd(NutsSession session, NutsDependencyFilter... all) {
        super(session,NutsFilterOp.AND);
        this.all = all;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if (nutsDependencyFilter != null && !nutsDependencyFilter.acceptDependency(from, dependency, session)) {
                return false;
            }
        }
        return true;
    }

    public NutsDependencyFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(getSession(),NutsDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return String.join(" and ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Arrays.deepHashCode(this.all);
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
        final NutsDependencyFilterAnd other = (NutsDependencyFilterAnd) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    public List<NutsFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
