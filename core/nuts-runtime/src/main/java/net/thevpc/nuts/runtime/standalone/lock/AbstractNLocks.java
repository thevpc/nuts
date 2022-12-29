package net.thevpc.nuts.runtime.standalone.lock;

import net.thevpc.nuts.concurrent.NLocks;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;

import java.io.File;
import java.nio.file.Path;

import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;

public abstract class AbstractNLocks implements NLocks {
    private NWorkspace ws;
    private Object source;
    private Object resource;
    private NSession session;

    public AbstractNLocks(NSession session) {
        this.session = session;
        this.ws = session.getWorkspace();
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, getSession());
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
    public NLocks setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NLocks setResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLocks setResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLocks setResource(Object source) {
        this.resource=source;
        return this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NLocks setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return this;
    }

    public NWorkspace getWs() {
        return ws;
    }

}
