package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NIdFilterNone extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter>, NExprIdFilter {

    private NIdFilter[] children;

    public NIdFilterNone(NIdFilter... all) {
        super(NFilterOp.NOT);
        List<NIdFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NIdFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.children = valid.toArray(new NIdFilter[0]);
    }

    public NIdFilter[] getChildren() {
        return Arrays.copyOf(children, children.length);
    }

    @Override
    public boolean acceptId(NId id) {
        if (children.length == 0) {
            return true;
        }
        for (NIdFilter filter : children) {
            if (filter.acceptId(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NIdFilter simplify() {
        return CoreFilterUtils.simplifyFilterNone( NIdFilter.class,this,children);
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
        for (NIdFilter id : children) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof NExprIdFilter) {
                NExprIdFilter b = (NExprIdFilter) id;
                String expr = b.toExpr();
                if (NBlankable.isBlank(expr)) {
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
        return CoreStringUtils.trueOrNone(Arrays.stream(children).map(NComplexExpressionString::toString).collect(Collectors.toList()));
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
        final NIdFilterNone other = (NIdFilterNone) obj;
        if (!Arrays.deepEquals(this.children, other.children)) {
            return false;
        }
        return true;
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(children);
    }
}
