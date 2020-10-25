package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecutionEntryManager;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceAppsManager;
import net.vpc.app.nuts.runtime.app.DefaultNutsApplicationContext;
import net.vpc.app.nuts.runtime.io.DefaultNutsExecutionEntryManager;

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
