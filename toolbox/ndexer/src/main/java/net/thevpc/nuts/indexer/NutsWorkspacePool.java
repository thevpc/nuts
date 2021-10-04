package net.thevpc.nuts.indexer;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.util.LinkedHashMap;
import java.util.Map;

import net.thevpc.nuts.NutsWorkspaceOptionsBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NutsWorkspacePool {

    @Autowired
    private NutsIndexerApplication.Config app;
    private final Map<String, NutsSession> pool = new LinkedHashMap<>();

    public NutsSession openWorkspace(String ws) {
        NutsSession o = pool.get(ws);
        if (o == null) {
            if (app.getApplicationContext().getSession().locations().getWorkspaceLocation().equals(ws)) {
                o = app.getApplicationContext().getSession();
            } else {
                o = Nuts.openWorkspace(NutsWorkspaceOptionsBuilder.of()
                        .setSkipCompanions(true)
                        .setWorkspace(ws)
                        .build()
                );
            }
            pool.put(ws, o);
            pool.put(o.getWorkspace().getUuid(), o);
        }
        return o;
    }
}
