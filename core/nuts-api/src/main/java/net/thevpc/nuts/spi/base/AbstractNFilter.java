package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Collections;
import java.util.List;

public abstract class AbstractNFilter implements NFilter {

    protected NWorkspace workspace;
    private NFilterOp op;

    public AbstractNFilter(NWorkspace workspace, NFilterOp op) {
        this.workspace = workspace;
        if(workspace ==null){
            throw new NullPointerException();
        }
        this.op = op;
    }

    @Override
    public NFilterOp getFilterOp() {
        return op;
    }

    @Override
    public List<NFilter> getSubFilters() {
        return Collections.emptyList();
    }

    @Override
    public NFilter or(NFilter other) {
        return other == null ? this : NFilters.of().any(this, other);
    }

    @Override
    public NFilter and(NFilter other) {
        return other == null ? this : NFilters.of().all(this, other);
    }

    @Override
    public NFilter neg() {
        return NFilters.of().not(this);
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return NFilters.of().to(type, this);
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return NFilters.of().detectType(this);
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return simplify().to(type);
    }

    @Override
    public NElement describe() {
        return NElements.of().ofString(toString());
    }
}
