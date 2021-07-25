package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

public interface NutsPathSPI {
    default NutsFormat formatter() {
        return null;
    }

    default String name() {
        return null;
    }

    default NutsPath compressedForm() {
        return null;
    }

    default URL toURL() {
        return null;
    }

    default Path toFilePath() {
        return null;
    }

    boolean exists();

    long length();

    String toString();

    NutsString asFormattedString();
    String asString();

    String location();

    InputStream inputStream();

    OutputStream outputStream();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    Instant lastModifiedInstant();

}
