package net.vpc.app.nuts.runtime.filters;

import net.vpc.app.nuts.NutsFilter;
import net.vpc.app.nuts.NutsFilterOp;
import net.vpc.app.nuts.NutsWorkspace;

public abstract class AbstractNutsFilter implements NutsFilter {
    private NutsWorkspace ws;
    private NutsFilterOp op;

    public AbstractNutsFilter(NutsWorkspace ws, NutsFilterOp op) {
        this.ws = ws;
        this.op = op;
    }

    @Override
    public NutsFilterOp getFilterOp() {
        return op;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public NutsFilter[] getSubFilters() {
        return new NutsFilter[0];
    }
}
