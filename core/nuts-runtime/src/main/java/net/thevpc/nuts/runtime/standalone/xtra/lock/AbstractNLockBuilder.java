package net.thevpc.nuts.runtime.standalone.xtra.lock;

import net.thevpc.nuts.concurrent.NLockBuilder;

import java.io.File;
import java.nio.file.Path;

import net.thevpc.nuts.io.NPath;

public abstract class AbstractNLockBuilder implements NLockBuilder {
    private Object source;
    private Object resource;

    public AbstractNLockBuilder() {
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
    public NLockBuilder setSource(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NLockBuilder setResource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder setResource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder setResource(NPath source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder setResource(Object source) {
        this.resource=source;
        return this;
    }

}
