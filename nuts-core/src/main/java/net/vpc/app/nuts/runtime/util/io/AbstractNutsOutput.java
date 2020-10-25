package net.vpc.app.nuts.runtime.util.io;

import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsOutput;
import net.vpc.app.nuts.NutsUnsupportedOperationException;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public abstract class AbstractNutsOutput implements NutsOutput {

    private Object value;
    private boolean path;
    private boolean url;
    private String name;
    private String typeName;

    public AbstractNutsOutput(Object value, boolean path, boolean url, String name, String typeName) {
        this.value = value;
        this.path = path;
        this.url = url;
        this.typeName = typeName;
        if(name==null){
            name=String.valueOf(value);
        }
        this.name = name;

    }

    protected NutsIOException createOpenError(Exception ex) {
        String n = getTypeName();
        if (n == null) {
            n = getName();
        }
        return new NutsIOException(null, "unable to open " + n + " from " + toString(), ex);
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
        throw new NutsUnsupportedOperationException(null);
    }

    public Path getPath() {
        throw new NutsUnsupportedOperationException(null);
    }

    public abstract OutputStream open();
}
