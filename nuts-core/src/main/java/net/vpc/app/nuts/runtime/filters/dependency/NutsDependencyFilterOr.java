package net.vpc.app.nuts.runtime.filters.dependency;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

public class NutsDependencyFilterOr extends AbstractNutsFilter implements NutsDependencyFilter, Simplifiable<NutsDependencyFilter> {

    private final NutsDependencyFilter[] all;

    public NutsDependencyFilterOr(NutsWorkspace ws,NutsDependencyFilter... all) {
        super(ws,NutsFilterOp.OR);
        this.all = all;
    }

    @Override
    public boolean acceptDependency(NutsId from, NutsDependency dependency, NutsSession session) {
        boolean one = false;
        for (NutsDependencyFilter nutsDependencyFilter : all) {
            if (nutsDependencyFilter != null) {
                one = true;
                if (nutsDependencyFilter.acceptDependency(from, dependency, session)) {
                    return true;
                }
            }
        }
        return one ? false : true;
    }

    @Override
    public NutsDependencyFilter simplify() {
        return CoreNutsUtils.simplifyFilterOr(getWorkspace(),NutsDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Arrays.deepHashCode(this.all);
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
        final NutsDependencyFilterOr other = (NutsDependencyFilterOr) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    public NutsFilter[] getSubFilters() {
        return all;
    }
}
