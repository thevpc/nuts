package net.thevpc.nuts;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Path;

public interface NutsInput extends AutoCloseable {
    String getName();

    String getTypeName();

    @Override
    void close();

    InputStream open();

    long length();

    Object getSource();

    boolean isPath();

    Path getPath();

    boolean isURL();

    URL getURL();
}
