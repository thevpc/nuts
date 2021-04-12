package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsTypedFilters;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public abstract class InternalNutsTypedFilters<T extends NutsFilter> implements NutsTypedFilters<T> {

    protected final DefaultNutsFilterModel model;
    protected final NutsWorkspace ws;
    private Class<T> type;
    private NutsSession session;

    public InternalNutsTypedFilters(DefaultNutsFilterModel model, Class<T> type) {
        this.model = model;
        this.ws = model.getWorkspace();
        this.type = type;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsTypedFilters setSession(NutsSession session) {
        this.session = session;
        return this;
    }
    
    protected void checkSession(){
        NutsWorkspaceUtils.checkSession(ws, session);
    }
    
    @Override
    public T nonnull(NutsFilter filter) {
        if (filter == null) {
            return always();
        }
        return filter.to(type);
    }

//    @Override
//    public T not(NutsFilter other) {
//        return defaultNutsFilterManager.not(type, other);
//    }
    protected List<T> convertList(NutsFilter... others) {
        checkSession();
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
