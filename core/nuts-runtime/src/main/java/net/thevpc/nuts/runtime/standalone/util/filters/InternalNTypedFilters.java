package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.NTypedFilters;
import net.thevpc.nuts.NWorkspace;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

public abstract class InternalNTypedFilters<T extends NFilter> implements NTypedFilters<T> {

    protected final DefaultNFilterModel model;
    protected final NWorkspace ws;
    private Class<T> type;

    public InternalNTypedFilters(Class<T> type) {
        this.model = NWorkspaceExt.of().getModel().filtersModel;
        this.ws = model.getWorkspace();
        this.type = type;
    }

    @Override
    public T nonnull(NFilter filter) {
        if (filter == null) {
            return always();
        }
        return filter.to(type);
    }

    protected List<T> convertList(NFilter... others) {
        List<T> all = new ArrayList<>();
        if (others != null) {
            for (NFilter other : others) {
                T a = from(other);
                if (a != null) {
                    all.add(a);
                }
            }
        }
        return all;
    }

}
