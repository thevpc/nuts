package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractNutsOutput implements NutsOutput {

    private Object value;
    private boolean path;
    private boolean url;
    private String name;
    private String typeName;
    private NutsSession ws;

    public AbstractNutsOutput(Object value, boolean path, boolean url, String name, String typeName, NutsSession ws) {
        this.value = value;
        this.path = path;
        this.url = url;
        this.typeName = typeName;
        if (name == null) {
            name = String.valueOf(value);
        }
        this.name = name;
        this.ws = ws;

    }

    protected NutsIOException createOpenError(Exception ex) {
        String n = getTypeName();
        if (n == null) {
            n = getName();
        }
        return new NutsIOException(ws, NutsMessage.cstyle("%s not found : %s", n, toString()), ex);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    public abstract OutputStream open();

    public Object getSource() {
        return value;
    }

    public boolean isPath() {
        return path;
    }

    public Path getFilePath() {
        throw new NutsUnsupportedOperationException(ws);
    }

    @Override
    public boolean isURL() {
        return url;
    }

    @Override
    public URL getURL() {
        throw new NutsUnsupportedOperationException(ws);
    }
}
