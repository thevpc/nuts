package net.thevpc.nuts.indexer;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class NIndexSubscriberListManagerPool {

    private Map<String, NIndexSubscriberListManager> pool = new LinkedHashMap<>();

    public synchronized NIndexSubscriberListManager openSubscriberListManager(String name) {
        NIndexSubscriberListManager o = pool.get(name);
        if (o == null) {
            o = new NIndexSubscriberListManager(name);
            pool.put(name, o);
        }
        return o;
    }
}
