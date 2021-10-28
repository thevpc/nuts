package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsPathPermission;
import net.thevpc.nuts.NutsSession;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFileAttributes;
import java.time.Instant;
import java.util.Set;

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

    default Path toFile() {
        return null;
    }

    boolean isSymbolicLink();

    boolean isOther();

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
    Instant getLastAccessInstant();
    Instant getCreationInstant();

    NutsPath getParent();

    NutsPath toAbsolute(NutsPath basePath);

    NutsPath normalize();

    boolean isAbsolute() ;
    String owner();

    String group();

    Set<NutsPathPermission> permissions();

    void setPermissions(NutsPathPermission... permissions);
    void addPermissions(NutsPathPermission... permissions);
    void removePermissions(NutsPathPermission... permissions);

}
