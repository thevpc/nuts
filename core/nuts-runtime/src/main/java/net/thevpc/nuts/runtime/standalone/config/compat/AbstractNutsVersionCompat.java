package net.thevpc.nuts.runtime.standalone.config.compat;

import net.thevpc.nuts.NutsWorkspace;

public abstract class AbstractNutsVersionCompat implements NutsVersionCompat {
    private NutsWorkspace ws;
    private String apiVersion;
    private int apiOrdinalVersion;

    public AbstractNutsVersionCompat(NutsWorkspace ws, String apiVersion, int apiOrdinalVersion) {
        this.ws = ws;
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
