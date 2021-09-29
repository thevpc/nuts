package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.AbstractNutsOutput;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;

public class DefaultNutsOutputAction implements NutsOutputAction {

    private NutsWorkspace ws;
    private String name;
    private String typeName;
    private NutsSession session;

    public DefaultNutsOutputAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    private static NutsOutput createOutputTarget(OutputStream target, String name, String typeName, NutsSession session) {
        if (target == null) {
            return null;
        }
        return new AbstractNutsOutput(target, false, false, name, typeName, session) {
            @Override
            public OutputStream open() {
                return (OutputStream) getSource();
            }

            @Override
            public String toString() {
                return "OutputStream(" + getSource() + ")";
            }

            @Override
            public void close() {
                try {
                    ((OutputStream) getSource()).close();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        };
    }

    private static NutsOutput createOutputTarget(String target, String name, String typeName, NutsSession session) {
        if (target == null) {
            return null;
        }
        return session.io().path(target).output();
    }

    @Override
    public NutsOutput of(Object source) {
        checkSession();
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
        } else if (source instanceof NutsPath) {
            return of((NutsPath) source);
        } else {
            throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("unsupported type %s", source.getClass().getName()));
        }
    }

    @Override
    public NutsOutput of(String resource) {
        checkSession();
        NutsOutput v = createOutputTarget(resource, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    private NutsOutput toMulti(NutsOutput v) {
        return v;
    }

    @Override
    public NutsOutput of(OutputStream stream) {
        checkSession();
        NutsOutput v = createOutputTarget(stream, getName(), getTypeName(), session);
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsOutput of(URL source) {
        checkSession();
        return session.io().path(source).output();
    }

    @Override
    public NutsOutput of(File source) {
        checkSession();
        return session.io().path(source).output();
    }

    @Override
    public NutsOutput of(Path source) {
        checkSession();
        return session.io().path(source).output();
    }

    @Override
    public NutsOutput of(NutsPath source) {
        checkSession();
        return source.output();
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
    public NutsOutputAction setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsOutputAction setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsOutputAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

}
