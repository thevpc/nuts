package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspaceListManager;
import net.vpc.app.nuts.NutsWorkspaceOptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsWorkspaceListManagerPool {
    private static Map<String, NutsWorkspaceListManager> pool = new LinkedHashMap<>();

    public static synchronized NutsWorkspaceListManager openListManager(String name) {
        NutsWorkspaceListManager o = pool.get(name);
        if (o == null) {
            o = NutsWorkspacePool.openWorkspace("default-workspace").getConfigManager().createWorkspaceListManager(name);
            pool.put(name, o);
        }
        return o;
    }
}
