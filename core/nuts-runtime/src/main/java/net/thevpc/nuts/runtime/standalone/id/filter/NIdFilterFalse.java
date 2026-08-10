package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
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
    public String toString() {
        return "false";
    }

}
