package net.thevpc.nuts.clown.services;

import net.thevpc.nuts.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsWorkspacePool {
    private static final Map<String, NutsWorkspace> pool = new LinkedHashMap<>();

    public static synchronized NutsWorkspace openWorkspace(String ws) {
        NutsWorkspace o = pool.get(ws);
        if (o == null) {
            o = Nuts.openWorkspace(new NutsDefaultWorkspaceOptions()
                    //should suppose the workspace is already created!
                    .setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING)
//                    .setRecover(true)
                    .setSkipCompanions(true)
                    .setWorkspace(ws)
            );
            pool.put(ws, o);
            pool.put(o.getUuid(), o);
        }
        return o;
    }
}
