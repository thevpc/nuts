package net.thevpc.nuts;

import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;

public interface NutsPath extends NutsFormattable {
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
