package net.vpc.app.nuts.extensions.filters.id;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

public class NutsIdFilterAnd implements NutsIdFilter, Simplifiable<NutsIdFilter>, JsNutsIdFilter {

    private NutsIdFilter[] all;

    public NutsIdFilterAnd(NutsIdFilter... all) {
        List<NutsIdFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsIdFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsIdFilter[valid.size()]);
    }

    @Override
    public boolean accept(NutsId id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsIdFilter filter : all) {
            if (!filter.accept(id)) {
                return false;
            }
        }
        return true;
    }

    public NutsIdFilter simplify() {
        NutsIdFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsIdFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsIdFilterAnd(newValues);
        }
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (all.length == 0) {
            return "true";
        }
        if (all.length > 1) {
            sb.append("(");
        }
        for (NutsIdFilter id : all) {
            if (sb.length() > 0) {
                sb.append(" && ");
            }
            if (id instanceof JsNutsIdFilter) {
                JsNutsIdFilter b = (JsNutsIdFilter) id;
                String expr = b.toJsNutsIdFilterExpr();
                if (CoreStringUtils.isEmpty(expr)) {
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
    public String toString() {
        return CoreStringUtils.join(" And ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + Arrays.deepHashCode(this.all);
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
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    
}
