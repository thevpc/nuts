package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.Simplifiable;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsIdFilterAnd extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {

    private NutsIdFilter[] children;

    public NutsIdFilterAnd(NutsSession session, NutsIdFilter... all) {
        super(session, NutsFilterOp.AND);
        List<NutsIdFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsIdFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.children = valid.toArray(new NutsIdFilter[0]);
    }

    public NutsIdFilter[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        if (children.length == 0) {
            return true;
        }
        for (NutsIdFilter filter : children) {
            if (!filter.acceptId(id, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsIdFilter simplify() {
        return CoreNutsUtils.simplifyFilterAnd(getSession(),NutsIdFilter.class,this,children);
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (children.length == 0) {
            return "true";
        }
        if (children.length > 1) {
            sb.append("(");
        }
        for (NutsIdFilter id : children) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof NutsScriptAwareIdFilter) {
                NutsScriptAwareIdFilter b = (NutsScriptAwareIdFilter) id;
                String expr = b.toJsNutsIdFilterExpr();
                if (NutsBlankable.isBlank(expr)) {
                    return null;
                }
                sb.append("(").append(expr).append("')");
            } else {
                return null;
            }
        }
        if (children.length > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return String.join(" and ", Arrays.asList(children).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.children);
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
        final NutsIdFilterAnd other = (NutsIdFilterAnd) obj;
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    public NutsFilter[] getSubFilters() {
        return children;
    }
}
