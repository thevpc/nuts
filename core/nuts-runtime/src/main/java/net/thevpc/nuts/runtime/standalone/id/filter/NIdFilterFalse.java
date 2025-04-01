package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.util.NFilterOp;

public class NIdFilterFalse extends AbstractIdFilter implements NIdFilter, NSimplifiable<NIdFilter>, NExprIdFilter {

    public NIdFilterFalse() {
        super(NFilterOp.FALSE);
    }

    @Override
    public boolean acceptId(NId id) {
        return false;
    }

    @Override
    public NIdFilter simplify() {
        return this;
    }

    @Override
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
        final NIdFilterFalse other = (NIdFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
