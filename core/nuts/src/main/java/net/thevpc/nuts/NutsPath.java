package net.thevpc.nuts;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

public interface NutsPath extends NutsFormattable {
    static NutsPath of(URL path, NutsSession session) {
        PrivateNutsUtils.checkSession(session);
        return session.getWorkspace().io().path(path);
    }

    static NutsPath of(String path, ClassLoader classLoader, NutsSession session) {
        PrivateNutsUtils.checkSession(session);
        return session.getWorkspace().io().path(path, classLoader);
    }

    static NutsPath of(File path, NutsSession session) {
        PrivateNutsUtils.checkSession(session);
        return session.getWorkspace().io().path(path);
    }

    static NutsPath of(String path, NutsSession session) {
        PrivateNutsUtils.checkSession(session);
        return session.getWorkspace().io().path(path);
    }

    /**
     * content encoding if explicitly defined (from HTTP headers for instance).
     * return null when unknown.
     *
     * @return content encoding if explicitly defined (from HTTP headers for instance)
     */
    String getContentEncoding();

    /**
     * content type if explicitly defined (from HTTP headers for instance) or probe for content type.
     * return null when unknown.
     *
     * @return content type if explicitly defined (from HTTP headers for instance) or probe for content type.
     */
    String getContentType();

    NutsString getFormattedName();

    String getBaseName();

    String getLastExtension();

    String getFullExtension();

    String getName();

    String asString();

    String getLocation();

    NutsPath toCompressedForm();

    URL toURL();

    boolean isURL();

    boolean isFilePath();

    Path toFilePath();


    String toString();

    URL asURL();

    Path asFilePath();

    NutsInput input();

    NutsOutput output();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    boolean exists();

    long getContentLength();

    Instant getLastModifiedInstant();
}
