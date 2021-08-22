package net.thevpc.nuts.runtime.bundles.io;

import net.thevpc.nuts.*;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractNutsOutput implements NutsOutput {

    private final Object value;
    private final boolean path;
    private final boolean url;
    private final String name;
    private final String typeName;
    private final NutsSession session;

    public AbstractNutsOutput(Object value, boolean path, boolean url, String name, String typeName, NutsSession session) {
        this.value = value;
        this.path = path;
        this.url = url;
        this.typeName = typeName;
        if (name == null) {
            name = String.valueOf(value);
        }
        this.name = name;
        this.session = session;

    }

    protected NutsIOException createOpenError(Exception ex) {
        String n = getTypeName();
        if (n == null) {
            n = getName();
        }
        return new NutsIOException(session, NutsMessage.cstyle("%s not found : %s", n, toString()), ex);
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
        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported operation '%s'", "getFilePath"));
    }

    @Override
    public boolean isURL() {
        return url;
    }

    @Override
    public URL getURL() {
        throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unsupported operation '%s'", "getURL"));
    }
}
