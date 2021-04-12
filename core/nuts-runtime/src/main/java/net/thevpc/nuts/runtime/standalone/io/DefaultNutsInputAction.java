package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsInput;
import net.thevpc.nuts.NutsInputAction;
import net.thevpc.nuts.NutsUnsupportedArgumentException;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.CoreInput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsInputAction implements NutsInputAction {

    private NutsWorkspace ws;
    private String name;
    private String typeName;
    private NutsSession session;
    private boolean multiRead;

    public DefaultNutsInputAction(NutsWorkspace ws) {
        this.ws = ws;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(ws, session);
    }

    @Override
    public NutsInput of(Object source) {
        checkSession();
        if (source == null) {
            return null;
        } else if (source instanceof NutsInput) {
            return (NutsInput) source;
        } else if (source instanceof InputStream) {
            return of((InputStream) source);
        } else if (source instanceof Path) {
            return of((Path) source);
        } else if (source instanceof File) {
            return of((File) source);
        } else if (source instanceof URL) {
            return of((URL) source);
        } else if (source instanceof byte[]) {
            return of(new ByteArrayInputStream((byte[]) source));
        } else if (source instanceof String) {
            return of((String) source);
        } else {
            throw new NutsUnsupportedArgumentException(getSession(), "Unsupported type " + source.getClass().getName());
        }
    }

    @Override
    public NutsInput of(String resource) {
        NutsInput v = CoreIOUtils.createInputSource(resource, getName(), getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    private NutsInput toMulti(NutsInput v) {
        if (isMultiRead()) {
            v = ((CoreInput) v).multi();
        }
        return v;
    }

    @Override
    public NutsInput of(byte[] bytes) {
        checkSession();
        NutsInput v = CoreIOUtils.createInputSource(bytes, getName(), getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsInput of(InputStream stream) {
        checkSession();
        NutsInput v = CoreIOUtils.createInputSource(stream, getName(), getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsInput of(URL source) {
        checkSession();
        String name = getName();
        String typeName = getTypeName();
        if (source == null) {
            return null;
        } else if (CoreIOUtils.isPathFile(source.toString())) {
            return toMulti(CoreIOUtils.createInputSource(CoreIOUtils.toPathFile(source.toString(), getSession()), name, typeName, getSession()));
        } else {
            if (name == null) {
                name = source.toString();
            }
            return toMulti(new CoreIOUtils.URLInput(name, source, typeName, getSession()));
        }
    }

    @Override
    public NutsInput of(File stream) {
        checkSession();
        NutsInput v = CoreIOUtils.createInputSource(stream, getName(), getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsInput of(Path stream) {
        checkSession();
        NutsInput v = CoreIOUtils.createInputSource(stream, getName(), getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsInput of(NutsInput stream) {
        checkSession();
        return toMulti(stream);
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
    public NutsInputAction setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsInputAction setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public boolean isMultiRead() {
        return multiRead;
    }

    @Override
    public NutsInputAction setMultiRead(boolean multiRead) {
        this.multiRead = multiRead;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsInputAction setSession(NutsSession session) {
        this.session = session;
        return this;
    }

}
