package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NVersion;
import net.thevpc.nuts.NWorkspace;

public abstract class AbstractNVersionCompat implements NVersionCompat {
    private NVersion apiVersion;
    private int apiOrdinalVersion;
    protected NWorkspace workspace;

    public AbstractNVersionCompat(NWorkspace workspace,NVersion apiVersion, int apiOrdinalVersion) {
        this.workspace = workspace;
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
