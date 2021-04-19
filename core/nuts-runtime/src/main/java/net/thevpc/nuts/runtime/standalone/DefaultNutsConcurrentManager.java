package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsConcurrentManager;

import java.util.concurrent.ExecutorService;
import net.thevpc.nuts.NutsIOLockAction;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsConcurrentManager implements NutsConcurrentManager {

    private DefaultNutsConcurrentModel model;
    private NutsSession session;

    public DefaultNutsConcurrentManager(DefaultNutsConcurrentModel model) {
        this.model = model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsConcurrentManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    @Override
    public ExecutorService executorService() {
        checkSession();
        return model.executorService(getSession());
    }

    public NutsIOLockAction lock() {
        return model.lock().setSession(getSession());
    }

}
