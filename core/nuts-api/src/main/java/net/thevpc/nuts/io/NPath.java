/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.io;

import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NWorkspaceProvider;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * this interface describes any local or remote resource path. It includes
 * simple file path (ex. '/home/here' and 'c:\\here') as well as urls and uri
 * ('ssh://here'), etc.
 *
 * @app.category Input Output
 */
public interface NPath extends NFormattable, NInputSource, NOutputTarget, NWorkspaceProvider {

    static NPath of(URL path) {
        return NPaths.of().createPath(path);
    }

    static NPath of(String path, ClassLoader classLoader) {
        return NPaths.of().createPath(path, classLoader);
    }

    static NPath of(File path) {
        return NPaths.of().createPath(path);
    }

    static NPath of(Path path) {
        return NPaths.of().createPath(path);
    }

    static NPath of(String path) {
        return NPaths.of().createPath(path);
    }

    static NPath of(NPathSPI path) {
        return NPaths.of().createPath(path);
    }

    /**
     * return user home path
     *
     * @return user home path
     */
    static NPath ofUserHome() {
        return NPath.of(Paths.get(System.getProperty("user.home")));
    }

    /**
     * return user current directory
     *
     * @return return user current directory
     */
    static NPath ofUserDirectory() {
        return NPath.of(Paths.get(System.getProperty("user.dir")));
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    static NPath ofTempFile(String name) {
        return NPaths.of().ofTempFile(name);
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    static NPath ofTempFile() {
        return NPaths.of().ofTempFile();
    }

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    static NPath ofTempFolder(String name) {
        return NPaths.of().ofTempFolder(name);
    }

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    static NPath ofTempFolder() {
        return NPaths.of().ofTempFolder();
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    static NPath ofTempRepositoryFile(String name, NRepository repository) {
        return NPaths.of().ofTempRepositoryFile(name, repository);
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    static NPath ofTempRepositoryFile(NRepository repository) {
        return NPaths.of().ofTempRepositoryFile(repository);
    }

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    static NPath ofTempRepositoryFolder(String name, NRepository repository) {
        return NPaths.of().ofTempRepositoryFolder(name, repository);
    }

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    static NPath ofTempRepositoryFolder(NRepository repository) {
        return NPaths.of().ofTempRepositoryFolder(repository);
    }

    /**
     * content encoding if explicitly defined (from HTTP headers for instance).
     * return null when unknown.
     *
     * @return content encoding if explicitly defined (from HTTP headers for
     * instance)
     */
    String getContentEncoding();

    /**
     * content type if explicitly defined (from HTTP headers for instance) or
     * probe for content type. return null when unknown.
     *
     * @return content type if explicitly defined (from HTTP headers for
     * instance) or probe for content type.
     */
    String getContentType();

    boolean isUserCache();

    NPath setUserCache(boolean userCache);

    boolean isUserTemporary();

    NPath setUserTemporary(boolean temporary);

    String getBaseName();

    String getSmartBaseName();

    String getSmartExtension();

    String getLongBaseName();

    String getLastExtension();

    String getLongExtension();

    String getName();

    String getLocation();

    NPath resolve(String other);

    NPath resolve(NPath other);

    NPath resolveSibling(String other);

    NPath resolveSibling(NPath other);

    byte[] readBytes(NPathOption... options);

    /**
     * read file content as UTF string
     *
     * @return file content as string
     * @since 0.8.4
     */
    String readString(NPathOption... options);

    /**
     * read file content as string using the given Charset
     *
     * @param cs charset (UTF8 if null)
     * @return file content as string
     * @since 0.8.4
     */
    String readString(Charset cs, NPathOption... options);

    NPath writeBytes(byte[] bytes, NPathOption... options);

    NPath writeString(String string, Charset cs, NPathOption... options);

    NPath writeString(String string, NPathOption... options);

    /**
     * path protocol or null if undefined. This is somehow similar to url
     * protocol Particularly file system paths have an empty (aka "") protocol
     *
     * @return path protocol or null if undefined
     */
    String getProtocol();

    NPath toCompressedForm();

    NOptional<URL> toURL();

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

    NOptional<Path> toPath();

    NOptional<File> toFile();

    String toString();

    NStream<NPath> stream();

    List<NPath> list();

    InputStream getInputStream(NPathOption... options);

    PrintStream getPrintStream();

    PrintStream getPrintStream(Charset cs, NPathOption... options);

    PrintStream getPrintStream(NPathOption... options);

    OutputStream getOutputStream(NPathOption... options);

    Reader getReader(NPathOption... options);

    BufferedReader getBufferedReader(NPathOption... options);

    BufferedReader getBufferedReader(Charset cs, NPathOption... options);

    Reader getReader(Charset cs, NPathOption... options);

    Writer getWriter();

    Writer getWriter(NPathOption... options);

    Writer getWriter(Charset cs, NPathOption... options);

    BufferedWriter getBufferedWriter();

    BufferedWriter getBufferedWriter(NPathOption... options);

    BufferedWriter getBufferedWriter(Charset cs, NPathOption... options);

    NPath delete();

    NPath deleteTree();

    NPath delete(boolean recurse);

    NPath mkdir(boolean parents);

    NPath mkdirs();

    NPath mkdir();

    NPath expandPath(Function<String, String> resolver);

    /**
     * create all parent folders if not existing
     *
     * @return {@code this} instance
     */
    NPath mkParentDirs();

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

    NPath getParent();

    boolean isAbsolute();

    NPath normalize();

    NPath toAbsolute();

    NPath toAbsolute(String basePath);

    NPath toAbsolute(NPath basePath);

    NPath toRelative(NPath basePath);

    String owner();

    String group();

    Set<NPathPermission> getPermissions();

    NPath setPermissions(NPathPermission... permissions);

    NPath addPermissions(NPathPermission... permissions);

    NPath removePermissions(NPathPermission... permissions);

    /**
     * return true if this path is a simple name that do not contain '/' or
     * equivalent
     *
     * @return true if this path is a simple name that do not contain '/' or
     * equivalent
     */
    boolean isName();

    /**
     * return path items count
     *
     * @return path items count
     */
    int getLocationItemsCount();

    /**
     * true if this is the root of the path file system. good examples are: '/'
     * , 'C:\' and 'http://myserver/'
     *
     * @return true if this is the root of the path file system
     */
    boolean isRoot();

    /**
     * Return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file. The file tree is traversed
     * depth-first, the elements in the stream are Path objects that are
     * obtained as if by resolving the relative path against start.
     *
     * @param options options
     * @param maxDepth max depth
     * @return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file
     */
    NStream<NPath> walk(int maxDepth, NPathOption[] options);

    /**
     * Return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file. The file tree is traversed
     * depth-first, the elements in the stream are Path objects that are
     * obtained as if by resolving the relative path against start.
     *
     * @param options options
     * @return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file
     */
    NStream<NPath> walk(NPathOption... options);

    /**
     * Return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file. The file tree is traversed
     * depth-first, the elements in the stream are Path objects that are
     * obtained as if by resolving the relative path against start.
     *
     * @param maxDepth max depth
     * @return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file
     */
    NStream<NPath> walk(int maxDepth);

    /**
     * Return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file. The file tree is traversed
     * depth-first, the elements in the stream are Path objects that are
     * obtained as if by resolving the relative path against start.
     *
     * @return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file
     */
    NStream<NPath> walk();

    NPath subpath(int beginIndex, int endIndex);

    String getLocationItem(int index);

    List<String> getLocationItems();

    void moveTo(NPath other, NPathOption... options);

    void copyTo(NPath other, NPathOption... options);

    void copyFrom(NPath other, NPathOption... options);

    void copyFromInputStream(InputStream other);

    void copyFromReader(Reader other);
    
    void copyFromReader(Reader other,Charset charset, NPathOption... options);

    void copyToOutputStream(OutputStream other);

    void copyToPrintStream(PrintStream other);

    void copyToPrintStream(PrintStream other, Charset cs);

    void copyToWriter(Writer other);

    void copyToWriter(Writer other, Charset cs);

    NPath getRoot();

    NPath walkDfs(NTreeVisitor<NPath> visitor, NPathOption... options);

    NPath walkDfs(NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options);

    NStream<NPath> walkGlob(NPathOption... options);

    /**
     * return true if this is a valid URL with http or https scheme
     *
     * @return true if this is a valid URL with http or https scheme
     */
    boolean isHttp();

    NPath copy();

    void setDeleteOnDispose(boolean deleteOnDispose);

    boolean isDeleteOnDispose();

}
