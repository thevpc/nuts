package net.vpc.app.nuts.main.config.compat;

import net.vpc.app.nuts.NutsWorkspace;

import java.util.Map;

public class NutsWorkspaceConfigHelper {
    private NutsWorkspace ws;

    public NutsWorkspaceConfigHelper(NutsWorkspace ws) {
        this.ws = ws;
    }


    private Map parseConfigMap(byte[] bytes) {
        return ws.json().parse(bytes, Map.class);
    }



}
