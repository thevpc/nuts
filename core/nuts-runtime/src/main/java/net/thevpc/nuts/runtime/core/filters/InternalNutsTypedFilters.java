package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsTypedFilters;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;

public abstract class InternalNutsTypedFilters<T extends NutsFilter> implements NutsTypedFilters<T> {
    protected final DefaultNutsFilterManager defaultNutsFilterManager;
    protected final NutsWorkspace ws;
    private Class<T> type;

    public InternalNutsTypedFilters(DefaultNutsFilterManager defaultNutsFilterManager, Class<T> type) {
        this.defaultNutsFilterManager = defaultNutsFilterManager;
        this.ws = defaultNutsFilterManager.ws;
        this.type = type;
    }

    @Override
    public T nonnull(NutsFilter filter) {
        return defaultNutsFilterManager.nonnull(type, filter);
    }

    @Override
    public T not(NutsFilter other) {
        return defaultNutsFilterManager.not(type, other);
    }



    protected List<T> convertList(NutsFilter... others) {
        List<T> all = new ArrayList<>();
        for (NutsFilter other : others) {
            T a = from(other);
            if (a != null) {
                all.add(a);
            }
        }
        return all;
    }

}
