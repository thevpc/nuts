package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsExprIdFilter;

public class NutsInstallStatusFilterFalse extends AbstractInstallStatusFilter implements NutsExprIdFilter {

    public NutsInstallStatusFilterFalse(NutsSession session) {
        super(session, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptInstallStatus(NutsInstallStatus status, NutsSession session) {
        return false;
    }

    public NutsInstallStatusFilter simplify() {
        return this;
    }

    public String toExpr() {
        return "false";
    }

    @Override
    public int hashCode() {
        return getClass().getName().hashCode();
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
        final NutsInstallStatusFilterFalse other = (NutsInstallStatusFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
