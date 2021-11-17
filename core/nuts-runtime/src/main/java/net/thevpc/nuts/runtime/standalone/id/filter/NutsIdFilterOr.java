package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsIdFilterOr extends AbstractIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsExprIdFilter {

    private final NutsIdFilter[] children;

    public NutsIdFilterOr(NutsSession session, NutsIdFilter... all) {
        super(session, NutsFilterOp.OR);
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
            if (filter.acceptId(id, session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsIdFilter simplify() {
        return CoreNutsUtils.simplifyFilterOr(getSession(),NutsIdFilter.class,this,children);
    }

    @Override
    public String toExpr() {
        StringBuilder sb = new StringBuilder();
        if (children.length == 0) {
            return "true";
        }
        if (children.length > 1) {
            sb.append("(");
        }
        for (NutsIdFilter id : children) {
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            if (id instanceof NutsExprIdFilter) {
                NutsExprIdFilter b = (NutsExprIdFilter) id;
                String expr = b.toExpr();
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
        return String.join(" Or ", Arrays.asList(children).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Arrays.deepHashCode(this.children);
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
        final NutsIdFilterOr other = (NutsIdFilterOr) obj;
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    public NutsFilter[] getSubFilters() {
        return children;
    }
}
