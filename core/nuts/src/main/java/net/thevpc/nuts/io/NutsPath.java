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
package net.thevpc.nuts.io;

import net.thevpc.nuts.NutsFormattable;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.format.NutsTreeVisitor;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.util.NutsStream;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public interface NutsPath extends NutsFormattable, NutsInputSource, NutsOutputTarget {
    static NutsPath of(URL path, NutsSession session) {
        return NutsPaths.of(session).createPath(path);
    }

    static NutsPath of(String path, ClassLoader classLoader, NutsSession session) {
        return NutsPaths.of(session).createPath(path, classLoader);
    }

    static NutsPath of(File path, NutsSession session) {
        return NutsPaths.of(session).createPath(path);
    }

    static NutsPath of(Path path, NutsSession session) {
        return NutsPaths.of(session).createPath(path);
    }

    static NutsPath of(String path, NutsSession session) {
        return NutsPaths.of(session).createPath(path);
    }

    static NutsPath of(NutsPathSPI path, NutsSession session) {
        return NutsPaths.of(session).createPath(path);
    }

    /**
     * return user home path
     *
     * @param session session
     * @return user home path
     */
    static NutsPath ofUserHome(NutsSession session) {
        return NutsPath.of(Paths.get(System.getProperty("user.home")), session);
    }

    /**
     * return user current directory
     *
     * @param session session
     * @return return user current directory
     */
    static NutsPath ofUserDirectory(NutsSession session) {
        return NutsPath.of(Paths.get(System.getProperty("user.dir")), session);
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

    boolean isUserCache();

    NutsPath setUserCache(boolean userCache);

    boolean isUserTemporary();

    NutsPath setUserTemporary(boolean temporary);

    String getBaseName();

    String getSmartBaseName();

    String getSmartExtension();

    String getLongBaseName();

    String getLastExtension();

    String getLongExtension();

    String getName();

    String getLocation();

    NutsPath resolve(String other);

    NutsPath resolve(NutsPath other);

    NutsPath resolveSibling(String other);

    NutsPath resolveSibling(NutsPath other);

    byte[] readBytes();

    /**
     * read file content as UTF string
     *
     * @return file content as string
     * @since 0.8.4
     */
    String readString();

    /**
     * read file content as string using the given Charset
     *
     * @param cs charset (UTF8 if null)
     * @return file content as string
     * @since 0.8.4
     */
    String readString(Charset cs);


    NutsPath writeBytes(byte[] bytes);

    NutsPath writeString(String string, Charset cs);

    NutsPath writeString(String string);

    /**
     * path protocol or null if undefined. This is somehow similar to url protocol
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

    Reader getReader(Charset cs);

    Stream<String> getLines(Charset cs);

    Stream<String> getLines();

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
     *
     * @return {@code this} instance
     */
    NutsPath mkParentDirs();

    boolean isOther();

    boolean isSymbolicLink();

    boolean isDirectory();

    boolean isRegularFile();

    boolean isRemote();

    boolean isLocal();

    boolean exists();

    long getContentLength();

    Instant getLastModifiedInstant();

    Instant getLastAccessInstant();

    Instant getCreationInstant();

    Stream<String> lines();

    List<String> head(int count);

    List<String> tail(int count);

    NutsPath getParent();

    boolean isAbsolute();

    NutsPath normalize();

    NutsPath toAbsolute();

    NutsPath toAbsolute(String basePath);

    NutsPath toAbsolute(NutsPath basePath);

    NutsPath toRelativePath(NutsPath basePath);

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

    List<String> getItems();

    void moveTo(NutsPath other, NutsPathOption... options);

    void copyTo(NutsPath other, NutsPathOption... options);

    NutsPath getRoot();

    NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, NutsPathOption... options);

    NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options);

    NutsStream<NutsPath> walkGlob(NutsPathOption... options);

    /**
     * return true if this is a valid URL with http or https scheme
     *
     * @return true if this is a valid URL with http or https scheme
     */
    boolean isHttp();

    NutsPath copy();
}
