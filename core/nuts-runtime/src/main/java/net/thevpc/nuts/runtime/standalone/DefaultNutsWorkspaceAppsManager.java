package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsExecutionEntryAction;
import net.thevpc.nuts.runtime.standalone.app.DefaultNutsApplicationContext;

public class DefaultNutsWorkspaceAppsManager implements NutsWorkspaceAppsManager {
    private NutsWorkspace ws;
    private DefaultNutsExecutionEntryAction execEntry;

    public DefaultNutsWorkspaceAppsManager(NutsWorkspace ws) {
        this.ws = ws;
        execEntry=new DefaultNutsExecutionEntryAction(ws);
    }

    @Override
    public NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis, NutsSession session) {
        return new DefaultNutsApplicationContext(ws, session,args, appClass, storeId, startTimeMillis);
    }

    @Override
    public NutsExecutionEntryAction execEntries() {
        return execEntry;
    }
}
