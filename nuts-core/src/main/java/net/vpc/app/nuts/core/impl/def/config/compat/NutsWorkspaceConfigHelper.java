package net.vpc.app.nuts.core.impl.def.config.compat;

import net.vpc.app.nuts.NutsWorkspace;

import java.util.Map;
import java.util.logging.Logger;

public class NutsWorkspaceConfigHelper {
    private NutsWorkspace ws;

    public NutsWorkspaceConfigHelper(NutsWorkspace ws) {
        this.ws = ws;
    }


    private Map parseConfigMap(byte[] bytes) {
        return ws.json().parse(bytes, Map.class);
    }



}
