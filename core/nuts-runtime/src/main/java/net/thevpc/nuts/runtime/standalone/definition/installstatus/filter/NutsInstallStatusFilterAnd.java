package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsInstallStatusFilterAnd extends AbstractInstallStatusFilter{

    private NutsInstallStatusFilter[] all;

    public NutsInstallStatusFilterAnd(NutsSession session, NutsInstallStatusFilter... all) {
        super(session, NutsFilterOp.AND);
        List<NutsInstallStatusFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsInstallStatusFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsInstallStatusFilter[0]);
    }

    @Override
    public boolean acceptInstallStatus(NutsInstallStatus id, NutsSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NutsInstallStatusFilter filter : all) {
            if (!filter.acceptInstallStatus(id, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsInstallStatusFilter simplify() {
        return CoreNutsUtils.simplifyFilterAnd(getSession(),NutsInstallStatusFilter.class,this,all);
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.deepHashCode(this.all);
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
        final NutsInstallStatusFilterAnd other = (NutsInstallStatusFilterAnd) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.join(" and ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    public NutsFilter[] getSubFilters() {
        return all;
    }
}
