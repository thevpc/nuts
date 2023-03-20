package net.thevpc.nuts.clown.services;

import net.thevpc.nuts.*;

import java.util.LinkedHashMap;
import java.util.Map;

public class NutsWorkspacePool {
    private static final Map<String, NutsSession> pool = new LinkedHashMap<>();

    public static synchronized NutsSession openWorkspace(String ws) {
        NutsSession o = pool.get(ws);
        if (o == null) {
            o = Nuts.openWorkspace(new NutsBootOptions()
                    //should suppose the workspace is already created!
                    .setOpenMode(NutsOpenMode.OPEN_OR_ERROR)
//                    .setRecover(true)
                    .setInstallCompanions(false)
                    .setWorkspace(ws)
            );
            pool.put(ws, o);
            pool.put(o.getWorkspace().getUuid(), o);
        }
        return o;
    }
}
