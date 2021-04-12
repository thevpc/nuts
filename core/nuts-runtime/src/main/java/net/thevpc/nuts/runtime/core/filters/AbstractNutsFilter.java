package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsFilterOp;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

public abstract class AbstractNutsFilter implements NutsFilter {

    private NutsSession session;
    private NutsFilterOp op;

    public AbstractNutsFilter(NutsSession session, NutsFilterOp op) {
        this.session = session;
        if(session==null){
            throw new NullPointerException();
        }
        this.op = op;
    }

    @Override
    public NutsFilterOp getFilterOp() {
        return op;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsFilter[] getSubFilters() {
        return new NutsFilter[0];
    }

    @Override
    public NutsFilter or(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().any(this, other);
    }

    @Override
    public NutsFilter and(NutsFilter other) {
        return other == null ? this : getWorkspace().filters().all(this, other);
    }

    @Override
    public NutsFilter neg() {
        return getWorkspace().filters().not(this);
    }

    @Override
    public <T extends NutsFilter> T to(Class<T> type) {
        return getWorkspace().filters().to(type, this);
    }

    @Override
    public Class<? extends NutsFilter> getFilterType() {
        return getWorkspace().filters().detectType(this);
    }

    @Override
    public <T extends NutsFilter> NutsFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

}
