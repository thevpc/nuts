package net.thevpc.nuts.runtime.filters.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.filters.AbstractNutsFilter;
import net.thevpc.nuts.runtime.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.stream.Collectors;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

public class NutsDependencyFilterAnd extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterAnd(NutsWorkspace ws, NutsDependencyFilter... all) {
        super(ws,NutsFilterOp.AND);
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
        return CoreNutsUtils.simplifyFilterAnd(getWorkspace(),NutsDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.join(" And ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
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

    public NutsFilter[] getSubFilters() {
        return all;
    }
}
