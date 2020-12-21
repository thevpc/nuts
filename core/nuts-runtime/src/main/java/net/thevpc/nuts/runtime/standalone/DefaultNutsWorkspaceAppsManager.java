package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.io.DefaultNutsExecutionEntryManager;
import net.thevpc.nuts.runtime.standalone.app.DefaultNutsApplicationContext;

public class DefaultNutsWorkspaceAppsManager implements NutsWorkspaceAppsManager {
    private NutsWorkspace ws;
    private DefaultNutsExecutionEntryManager execEntry;

    public DefaultNutsWorkspaceAppsManager(NutsWorkspace ws) {
        this.ws = ws;
        execEntry=new DefaultNutsExecutionEntryManager(ws);
    }

    @Override
    public NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis, NutsSession session) {
        return new DefaultNutsApplicationContext(ws, session,args, appClass, storeId, startTimeMillis);
    }

    @Override
    public NutsExecutionEntryManager execEntries() {
        return execEntry;
    }
}
