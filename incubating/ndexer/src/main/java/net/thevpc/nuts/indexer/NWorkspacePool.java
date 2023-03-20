package net.thevpc.nuts.indexer;

import net.thevpc.nuts.NLocations;
import net.thevpc.nuts.boot.DefaultNWorkspaceOptionsBuilder;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.NSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NWorkspacePool {

    @Autowired
    private NIndexerApplication.Config app;
    private final Map<String, NSession> pool = new LinkedHashMap<>();

    public NSession openWorkspace(String ws) {
        NSession o = pool.get(ws);
        if (o == null) {
            if (NLocations.of(app.getSession()).getWorkspaceLocation().toString().equals(ws)) {
                o = app.getSession();
            } else {
                o = Nuts.openWorkspace(new DefaultNWorkspaceOptionsBuilder()
                        .setInstallCompanions(false)
                        .setWorkspace(ws)
                );
            }
            pool.put(ws, o);
            pool.put(o.getWorkspace().getUuid(), o);
        }
        return o;
    }
}
