/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.internal.util.NBootPlatformHome;
import net.thevpc.nuts.core.NLocationKey;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.internal.rpi.NIORPI;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NTreeVisitor;
import net.thevpc.nuts.spi.NPathSPI;

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
 * Represents a unified abstraction of a resource location.
 * <p>
 * An {@code NPath} models both local and remote paths in a uniform, protocol-aware
 * way. It can represent:
 * </p>
 * <ul>
 *     <li>Standard filesystem paths (e.g., {@code "/home/user"} or {@code "C:\\temp"})</li>
 *     <li>Classloader resources</li>
 *     <li>Network paths and URLs/URIs (e.g., {@code "ssh://host/path"}, {@code "http://..."} )</li>
 *     <li>Nuts-specific virtual locations, including workspace stores, repositories,
 *         temporary folders, and ID-based storage</li>
 * </ul>
 *
 * <p>
 * {@code NPath} exposes a high-level API that:
 * </p>
 * <ul>
 *     <li>Allows reading and writing in multiple forms (bytes, strings, streams).</li>
 *     <li>Supports automatic parent creation, deletion, walking, normalization and
 *         conversion to {@link java.nio.file.Path}, {@link java.io.File}, or {@link java.net.URL}
 *         when possible.</li>
 *     <li>Recognizes protocols, permissions, symbolic links, and remote/local semantics.</li>
 *     <li>Provides Nuts-aware utilities: temporary resources, repository storage,
 *         ID-based paths, user/system store access, and content metadata.</li>
 * </ul>
 *
 * <p>
 * All factory methods delegate to {@link NIO#of()}, which selects the correct internal
 * provider based on the given input. Providers handle path resolution, permissions,
 * streaming, and protocol behavior.
 * </p>
 *
 * <h3>Key Characteristics</h3>
 * <p>
 * An {@code NPath} is:
 * </p>
 * <ul>
 *     <li><strong>Immutable:</strong> Operations such as {@code resolve}, {@code normalize},
 *         or {@code toAbsolute} return new path instances.</li>
 *     <li><strong>Protocol-aware:</strong> It preserves the original scheme
 *         (e.g., {@code file}, {@code ssh}, {@code http}, or {@code ""} for local filesystem).</li>
 *     <li><strong>Compatible with NInputSource/NOutputTarget:</strong> This makes paths
 *         first-class citizens in the Nuts IO architecture, suitable for streaming,
 *         formatting, piping, and writing structured formats.</li>
 * </ul>
 *
 * <h3>Usage Examples</h3>
 *
 * <pre>
 * // Create from a local file
 * NPath p = NPath.of("/home/user/data.txt");
 * String content = p.readString();
 *
 * // Create from an URL
 * NPath remote = NPath.of(new URL("https://example.com/data.json"));
 *
 * // Resolve children
 * NPath logs = NPath.ofUserHome().resolve("logs/app.log");
 *
 * // Temporary workspace file
 * NPath tmp = NPath.ofTempFile("buffer.bin");
 *
 * // Repository-scoped temp folder
 * NPath repTmp = NPath.ofTempRepositoryFolder(repository);
 *
 * // Convert to standard Java types when possible
 * Optional<Path> nioPath = p.toPath().asOptional();
 *
 * // Walk a directory tree
 * p.walk(5, NPathOption.NONE)
 *  .filter(NPath::isRegularFile)
 *  .forEach(System.out::println);
 * </pre>
 *
 * <h3>Comparison and Ordering</h3>
 * <p>
 * {@code NPath} implements {@link Comparable}, using a provider-defined stable ordering
 * that typically compares normalized textual forms. This does not imply any filesystem
 * hierarchy ordering semantics.
 * </p>
 *
 * <h3>Error Handling</h3>
 * <p>
 * Most read/write methods throw unchecked Nuts exceptions wrapping I/O failures.
 * Path manipulation methods never throw for normal logical operations
 * (resolve, normalize, toAbsolute), and instead delegate failures to provider semantics.
 * </p>
 *
 * @app.category Input Output
 */
public interface NPath extends NInputSource, NOutputTarget, Comparable<NPath> {

    /**
     * Creates an {@code NPath} from the given URL.
     * <p>
     * The underlying {@link NIO} provider determines how the URL is interpreted.
     * Supported schemes typically include {@code file:}, {@code http:}, {@code https:},
     * {@code jar:}, and provider-specific protocols.
     * </p>
     *
     * @param path the URL to convert; may be {@code null}
     * @return a new {@code NPath} instance representing the URL
     */
    static NPath of(URL path) {
        return NIORPI.of().createPath(path);
    }

    /**
     * Creates an {@code NPath} from a connection string.
     * <p>
     * The {@link NConnectionString} is first converted to its canonical string
     * representation, then interpreted by the underlying {@link NIO} provider.
     * </p>
     *
     * @param path the connection string to convert; may be {@code null}
     * @return a new {@code NPath} instance representing the connection string
     */
    static NPath of(NConnectionString path) {
        return NIORPI.of().createPath(path == null ? null : path.toString());
    }

    /**
     * Creates an {@code NPath} from a textual location using the given class loader
     * to resolve classpath-based paths when applicable.
     *
     * @param path         the textual location (file, URL, classpath resource)
     * @param classLoader  optional class loader for resource resolution
     * @return a new {@code NPath} instance
     */
    static NPath of(String path, ClassLoader classLoader) {
        return NIORPI.of().createPath(path, classLoader);
    }

    /**
     * Creates an {@code NPath} from a {@link File}.
     * The resulting path represents the local filesystem entry.
     *
     * @param path the file system path; may not exist
     * @return an {@code NPath} instance pointing to the file
     */
    static NPath of(File path) {
        return NIORPI.of().createPath(path);
    }

    /**
     * Creates an {@code NPath} from a {@link java.nio.file.Path}.
     *
     * @param path the NIO file system path
     * @return an {@code NPath} instance representing the underlying path
     */
    static NPath of(Path path) {
        return NIORPI.of().createPath(path);
    }

    /**
     * Creates an {@code NPath} from a string location.
     * <p>
     * The location may refer to a filesystem path, an URL, a Nuts-specific
     * protocol, or a classpath resource. The interpretation is delegated
     * to the active {@link NIO} provider.
     * </p>
     *
     * @param path the raw location
     * @return an {@code NPath} for the location
     */
    static NPath of(String path) {
        return NIORPI.of().createPath(path);
    }

    /**
     * Wraps a low-level provider implementation into an {@code NPath}.
     *
     * @param path an existing {@link NPathSPI} instance
     * @return the public {@code NPath} wrapper
     */
    static NPath of(NPathSPI path) {
        return NIORPI.of().createPath(path);
    }

    /**
     * Returns the user's home directory as an {@code NPath}.
     *
     * @return the platform home directory
     */
    static NPath ofUserHome() {
        return NPath.of(Paths.get(System.getProperty("user.home")));
    }

    /**
     * Resolves a path to a standard store location specific to the given application identifier (GAV)
     * and store type. This method provides a convenient way to access predefined directories
     * for application-related resources such as binaries, configuration, data, logs, temporary
     * files, caches, libraries, or runtime objects.
     * <p>
     * The {@link NId} represents the unique GAV (Group, Artifact, Version) identifier of the
     * application, while {@link NStoreType} specifies the type of store:
     * <ul>
     *     <li>{@link NStoreType#BIN} – Application binaries/executables.</li>
     *     <li>{@link NStoreType#CONF} – Configuration files, equivalent to $XDG_CONFIG_HOME on Linux.</li>
     *     <li>{@link NStoreType#VAR} – Variable/modifiable data files, equivalent to $XDG_DATA_HOME on Linux.</li>
     *     <li>{@link NStoreType#LOG} – Log files, equivalent to $XDG_LOG_HOME on Linux.</li>
     *     <li>{@link NStoreType#TEMP} – Temporary files.</li>
     *     <li>{@link NStoreType#CACHE} – Cached non-essential data, libraries, or packages.</li>
     *     <li>{@link NStoreType#LIB} – Non-executable libraries or packages.</li>
     *     <li>{@link NStoreType#RUN} – Temporary runtime files, sockets, or named pipes, equivalent to $XDG_RUNTIME_DIR on Linux.</li>
     * </ul>
     * </p>
     * <p>
     * This method guarantees that the returned {@link NPath} points to a location that is consistent
     * across executions and platforms, enabling applications to store or retrieve files in a
     * standard, predictable manner based on their GAV.
     * </p>
     *
     * @param id the application identifier (GAV) to associate with the store
     * @param storeType the type of store specifying the nature of resources to access
     * @return a non-null {@link NPath} pointing to the standard store location for the given GAV and store type
     * @throws NullPointerException if {@code id} or {@code storeType} is null or blank
     * @see NStoreType
     */
    static NPath ofIdStore(NId id, NStoreType storeType) {
        NAssert.requireNonBlank(id, "id");
        NAssert.requireNonBlank(storeType, "storeType");
        return NWorkspace.of().getStoreLocation(id, storeType);
    }

    /**
     * Returns a workspace-level store path for the given store type.
     *
     * @param storeType type of store
     * @return the workspace’s store path
     */
    static NPath ofWorkspaceStore(NStoreType storeType) {
        NAssert.requireNonBlank(storeType, "storeType");
        return NWorkspace.of().getStoreLocation(storeType);
    }

    /**
     * Returns the resolved store path corresponding to the given location key.
     *
     * @param locationKey store identification key
     * @return the resolved store directory
     */
    static NPath ofStore(NLocationKey locationKey) {
        NAssert.requireNonBlank(locationKey, "locationKey");
        return NWorkspace.of().getStoreLocation(locationKey);
    }

    /**
     * Returns the user-level Nuts store directory for the given store type.
     * <p>
     * This does not depend on the workspace; it uses platform-wide user
     * configuration (e.g., {@code ~/.config/nuts/...}).
     * </p>
     *
     * @param storeType store type
     * @return user store path
     */
    static NPath ofUserStore(NStoreType storeType) {
        NAssert.requireNonBlank(storeType, "storeType");
        return NPath.of(NBootPlatformHome.of(null).getStore(storeType.id()));
    }

    /**
     * Returns the system-level Nuts store directory for the given store type.
     *
     * @param storeType store type
     * @return system store path
     */
    static NPath ofSystemStore(NStoreType storeType) {
        NAssert.requireNonBlank(storeType, "storeType");
        return NPath.of(NBootPlatformHome.ofSystem(null).getStore(storeType.id()));
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
        return NIORPI.of().ofTempFile(name);
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    static NPath ofTempFile() {
        return NIORPI.of().ofTempFile();
    }

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    static NPath ofTempFolder(String name) {
        return NIORPI.of().ofTempFolder(name);
    }

    /**
     * create temp folder in the workspace's temp folder
     *
     * @return newly created temp folder
     */
    static NPath ofTempFolder() {
        return NIORPI.of().ofTempFolder();
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    static NPath ofTempRepositoryFile(String name, NRepository repository) {
        return NIORPI.of().ofTempRepositoryFile(name, repository);
    }

    /**
     * create temp file in the repositoryId's temp folder
     *
     * @return newly created file path
     */
    static NPath ofTempRepositoryFile(NRepository repository) {
        return NIORPI.of().ofTempRepositoryFile(repository);
    }

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    static NPath ofTempRepositoryFolder(String name, NRepository repository) {
        return NIORPI.of().ofTempRepositoryFolder(name, repository);
    }

    /**
     * create temp folder in the repository's temp folder
     *
     * @return newly created temp folder
     */
    static NPath ofTempRepositoryFolder(NRepository repository) {
        return NIORPI.of().ofTempRepositoryFolder(repository);
    }


    /**
     * create temp file in the id's temp folder
     *
     * @param name file name
     * @return newly created file path
     */
    static NPath ofTempIdFile(String name, NId id) {
        return NIORPI.of().ofTempIdFile(name, id);
    }

    /**
     * create temp file in the id's temp folder
     *
     * @return newly created file path
     */
    static NPath ofTempIdFile(NId id) {
        return NIORPI.of().ofTempIdFile(id);
    }

    /**
     * create temp folder in the id's temp folder
     *
     * @param name folder name
     * @return newly created temp folder
     */
    static NPath ofTempIdFolder(String name, NId id) {
        return NIORPI.of().ofTempIdFolder(name, id);
    }

    /**
     * create temp folder in the id's temp folder
     *
     * @return newly created temp folder
     */
    static NPath ofTempIdFolder(NId id) {
        return NIORPI.of().ofTempIdFolder(id);
    }

    /**
     * content encoding if explicitly defined (from HTTP headers for instance).
     * return null when unknown.
     *
     * @return content encoding if explicitly defined (from HTTP headers for
     * instance)
     */
    String contentEncoding();

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

    NPath resolveSibling(NPathRenameOptions renameOptions);


    /**
     * Returns the name parts of this path using the default extension type strategy.
     * <p>
     * This is equivalent to calling {@code nameParts(NPathExtensionType.SHORT)}.
     * </p>
     *
     * @return an {@link NPathNameParts} object containing the base name, extension, and full extension
     */
    NPathNameParts nameParts();


    /**
     * Returns the name parts of this path using the specified extension type strategy.
     * <p>
     * The strategy determines how the extension is extracted from the file name:
     * <ul>
     *   <li>{@code SHORT} – Uses the last dot to determine the extension.</li>
     *   <li>{@code LONG} – Uses the first dot to determine the extension.</li>
     *   <li>{@code SMART} – Uses a version-aware or heuristic-based approach.</li>
     * </ul>
     * </p>
     *
     * @param type the extension type strategy to use; if {@code null}, defaults to {@code SHORT}
     * @return an {@link NPathNameParts} object containing the base name, extension, and full extension
     */

    NPathNameParts nameParts(NPathExtensionType type);

    String getName();

    String getLocation();

    NPath resolve(String other);

    /**
     * same as resolve but will ignore any leading '/' or '\' in the given child
     *
     * @param other other location
     * @return NPath for the child given other location
     */
    NPath resolveChild(String other);

    /**
     * same as resolve but will ignore any leading '/' or '\' in the given child
     *
     * @param other other location
     * @return NPath for the child given other location
     */
    NPath resolveChild(NPath other);

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

    default NPath writeObject(Object any, NPathOption... options) {
        try (NPrintStream out = this.getNPrintStream(options)) {
            out.print(any);
        }
        return this;
    }

    default NPath writeMsg(NMsg any, NPathOption... options) {
        try (NPrintStream out = this.getNPrintStream(options)) {
            out.print(any);
        }
        return this;
    }

    default NPath writeText(NText any, NPathOption... options) {
        try (NPrintStream out = this.getNPrintStream(options)) {
            out.print(any);
        }
        return this;
    }


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

    List<NPathInfo> listInfos();

    InputStream getInputStream(NPathOption... options);

    PrintStream getPrintStream();

    PrintStream getPrintStream(Charset cs, NPathOption... options);

    PrintStream getPrintStream(NPathOption... options);

    NPrintStream getNPrintStream(NPathOption... options);

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

    /**
     * ensure that the folder or the file exists and is empty.
     * If the file or the folder does not exist it will be created
     * If the file or the folder does exist it will be emptied
     *
     * @return current path
     */
    NPath ensureEmptyDirectory();

    /**
     * ensure that the folder or the file exists and is empty.
     * If the file or the folder does not exist it will be created
     * If the file or the folder does exist it will be emptied
     *
     * @return current path
     */
    NPath ensureEmptyFile();

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

    NPathType type();

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

    NOptional<String> toRelative(NPath basePath);

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
     * @param options  options
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

    String getName(int index);

    List<String> getNames();

    void moveTo(NPath other, NPathOption... options);

    void copyTo(NPath other, NPathOption... options);

    void copyFrom(NPath other, NPathOption... options);

    void copyFromInputStream(InputStream other, NPathOption... options);

    void copyFromInputStreamProvider(NInputStreamProvider other, NPathOption... options);

    void copyFromReader(Reader other, NPathOption... options);

    void copyFromReader(Reader other, Charset charset, NPathOption... options);

    void copyToOutputStream(OutputStream other, NPathOption... options);

    void copyToPrintStream(PrintStream other, NPathOption... options);

    void copyToPrintStream(PrintStream other, Charset cs, NPathOption... options);

    void copyToWriter(Writer other, NPathOption... options);

    void copyToWriter(Writer other, Charset cs, NPathOption... options);

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

    boolean isEqOrDeepChildOf(NPath other);

    boolean startsWith(NPath other);

    boolean startsWith(String other);

    int getNameCount();

    int compareTo(NPath other);

    List<NPathChildDigestInfo> listDigestInfo();

    List<NPathChildDigestInfo> listDigestInfo(String algo);

    List<NPathChildStringDigestInfo> listStringDigestInfo();

    List<NPathChildStringDigestInfo> listStringDigestInfo(String algo);

    /**
     *
     * @since 0.8.9
     */
    NPathInfo getInfo();
}
