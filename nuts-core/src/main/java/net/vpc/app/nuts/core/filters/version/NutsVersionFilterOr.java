package net.vpc.app.nuts.core.filters.version;

import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.core.filters.id.NutsJsAwareIdFilter;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.CoreStringUtils;

public class NutsVersionFilterOr implements NutsVersionFilter, Simplifiable<NutsVersionFilter>, NutsJsAwareIdFilter {

    private NutsVersionFilter[] all;

    public NutsVersionFilterOr(NutsVersionFilter... all) {
        List<NutsVersionFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsVersionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsVersionFilter[0]);
    }

    @Override
    public boolean accept(NutsVersion id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsVersionFilter filter : all) {
            if (filter.accept(id)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsVersionFilter simplify() {
        if(all.length==0){
            return null;
        }
        NutsVersionFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsVersionFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsVersionFilterOr(newValues);
        }
        if (all.length == 0) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
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
        for (NutsVersionFilter id : all) {
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            if (id instanceof NutsJsAwareIdFilter) {
                NutsJsAwareIdFilter b = (NutsJsAwareIdFilter) id;
                String expr = b.toJsNutsIdFilterExpr();
                if (CoreStringUtils.isBlank(expr)) {
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
        int hash = 7;
        hash = 71 * hash + Arrays.deepHashCode(this.all);
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
        final NutsVersionFilterOr other = (NutsVersionFilterOr) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }
    
    @Override
    public String toString() {
        return CoreStringUtils.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

}
