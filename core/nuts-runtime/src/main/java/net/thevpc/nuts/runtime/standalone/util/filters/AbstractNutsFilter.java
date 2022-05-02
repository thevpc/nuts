package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElements;

import java.util.Collections;
import java.util.List;

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
    public List<NutsFilter> getSubFilters() {
        return Collections.emptyList();
    }

    @Override
    public NutsFilter or(NutsFilter other) {
        return other == null ? this : NutsFilters.of(getSession()).any(this, other);
    }

    @Override
    public NutsFilter and(NutsFilter other) {
        return other == null ? this : NutsFilters.of(getSession()).all(this, other);
    }

    @Override
    public NutsFilter neg() {
        return NutsFilters.of(getSession()).not(this);
    }

    @Override
    public <T extends NutsFilter> T to(Class<T> type) {
        return NutsFilters.of(getSession()).to(type, this);
    }

    @Override
    public Class<? extends NutsFilter> getFilterType() {
        return NutsFilters.of(getSession()).detectType(this);
    }

    @Override
    public <T extends NutsFilter> NutsFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    @Override
    public NutsElement describe(NutsSession session) {
        return NutsElements.of(session).ofString(toString());
    }
}
