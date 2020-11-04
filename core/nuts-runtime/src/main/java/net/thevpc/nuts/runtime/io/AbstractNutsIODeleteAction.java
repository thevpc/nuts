package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.NutsException;
import net.thevpc.nuts.NutsIODeleteAction;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractNutsIODeleteAction implements NutsIODeleteAction {
    private NutsWorkspace ws;
    private Object target;
    private NutsSession session;
    private boolean failFast;

    public AbstractNutsIODeleteAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsIODeleteAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    @Override
    public Object getTarget() {
        return target;
    }

    @Override
    public NutsIODeleteAction setTarget(Object target) {
        if (target == null) {
            this.target = null;
            return this;
        }
        if (target instanceof File) {
            return setTarget((File) target);
        }
        if (target instanceof Path) {
            return setTarget((Path) target);
        }
        throw new NutsException(ws, "Unsupported Delete " + target);
    }

    @Override
    public NutsIODeleteAction setTarget(File target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsIODeleteAction setTarget(Path target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsIODeleteAction target(Object target) {
        return setTarget(target);
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsIODeleteAction failFast() {
        return failFast(true);
    }

    @Override
    public NutsIODeleteAction setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsIODeleteAction failFast(boolean failFast) {
        return setFailFast(false);
    }
}
