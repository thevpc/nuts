package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.concurrent.NutsLocks;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.File;
import java.nio.file.Path;

import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

public abstract class AbstractNutsLocks implements NutsLocks {
    private NutsWorkspace ws;
    private Object source;
    private Object resource;
    private NutsSession session;

    public AbstractNutsLocks(NutsSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    protected void checkSession() {
        NutsSessionUtils.checkSession(ws, getSession());
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getResource() {
        return resource;
    }

    @Override
    public NutsLocks setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NutsLocks setResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsLocks setResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsLocks setResource(Object source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsLocks setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

}
