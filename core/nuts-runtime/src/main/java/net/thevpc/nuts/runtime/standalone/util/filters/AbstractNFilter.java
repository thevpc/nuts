package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collections;
import java.util.List;

public abstract class AbstractNFilter implements NFilter {

    private NSession session;
    private NFilterOp op;

    public AbstractNFilter(NSession session, NFilterOp op) {
        this.session = session;
        if(session==null){
            throw new NullPointerException();
        }
        this.op = op;
    }

    @Override
    public NFilterOp getFilterOp() {
        return op;
    }

    @Override
    public NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    public NSession getSession() {
        return session;
    }

    @Override
    public List<NFilter> getSubFilters() {
        return Collections.emptyList();
    }

    @Override
    public NFilter or(NFilter other) {
        return other == null ? this : NFilters.of(getSession()).any(this, other);
    }

    @Override
    public NFilter and(NFilter other) {
        return other == null ? this : NFilters.of(getSession()).all(this, other);
    }

    @Override
    public NFilter neg() {
        return NFilters.of(getSession()).not(this);
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return NFilters.of(getSession()).to(type, this);
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return NFilters.of(getSession()).detectType(this);
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    @Override
    public NElement describe(NSession session) {
        return NElements.of(session).ofString(toString());
    }
}
