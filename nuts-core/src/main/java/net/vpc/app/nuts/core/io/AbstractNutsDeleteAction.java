package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.*;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractNutsDeleteAction implements NutsDeleteAction {
    private NutsWorkspace ws;
    private Object target;
    private NutsSession session;
    private boolean failFast;

    public AbstractNutsDeleteAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsDeleteAction session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsDeleteAction setSession(NutsSession session) {
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
    public NutsDeleteAction setTarget(Object target) {
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
    public NutsDeleteAction setTarget(File target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsDeleteAction setTarget(Path target) {
        this.target = target;
        return this;
    }

    @Override
    public NutsDeleteAction target(Object target) {
        return setTarget(target);
    }

    @Override
    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public NutsDeleteAction setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsDeleteAction failFast(boolean failFast) {
        return setFailFast(false);
    }
}
