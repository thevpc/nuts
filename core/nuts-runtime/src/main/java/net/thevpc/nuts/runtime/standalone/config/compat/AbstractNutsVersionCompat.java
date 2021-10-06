package net.thevpc.nuts.runtime.standalone.config.compat;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

public abstract class AbstractNutsVersionCompat implements NutsVersionCompat {
    private NutsSession ws;
    private String apiVersion;
    private int apiOrdinalVersion;

    public AbstractNutsVersionCompat(NutsSession ws, String apiVersion, int apiOrdinalVersion) {
        this.ws = ws;
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NutsSession getSession() {
        return ws;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
