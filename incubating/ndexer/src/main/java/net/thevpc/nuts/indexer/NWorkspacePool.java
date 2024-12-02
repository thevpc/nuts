package net.thevpc.nuts.indexer;


import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.NWorkspaceOptionsBuilder;
import net.thevpc.nuts.Nuts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class NWorkspacePool {

    @Autowired
    private NWorkspace workspace;
    private final Map<String, NWorkspace> pool = new LinkedHashMap<>();

    public NWorkspace openWorkspace(String ws) {
        NWorkspace o = pool.get(ws);
        if (o == null) {
            if (NWorkspace.of().getWorkspaceLocation().toString().equals(ws)) {
                o = workspace;
            } else {
                o = Nuts.openWorkspace(NWorkspaceOptionsBuilder.of()
                        .setInstallCompanions(false)
                        .setWorkspace(ws)
                );
            }
            pool.put(ws, o);
            pool.put(o.getUuid(), o);
        }
        return o;
    }
}
