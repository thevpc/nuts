package net.vpc.app.nuts.core.io;

import net.vpc.app.nuts.NutsLockBuilder;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.File;
import java.nio.file.Path;

public abstract class AbstractNutsLockBuilder implements NutsLockBuilder {
    private NutsWorkspace ws;
    private Object source;
    private Object resource;
    private NutsSession session;

    public AbstractNutsLockBuilder(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public Object getLockResource() {
        return resource;
    }

    @Override
    public NutsLockBuilder source(Object source) {
        return setSource(source);
    }

    @Override
    public NutsLockBuilder setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NutsLockBuilder setLockResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsLockBuilder setLockResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsLockBuilder lockResource(Object source) {
        this.resource=source;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsLockBuilder session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsLockBuilder setSession(NutsSession session) {
        this.session=session;
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }
}
