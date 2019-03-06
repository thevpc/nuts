package net.vpc.app.nuts.indexer;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsIndexSubscriberListManagerPool {
    private static Map<String, NutsIndexSubscriberListManager> pool = new LinkedHashMap<>();

    public static synchronized NutsIndexSubscriberListManager openSubscriberListManager(String name) {
        NutsIndexSubscriberListManager o = pool.get(name);
        if (o == null) {
            o = new NutsIndexSubscriberListManager(NutsWorkspacePool.openWorkspace("default-workspace"), name);
            pool.put(name, o);
        }
        return o;
    }
}
