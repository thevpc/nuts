package net.thevpc.nuts;

import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsOutput extends AutoCloseable {
    String getName();

    String getTypeName();

    @Override
    void close();

    OutputStream open();

    Object getSource();

    boolean isPath();

    Path getPath();

    boolean isURL();

    URL getURL();
}
