package net.thevpc.nuts.indexer;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspaceListManager;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NWorkspaceListManagerPool {

    @Autowired
    private NIndexerApplication.Config app;
    private final Map<String, NWorkspaceListManager> pool = new LinkedHashMap<>();

    public synchronized NWorkspaceListManager openListManager(String name) {
        NWorkspaceListManager o = pool.get(name);
        if (o == null) {
            NSession session = app.getApplicationContext().getSession();
            o = NWorkspaceListManager.of(session).setName(name);
            pool.put(name, o);
        }
        return o;
    }
}
