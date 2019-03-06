package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceOptions;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsWorkspacePool {
    private static Map<String, NutsWorkspace> pool = new LinkedHashMap<>();

    public static synchronized NutsWorkspace openWorkspace(String ws) {
        NutsWorkspace o = pool.get(ws);
        if (o == null) {
            o = Nuts.openWorkspace(new NutsWorkspaceOptions().setRecover(true).setSkipPostCreateInstallCompanionTools(true).setWorkspace(ws));
            pool.put(ws, o);
            pool.put(o.getUuid(), o);
        }
        return o;
    }
}
