package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.spi.base.NIdFilterBase;
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
import java.util.Objects;
import java.util.stream.Collectors;

public class NIdFilterOr extends NIdFilterBase implements NIdFilter, NSimplifiable<NIdFilter>, NExprIdFilter, NComplexExpressionString {

    private final NIdFilter[] children;

    public NIdFilterOr(NIdFilter... all) {
        super(NFilterOp.OR);
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
                return true;
            }
        }
        return false;
    }

    @Override
    public NIdFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(NIdFilter.class,this,children);
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
                sb.append(" || ");
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
        return
                CoreStringUtils.trueOrOr(Arrays.stream(children).map(NComplexExpressionString::toString).collect(Collectors.toList()))
                ;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NIdFilterOr that = (NIdFilterOr) o;
        return Objects.deepEquals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(children));
    }

    public List<NFilter> subFilters() {
        return Arrays.asList(children);
    }
}
