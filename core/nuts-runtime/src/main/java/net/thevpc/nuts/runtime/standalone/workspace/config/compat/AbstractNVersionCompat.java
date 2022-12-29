package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NVersion;

public abstract class AbstractNVersionCompat implements NVersionCompat {
    private NSession ws;
    private NVersion apiVersion;
    private int apiOrdinalVersion;

    public AbstractNVersionCompat(NSession ws, NVersion apiVersion, int apiOrdinalVersion) {
        this.ws = ws;
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NSession getSession() {
        return ws;
    }

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
