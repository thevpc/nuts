package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NInstallStatusFilterOr extends AbstractInstallStatusFilter implements NComplexExpressionString {

    private NInstallStatusFilter[] all;

    public NInstallStatusFilterOr(NSession session, NInstallStatusFilter... all) {
        super(session, NFilterOp.OR);
        List<NInstallStatusFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NInstallStatusFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NInstallStatusFilter[0]);
    }

    @Override
    public boolean acceptInstallStatus(NInstallStatus id, NSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NInstallStatusFilter filter : all) {
            if (filter.acceptInstallStatus(id, session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NInstallStatusFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(getSession(), NInstallStatusFilter.class,this,all);
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrOr(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
