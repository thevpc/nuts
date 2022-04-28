package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsVersion;

public abstract class AbstractNutsVersionCompat implements NutsVersionCompat {
    private NutsSession ws;
    private NutsVersion apiVersion;
    private int apiOrdinalVersion;

    public AbstractNutsVersionCompat(NutsSession ws, NutsVersion apiVersion, int apiOrdinalVersion) {
        this.ws = ws;
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NutsSession getSession() {
        return ws;
    }

    public NutsVersion getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
