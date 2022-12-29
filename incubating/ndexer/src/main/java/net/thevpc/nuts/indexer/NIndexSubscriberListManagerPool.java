package net.thevpc.nuts.indexer;

import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.NSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NIndexSubscriberListManagerPool {

    @Autowired
    private NIndexerApplication.Config app;
    private Map<String, NIndexSubscriberListManager> pool = new LinkedHashMap<>();

    public synchronized NIndexSubscriberListManager openSubscriberListManager(String name) {
        NIndexSubscriberListManager o = pool.get(name);
        if (o == null) {
            NSession session=app.getApplicationContext().getSession();
            o = new NIndexSubscriberListManager(session,name);
            pool.put(name, o);
        }
        return o;
    }
}
