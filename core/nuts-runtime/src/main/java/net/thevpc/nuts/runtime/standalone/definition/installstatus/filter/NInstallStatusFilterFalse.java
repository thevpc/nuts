package net.thevpc.nuts.runtime.standalone.definition.installstatus.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NExprIdFilter;
import net.thevpc.nuts.util.NFilterOp;

public class NInstallStatusFilterFalse extends AbstractInstallStatusFilter implements NExprIdFilter {

    public NInstallStatusFilterFalse(NSession session) {
        super(session, NFilterOp.FALSE);
    }

    @Override
    public boolean acceptInstallStatus(NInstallStatus status, NSession session) {
        return false;
    }

    public NInstallStatusFilter simplify() {
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
        final NInstallStatusFilterFalse other = (NInstallStatusFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
