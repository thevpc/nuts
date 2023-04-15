package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NVersion;

public abstract class AbstractNVersionCompat implements NVersionCompat {
    private NSession session;
    private NVersion apiVersion;
    private int apiOrdinalVersion;

    public AbstractNVersionCompat(NSession session, NVersion apiVersion, int apiOrdinalVersion) {
        this.session = session;
        this.apiVersion = apiVersion;
        this.apiOrdinalVersion = apiOrdinalVersion;
    }

    public NSession getSession() {
        return session;
    }

    public NVersion getApiVersion() {
        return apiVersion;
    }

    public int getApiOrdinalVersion() {
        return apiOrdinalVersion;
    }
}
