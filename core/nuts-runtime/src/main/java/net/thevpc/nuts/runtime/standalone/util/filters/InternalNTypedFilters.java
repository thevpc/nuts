package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.NFilter;
import net.thevpc.nuts.NTypedFilters;
import net.thevpc.nuts.NWorkspace;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

public abstract class InternalNTypedFilters<T extends NFilter> implements NTypedFilters<T> {

    protected final DefaultNFilterModel model;
    protected final NWorkspace ws;
    private Class<T> type;
    private NSession session;

    public InternalNTypedFilters(NSession session, Class<T> type) {
        this.session = session;
        this.model = NWorkspaceExt.of(session.getWorkspace()).getModel().filtersModel;
        this.ws = model.getWorkspace();
        this.type = type;
    }

    public NSession getSession() {
        return session;
    }

    protected void checkSession(){
        NSessionUtils.checkSession(ws, session);
    }
    
    @Override
    public T nonnull(NFilter filter) {
        if (filter == null) {
            return always();
        }
        return filter.to(type);
    }

    protected List<T> convertList(NFilter... others) {
        checkSession();
        List<T> all = new ArrayList<>();
        for (NFilter other : others) {
            T a = from(other);
            if (a != null) {
                all.add(a);
            }
        }
        return all;
    }

}
