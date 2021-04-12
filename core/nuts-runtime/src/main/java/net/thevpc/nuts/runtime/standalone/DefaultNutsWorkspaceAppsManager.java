package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsExecutionEntryAction;
import net.thevpc.nuts.runtime.core.app.DefaultNutsApplicationContext;

public class DefaultNutsWorkspaceAppsManager implements NutsWorkspaceAppsManager {

    private NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsWorkspaceAppsManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis, NutsSession session) {
        if (session == null) {
            session = this.session;
        }
        return new DefaultNutsApplicationContext(ws, session, args, appClass, storeId, startTimeMillis);
    }

    @Override
    public NutsExecutionEntryAction execEntries() {
        return new DefaultNutsExecutionEntryAction(ws).setSession(session);
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsWorkspaceAppsManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }
    
}
