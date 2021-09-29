/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

/**
 * this interface describes any local or remote file path. It includes simple file path (ex. '/home/here' and 'c:\\here')
 * as well as urls and uri ('ssh://here'), etc.
 * @app.category Input Output
 */
public interface NutsPath extends NutsFormattable {
    static NutsPath of(URL path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.io().path(path);
    }

    static NutsPath of(String path, ClassLoader classLoader, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.io().path(path, classLoader);
    }

    static NutsPath of(File path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.io().path(path);
    }

    static NutsPath of(Path path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.io().path(path);
    }

    static NutsPath of(String path, NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.io().path(path);
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

    NutsPath resolve(String other);

    /**
     * path protocol or null if undefined. This is some how similar to url protocol
     * Particularly file system paths have an empty (aka "") protocol
     * @return path protocol or null if undefined
     */
    String getProtocol();

    NutsPath toCompressedForm();

    URL toURL();

    boolean isURL();

    boolean isFilePath();

    Path toFilePath();


    String toString();

    URL asURL();

    Path asFilePath();

    NutsInput input();

    InputStream inputStream();

    OutputStream outputStream();

    NutsOutput output();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    boolean isDirectory();

    boolean isRegularFile();

    boolean exists();

    long getContentLength();

    Instant getLastModifiedInstant();

    NutsPathBuilder builder();

    Stream<String> lines();

    List<String> head(int count);

    List<String> tail(int count);

}
