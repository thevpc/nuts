package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsExecutionEntryAction;
import net.thevpc.nuts.runtime.core.app.DefaultNutsApplicationContext;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsWorkspaceAppsManager implements NutsWorkspaceAppsManager {

    private NutsWorkspace ws;
    private NutsSession session;

    public DefaultNutsWorkspaceAppsManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
    }
    @Override
    public NutsApplicationContext createApplicationContext(NutsSession session, String[] args, long startTimeMillis, Class appClass, String storeId) {
        if (session == null) {
            checkSession(this.session);
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
