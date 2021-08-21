package net.thevpc.nuts.runtime.core.filters.descriptor;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsDescriptorFilterOr extends AbstractDescriptorFilter implements JsNutsDescriptorFilter {

    private NutsDescriptorFilter[] all;

    public NutsDescriptorFilterOr(NutsSession ws, NutsDescriptorFilter... all) {
        super(ws, NutsFilterOp.OR);
        List<NutsDescriptorFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsDescriptorFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsDescriptorFilter[0]);
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor id, NutsSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NutsDescriptorFilter filter : all) {
            if (filter.acceptDescriptor(id, session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return CoreNutsUtils.simplifyFilterOr(getWorkspace(),NutsDescriptorFilter.class,this,all);
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        StringBuilder sb = new StringBuilder();
        if (all.length == 0) {
            return "true";
        }
        if (all.length > 1) {
            sb.append("(");
        }
        for (NutsDescriptorFilter id : all) {
            if (sb.length() > 0) {
                sb.append(" || ");
            }
            if (id instanceof JsNutsDescriptorFilter) {
                JsNutsDescriptorFilter b = (JsNutsDescriptorFilter) id;
                String expr = b.toJsNutsDescriptorFilterExpr();
                if (NutsUtilStrings.isBlank(expr)) {
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
        return String.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    public NutsFilter[] getSubFilters() {
        return all;
    }
}
