package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsConcurrentManager;

import java.util.concurrent.ExecutorService;
import net.thevpc.nuts.NutsIOLockAction;
import net.thevpc.nuts.NutsSession;

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

    @Override
    public ExecutorService executorService() {
        return model.executorService();
    }

    public NutsIOLockAction lock() {
        return model.lock().setSession(session);
    }

}
