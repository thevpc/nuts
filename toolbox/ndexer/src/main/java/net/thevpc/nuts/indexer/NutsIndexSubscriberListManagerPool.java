package net.thevpc.nuts.indexer;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NutsIndexSubscriberListManagerPool {

    @Autowired
    private NutsIndexerApplication.Config app;
    private Map<String, NutsIndexSubscriberListManager> pool = new LinkedHashMap<>();

    public synchronized NutsIndexSubscriberListManager openSubscriberListManager(String name) {
        NutsIndexSubscriberListManager o = pool.get(name);
        if (o == null) {
            o = new NutsIndexSubscriberListManager(app.getApplicationContext().getWorkspace(), name);
            pool.put(name, o);
        }
        return o;
    }
}
