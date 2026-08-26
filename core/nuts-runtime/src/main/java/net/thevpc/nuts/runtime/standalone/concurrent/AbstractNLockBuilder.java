package net.thevpc.nuts.runtime.standalone.concurrent;

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
    public Object source() {
        return source;
    }

    @Override
    public Object resource() {
        return resource;
    }

    @Override
    public NLockBuilder source(Object source) {
        this.source=source;
        return this;
    }

    @Override
    public NLockBuilder resource(File source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder resource(Path source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder resource(NPath source) {
        this.resource=source;
        return this;
    }

    @Override
    public NLockBuilder resource(Object source) {
        this.resource=source;
        return this;
    }

}
