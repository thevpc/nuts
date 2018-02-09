package net.vpc.app.nuts.extensions.filters.version;

import net.vpc.app.nuts.extensions.filters.id.JsNutsIdFilter;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.Simplifiable;

import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

public class NutsVersionFilterAnd implements NutsVersionFilter, Simplifiable<NutsVersionFilter> ,JsNutsIdFilter{
    private NutsVersionFilter[] all;

    public NutsVersionFilterAnd(NutsVersionFilter... all) {
        List<NutsVersionFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsVersionFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsVersionFilter[valid.size()]);
    }

    @Override
    public boolean accept(NutsVersion id) {
        if (all.length == 0) {
            return true;
        }
        for (NutsVersionFilter filter : all) {
            if (!filter.accept(id)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsVersionFilter simplify() {
        NutsVersionFilter[] newValues = CoreNutsUtils.simplifyAndShrink(NutsVersionFilter.class, all);
        if (newValues != null) {
            if (newValues.length == 0) {
                return null;
            }
            if (newValues.length == 1) {
                return newValues[0];
            }
            return new NutsVersionFilterAnd(newValues);
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
}
