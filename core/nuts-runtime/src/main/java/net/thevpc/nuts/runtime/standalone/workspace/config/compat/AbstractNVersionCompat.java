package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NVersion;

public abstract class AbstractNVersionCompat implements NVersionCompat {
    private NVersion apiVersion;
    private int apiOrdinalVersion;

    public AbstractNVersionCompat(NVersion apiVersion, int apiOrdinalVersion) {
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
