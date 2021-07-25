package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.io.FilePath;
import net.thevpc.nuts.runtime.core.io.URLPath;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.bundles.io.CoreInput;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsInputAction implements NutsInputAction {

    private NutsWorkspace ws;
    private NutsString name;
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
        } else if (source instanceof NutsPath) {
            return of((NutsPath) source);
        } else {
            throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("unsupported type %s", source.getClass().getName()));
        }
    }

    @Override
    public NutsInput of(String resource) {
        return resource==null?null:session.getWorkspace().io().path(resource).input();
    }

    private NutsInput toMulti(NutsInput v) {
        if(v==null){
            return v;
        }
        if (isMultiRead()) {
            v = ((CoreInput) v).multi();
        }
        return v;
    }

    @Override
    public NutsInput of(byte[] bytes) {
        checkSession();
        NutsString n = getName();
        if(n==null){
            n=getSession().getWorkspace().text().forStyled("<bytes>",NutsTextStyle.path());
        }
        NutsInput v = CoreIOUtils.createInputSource(bytes, n.filteredText(),n,  getTypeName(), getSession());
        v = toMulti(v);
        return v;
    }

    @Override
    public NutsInput of(InputStream source) {
        if(source==null){
            return null;
        }
        checkSession();
        NutsString name = getName();
        if(name==null){
            name=getSession().getWorkspace().text().toText(source);
        }
        return toMulti(new CoreIOUtils.InputStream(name.filteredText(),name, source, "inputStream", getSession()));
    }

    @Override
    public NutsInput of(URL source) {
        checkSession();
        return getSession().getWorkspace().io().path(source).input();
    }

    @Override
    public NutsInput of(File source) {
        checkSession();
        return getSession().getWorkspace().io().path(source).input();
    }

    @Override
    public NutsInput of(Path source) {
        checkSession();
        return getSession().getWorkspace().io().path(source).input();
    }

    @Override
    public NutsInput of(NutsPath stream) {
        checkSession();
        return stream==null?null:stream.input();
    }

    @Override
    public NutsInput of(NutsInput stream) {
        checkSession();
        return toMulti(stream);
    }

    @Override
    public NutsString getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public NutsInputAction setName(NutsString name) {
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
