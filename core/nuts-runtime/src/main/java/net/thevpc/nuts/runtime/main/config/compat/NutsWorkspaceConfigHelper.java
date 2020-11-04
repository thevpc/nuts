package net.thevpc.nuts.runtime.main.config.compat;

import net.thevpc.nuts.NutsWorkspace;

import java.util.Map;

public class NutsWorkspaceConfigHelper {
    private NutsWorkspace ws;

    public NutsWorkspaceConfigHelper(NutsWorkspace ws) {
        this.ws = ws;
    }


    private Map parseConfigMap(byte[] bytes) {
        return ws.formats().json().parse(bytes, Map.class);
    }



}
