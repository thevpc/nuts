package net.vpc.app.nuts.clown.services;

import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceOptions;

import java.util.LinkedHashMap;
import java.util.Map;
import net.vpc.app.nuts.NutsWorkspaceOpenMode;

public class NutsWorkspacePool {
    private static final Map<String, NutsWorkspace> pool = new LinkedHashMap<>();

    public static synchronized NutsWorkspace openWorkspace(String ws) {
        NutsWorkspace o = pool.get(ws);
        if (o == null) {
            o = Nuts.openWorkspace(new NutsWorkspaceOptions()
                    //should suppose the workspace is already created!
                    .setOpenMode(NutsWorkspaceOpenMode.OPEN_EXISTING)
//                    .setRecover(true)
                    .setSkipPostCreateInstallCompanionTools(true)
                    .setWorkspace(ws)
            );
            pool.put(ws, o);
            pool.put(o.getUuid(), o);
        }
        return o;
    }
}
