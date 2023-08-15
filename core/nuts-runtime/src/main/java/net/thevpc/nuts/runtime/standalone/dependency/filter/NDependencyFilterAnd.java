package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NDependencyFilterAnd extends AbstractDependencyFilter implements NComplexExpressionString {

    private final NDependencyFilter[] all;

    public NDependencyFilterAnd(NSession session, NDependencyFilter... all) {
        super(session, NFilterOp.AND);
        this.all = all;
    }

    @Override
    public boolean acceptDependency(NId from, NDependency dependency, NSession session) {
        for (NDependencyFilter nDependencyFilter : all) {
            if (nDependencyFilter != null && !nDependencyFilter.acceptDependency(from, dependency, session)) {
                return false;
            }
        }
        return true;
    }

    public NDependencyFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(getSession(), NDependencyFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrAnd(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
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
        final NDependencyFilterAnd other = (NDependencyFilterAnd) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
