package net.thevpc.nuts.runtime;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsExecutionEntryManager;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceAppsManager;
import net.thevpc.nuts.runtime.io.DefaultNutsExecutionEntryManager;
import net.thevpc.nuts.runtime.app.DefaultNutsApplicationContext;

public class DefaultNutsWorkspaceAppsManager implements NutsWorkspaceAppsManager {
    private NutsWorkspace ws;
    private DefaultNutsExecutionEntryManager execEntry;

    public DefaultNutsWorkspaceAppsManager(NutsWorkspace ws) {
        this.ws = ws;
        execEntry=new DefaultNutsExecutionEntryManager(ws);
    }

    @Override
    public NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis) {
        return new DefaultNutsApplicationContext(ws, args, appClass, storeId, startTimeMillis);
    }

    @Override
    public NutsExecutionEntryManager execEntries() {
        return execEntry;
    }
}
