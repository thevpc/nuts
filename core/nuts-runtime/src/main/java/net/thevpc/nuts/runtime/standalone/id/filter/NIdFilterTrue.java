package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.spi.base.NIdFilterBase;
import net.thevpc.nuts.util.NSimplifiable;
import net.thevpc.nuts.util.NFilterOp;

public class NIdFilterTrue extends NIdFilterBase implements NIdFilter, NSimplifiable<NIdFilter>, NExprIdFilter {

    public NIdFilterTrue() {
        super(NFilterOp.TRUE);
    }

    @Override
    public boolean acceptId(NId id) {
        return true;
    }

    @Override
    public NIdFilter simplify() {
        return this;
    }

    @Override
    public String toExpr() {
        return "true";
    }


    @Override
    public String toString() {
        return "true";
    }

}
