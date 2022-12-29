package net.thevpc.nuts.runtime.standalone.version.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NVersionFilterNone extends AbstractVersionFilter implements NExprIdFilter {

    private NVersionFilter[] all;

    public NVersionFilterNone(NSession session, NVersionFilter... all) {
        super(session, NFilterOp.NOT);
        List<NVersionFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NVersionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NVersionFilter[0]);
    }

    @Override
    public boolean acceptVersion(NVersion id, NSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NVersionFilter filter : all) {
            if (filter.acceptVersion(id, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NVersionFilter simplify() {
        return CoreFilterUtils.simplifyFilterNone(getSession(), NVersionFilter.class,this,all);
    }

    public String toExpr() {
        StringBuilder sb = new StringBuilder();
        if (all.length == 0) {
            return "true";
        }
        if (all.length > 1) {
            sb.append("(");
        }
        for (NVersionFilter id : all) {
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
        if (all.length > 0) {
            sb.append(")");
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + Arrays.deepHashCode(this.all);
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
        final NVersionFilterNone other = (NVersionFilterNone) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Not("+String.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()))+")";
    }
    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }

}
