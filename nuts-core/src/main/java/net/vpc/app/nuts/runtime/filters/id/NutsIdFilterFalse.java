package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.AbstractNutsFilter;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.common.Simplifiable;

import java.util.Arrays;
import java.util.stream.Collectors;

public class NutsIdFilterFalse extends AbstractNutsFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsScriptAwareIdFilter {

    public NutsIdFilterFalse(NutsWorkspace ws) {
        super(ws, NutsFilterOp.FALSE);
    }

    @Override
    public boolean acceptId(NutsId id, NutsSession session) {
        return false;
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
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
        final NutsIdFilterFalse other = (NutsIdFilterFalse) obj;
        return true;
    }

    @Override
    public String toString() {
        return "false";
    }

}
