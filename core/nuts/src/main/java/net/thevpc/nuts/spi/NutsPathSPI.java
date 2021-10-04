package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

public interface NutsPathSPI {
    NutsPath[] getChildren();

    NutsFormatSPI getFormatterSPI();

    default String getName() {
        return null;
    }

    default String getProtocol() {
        return null;
    }

    NutsPath resolve(String path);

    default NutsPath toCompressedForm() {
        return null;
    }

    default URL toURL() {
        return null;
    }

    default Path toFilePath() {
        return null;
    }

    boolean isDirectory();

    boolean isRegularFile();

    boolean exists();

    long getContentLength();

    String getContentEncoding();

    String getContentType();

    String toString();

    String asString();

    String getLocation();

    InputStream getInputStream();

    OutputStream getOutputStream();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    Instant getLastModifiedInstant();

    NutsPath getParent();
}
