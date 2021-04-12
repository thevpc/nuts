package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsOutput;
import net.thevpc.nuts.NutsUnsupportedOperationException;
import net.thevpc.nuts.NutsWorkspace;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import net.thevpc.nuts.NutsSession;

public abstract class AbstractNutsOutput implements NutsOutput {

    private Object value;
    private boolean path;
    private boolean url;
    private String name;
    private String typeName;
    private NutsSession ws;

    public AbstractNutsOutput(Object value, boolean path, boolean url, String name, String typeName,NutsSession ws) {
        this.value = value;
        this.path = path;
        this.url = url;
        this.typeName = typeName;
        if(name==null){
            name=String.valueOf(value);
        }
        this.name = name;
        this.ws = ws;

    }

    protected NutsIOException createOpenError(Exception ex) {
        String n = getTypeName();
        if (n == null) {
            n = getName();
        }
        return new NutsIOException(ws, n+" not found : " +toString(), ex);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    public boolean isPath() {
        return path;
    }

    public Object getSource() {
        return value;
    }

    @Override
    public boolean isURL() {
        return url;
    }

    @Override
    public URL getURL() {
        throw new NutsUnsupportedOperationException(ws);
    }

    public Path getPath() {
        throw new NutsUnsupportedOperationException(ws);
    }

    public abstract OutputStream open();
}
