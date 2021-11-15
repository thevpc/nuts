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
import net.thevpc.nuts.spi.NutsPathFactory;
import net.thevpc.nuts.spi.NutsPaths;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * this interface describes any local or remote resource path. It includes simple file path (ex. '/home/here' and 'c:\\here')
 * as well as urls and uri ('ssh://here'), etc.
 *
 * @app.category Input Output
 */
public interface NutsPath extends NutsFormattable {
    static NutsPath of(URL path, NutsSession session) {
        return NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .createPath(path, session);
    }

    static NutsPath of(String path, ClassLoader classLoader, NutsSession session) {
        return NutsPaths.of(session).createPath(path, classLoader, session);
    }

    static NutsPath of(File path, NutsSession session) {
        return NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .createPath(path, session);
    }

    static NutsPath of(Path path, NutsSession session) {
        return NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .createPath(path, session);
    }

    static NutsPath of(String path, NutsSession session) {
        return NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .createPath(path, session);
    }

    static void addPathFactory(NutsPathFactory pathFactory, NutsSession session) {
        NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .addPathFactory(pathFactory, session);
    }

    static void removePathFactory(NutsPathFactory pathFactory, NutsSession session) {
        NutsApiUtils
                .createSessionCachedType(NutsPaths.class, session, () -> NutsPaths.of(session))
                .removePathFactory(pathFactory, session);
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

    String getLocation();

    NutsPath resolve(String other);

    NutsPath resolve(NutsPath other);

    NutsPath resolveSibling(String other);

    NutsPath resolveSibling(NutsPath other);

    byte[] readAllBytes();

    NutsPath writeBytes(byte[] bytes);

    /**
     * path protocol or null if undefined. This is some how similar to url protocol
     * Particularly file system paths have an empty (aka "") protocol
     *
     * @return path protocol or null if undefined
     */
    String getProtocol();

    NutsPath toCompressedForm();

    URL toURL();

    /**
     * return true if the path is or can be converted to a valid url
     *
     * @return true if the path is or can be converted to a valid url
     */
    boolean isURL();

    /**
     * return true if the path is or can be converted to a valid local file
     *
     * @return true if the path is or can be converted to a valid local file
     */
    boolean isFile();

    Path toFile();


    String toString();

    /**
     * return a valid url or null
     *
     * @return a valid url or null
     */
    URL asURL();

    /**
     * return a valid local file
     *
     * @return return a valid local file or null
     */
    Path asFile();

    NutsStream<NutsPath> list();

    InputStream getInputStream();

    OutputStream getOutputStream();

    Reader getReader();

    Writer getWriter();

//    NutsOutput output();

    NutsSession getSession();

    NutsPath delete();

    NutsPath deleteTree();

    NutsPath delete(boolean recurse);

    NutsPath mkdir(boolean parents);

    NutsPath mkdirs();

    NutsPath mkdir();

    NutsPath expandPath(Function<String, String> resolver);

    /**
     * create all parent folders if not existing
     * @return {@code this} instance
     */
    NutsPath mkParentDirs();

    boolean isOther();

    boolean isSymbolicLink();

    boolean isDirectory();

    boolean isRegularFile();

    boolean exists();

    long getContentLength();

    Instant getLastModifiedInstant();

    Instant getLastAccessInstant();

    Instant getCreationInstant();

    Stream<String> lines();

    List<String> head(int count);

    List<String> tail(int count);

    NutsPath getParent();

    String getUserKind();

    NutsPath setUserKind(String userKind);

    boolean isAbsolute();

    NutsPath normalize();

    NutsPath toAbsolute();

    NutsPath toAbsolute(String basePath);

    NutsPath toAbsolute(NutsPath basePath);

    String owner();

    String group();

    Set<NutsPathPermission> getPermissions();

    NutsPath setPermissions(NutsPathPermission... permissions);

    NutsPath addPermissions(NutsPathPermission... permissions);

    NutsPath removePermissions(NutsPathPermission... permissions);

    /**
     * return true if this path is a simple name that do not contain '/' or equivalent
     *
     * @return true if this path is a simple name that do not contain '/' or equivalent
     */
    boolean isName();

    /**
     * return path items count
     *
     * @return path items count
     */
    int getPathCount();

    /**
     * true if this is the root of the path file system.
     * good examples are:
     * '/' , 'C:\' and 'http://myserver/'
     *
     * @return true if this is the root of the path file system
     */
    boolean isRoot();


    /**
     * Return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file.
     * The file tree is traversed depth-first, the elements in the stream are Path objects that are obtained as if by resolving the relative path against start.
     *
     * @param options  options
     * @param maxDepth max depth
     * @return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file
     */
    NutsStream<NutsPath> walk(int maxDepth, NutsPathOption[] options);

    /**
     * Return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file.
     * The file tree is traversed depth-first, the elements in the stream are Path objects that are obtained as if by resolving the relative path against start.
     *
     * @param options options
     * @return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file
     */
    NutsStream<NutsPath> walk(NutsPathOption... options);

    /**
     * Return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file.
     * The file tree is traversed depth-first, the elements in the stream are Path objects that are obtained as if by resolving the relative path against start.
     *
     * @param maxDepth max depth
     * @return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file
     */
    NutsStream<NutsPath> walk(int maxDepth);

    /**
     * Return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file.
     * The file tree is traversed depth-first, the elements in the stream are Path objects that are obtained as if by resolving the relative path against start.
     *
     * @return a Stream that is lazily populated with Path by walking the file tree rooted at a given starting file
     */
    NutsStream<NutsPath> walk();

    NutsPath subpath(int beginIndex, int endIndex);

    String getItem(int index);

    String[] getItems();

    void moveTo(NutsPath other, NutsPathOption... options);

    void copyTo(NutsPath other, NutsPathOption... options);

    NutsPath getRoot();

    NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, NutsPathOption... options);

    NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor,int maxDepth, NutsPathOption... options);
}
