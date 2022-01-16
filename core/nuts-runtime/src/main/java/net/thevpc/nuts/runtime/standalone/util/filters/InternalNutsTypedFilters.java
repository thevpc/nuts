package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.NutsFilter;
import net.thevpc.nuts.NutsTypedFilters;
import net.thevpc.nuts.NutsWorkspace;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

public abstract class InternalNutsTypedFilters<T extends NutsFilter> implements NutsTypedFilters<T> {

    protected final DefaultNutsFilterModel model;
    protected final NutsWorkspace ws;
    private Class<T> type;
    private NutsSession session;

    public InternalNutsTypedFilters(NutsSession session, Class<T> type) {
        this.session = session;
        this.model = NutsWorkspaceExt.of(session.getWorkspace()).getModel().filtersModel;
        this.ws = model.getWorkspace();
        this.type = type;
    }

    public NutsSession getSession() {
        return session;
    }

    protected void checkSession(){
        NutsSessionUtils.checkSession(ws, session);
    }
    
    @Override
    public T nonnull(NutsFilter filter) {
        if (filter == null) {
            return always();
        }
        return filter.to(type);
    }

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
