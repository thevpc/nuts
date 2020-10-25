package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsOutputManager implements NutsOutputManager {
    private NutsWorkspace ws;
    private String name;
    private String typeName;

    public DefaultNutsOutputManager(NutsWorkspace ws) {
        this.ws = ws;
    }


    @Override
    public NutsOutput of(Object source) {
        if (source == null) {
            return null;
        } else if (source instanceof NutsOutput) {
            return (NutsOutput) source;
        } else if (source instanceof OutputStream) {
            return of((OutputStream) source);
        } else if (source instanceof Path) {
            return of((Path) source);
        } else if (source instanceof File) {
            return of((File) source);
        } else if (source instanceof URL) {
            return of((URL) source);
        } else if (source instanceof String) {
            return of((String) source);
        } else {
            throw new NutsUnsupportedArgumentException(null, "Unsupported type " + source.getClass().getName());
        }
    }

    @Override
    public NutsOutput of(String resource) {
        NutsOutput v = CoreIOUtils.createOutputTarget(resource, getName(), getTypeName());
        v = toMulti(v);
        return v;
    }

    private NutsOutput toMulti(NutsOutput v) {
        return v;
    }

    @Override
    public NutsOutput of(OutputStream stream) {
        NutsOutput v = CoreIOUtils.createOutputTarget(stream, getName(), getTypeName());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(URL stream) {
        NutsOutput v = CoreIOUtils.createOutputTarget(stream, getName(), getTypeName());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(File stream) {
        NutsOutput v = CoreIOUtils.createOutputTarget(stream, getName(), getTypeName());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(Path stream) {
        NutsOutput v = CoreIOUtils.createOutputTarget(stream, getName(), getTypeName());
        v = toMulti(v);
        return v;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public NutsOutputManager setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsOutputManager setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

}
