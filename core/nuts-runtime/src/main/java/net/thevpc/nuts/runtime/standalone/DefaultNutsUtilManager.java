package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUtilManager;
import net.thevpc.nuts.NutsVal;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsUtilManager implements NutsUtilManager {

    private DefaultNutsUtilModel model;
    private NutsSession session;

    public DefaultNutsUtilManager(DefaultNutsUtilModel model) {
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsUtilManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsVal valOf(Object str) {
        return new DefaultNutsVal(str);
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }
}
