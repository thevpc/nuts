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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NUnsupportedOperationException;
import net.thevpc.nuts.text.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

/**
 * NPathSPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NPathSPI {

    /**
     * List.
     *
     * @param basePath base path
     * @return list result
     */
    NStream<NPath> list(NPath basePath);

    /**
     * Checks if is hidden.
     *
     * @param basePath base path
     * @return is hidden result
     */
    boolean isHidden(NPath basePath);

    /**
     * Returns the type.
     *
     * @param basePath base path
     * @return get type result
     */
    NPathType getType(NPath basePath);

    /**
     * Exists.
     *
     * @param basePath base path
     * @return exists result
     */
    boolean exists(NPath basePath);

    /**
     * Returns the content length.
     *
     * @param basePath base path
     * @return get content length result
     */
    long getContentLength(NPath basePath);

    String toString();

    /**
     * Returns the input stream.
     *
     * @param basePath base path
     * @param options options
     * @return get input stream result
     */
    InputStream getInputStream(NPath basePath, NPathOption... options);

    /**
     * Returns the output stream.
     *
     * @param basePath base path
     * @param options options
     * @return get output stream result
     */
    OutputStream getOutputStream(NPath basePath, NPathOption... options);

    /**
     * Delete.
     *
     * @param basePath base path
     * @param recurse recurse
     */
    void delete(NPath basePath, boolean recurse);

    /**
     * Mkdir.
     *
     * @param parents parents
     * @param basePath base path
     */
    void mkdir(boolean parents, NPath basePath);

    /**
     * return the root associated to this path
     *
     * @param basePath basePath
     * @return root or this
     */
    NPath getRoot(NPath basePath);


    /// ////////////////////////////////////////////////
    /// DEFAULT IMPLEMENTATIONS

    /**
     * Strip parent.
     *
     * @param basePath base path
     * @param parentPath parent path
     * @return strip parent result
     */
    default NOptional<String> stripParent(NPath basePath, NPath parentPath) {
        return null;
    }

    /**
     * Relativize.
     *
     * @param basePath base path
     * @param parentPath parent path
     * @return relativize result
     */
    default NOptional<String> relativize(NPath basePath, NPath parentPath) {
        return null;
    }


    /**
     * Returns the names.
     *
     * @param basePath base path
     * @return get names result
     */
    default List<String> getNames(NPath basePath) {
        return null;
    }

    /**
     * Checks if is local.
     *
     * @param basePath base path
     * @return is local result
     */
    default boolean isLocal(NPath basePath) {
        return true;
    }


    /**
     * Returns the location.
     *
     * @param basePath base path
     * @return get location result
     */
    default String getLocation(NPath basePath) {
        return null;
    }

    /**
     * Returns the protocol.
     *
     * @param basePath base path
     * @return get protocol result
     */
    default String getProtocol(NPath basePath) {
        return null;
    }

    /**
     * Formatter.
     *
     * @param basePath base path
     * @return formatter result
     */
    default NObjectWriterSPI formatter(NPath basePath) {
        return null;
    }

    /**
     * Converts to absolute.
     *
     * @param basePath base path
     * @param rootPath root path
     * @return to absolute result
     */
    default NPath toAbsolute(NPath basePath, NPath rootPath) {
        return null;
    }

    /**
     * Checks if is absolute.
     *
     * @param basePath base path
     * @return is absolute result
     */
    default boolean isAbsolute(NPath basePath) {
        return true;
    }

    /**
     * Returns the name.
     *
     * @param basePath base path
     * @return get name result
     */
    default String getName(NPath basePath) {
        return null;
    }


    /**
     * Resolve.
     *
     * @param basePath base path
     * @param path path
     * @return resolve result
     */
    default NPath resolve(NPath basePath, String path) {
        return null;
    }

    /**
     * Resolve sibling.
     *
     * @param basePath base path
     * @param path path
     * @return resolve sibling result
     */
    default NPath resolveSibling(NPath basePath, String path) {
        NPath parent = basePath.parent();
        return parent.resolve(path);
    }


    /**
     * Converts to url.
     *
     * @param basePath base path
     * @return to url result
     */
    default NOptional<URL> toURL(NPath basePath) {
        return NOptional.ofNamedEmpty("url");
    }

    /**
     * Converts to path.
     *
     * @param basePath base path
     * @return to path result
     */
    default NOptional<Path> toPath(NPath basePath) {
        return NOptional.ofNamedEmpty("path");
    }


    /**
     * Normalize.
     *
     * @param basePath base path
     * @return normalize result
     */
    default NPath normalize(NPath basePath) {
        return null;
    }

    /**
     * Returns the parent.
     *
     * @param basePath base path
     * @return get parent result
     */
    default NPath getParent(NPath basePath) {
        return null;
    }


    /**
     * Returns the content encoding.
     *
     * @param basePath base path
     * @return get content encoding result
     */
    default String getContentEncoding(NPath basePath) {
        return null;
    }

    /**
     * Returns the content type.
     *
     * @param basePath base path
     * @return get content type result
     */
    default String getContentType(NPath basePath) {
        return null;
    }

    /**
     * Returns the charset.
     *
     * @param basePath base path
     * @return get charset result
     */
    default String getCharset(NPath basePath) {
        return null;
    }

    /**
     * Returns the last modified instant.
     *
     * @param basePath base path
     * @return get last modified instant result
     */
    default Instant getLastModifiedInstant(NPath basePath) {
        return null;
    }

    /**
     * Returns the last access instant.
     *
     * @param basePath base path
     * @return get last access instant result
     */
    default Instant getLastAccessInstant(NPath basePath) {
        return null;
    }

    /**
     * Returns the creation instant.
     *
     * @param basePath base path
     * @return get creation instant result
     */
    default Instant getCreationInstant(NPath basePath) {
        return null;
    }

    /**
     * Returns the owner.
     *
     * @param basePath base path
     * @return get owner result
     */
    default String getOwner(NPath basePath) {
        return null;
    }

    /**
     * Returns the group.
     *
     * @param basePath base path
     * @return get group result
     */
    default String getGroup(NPath basePath) {
        return null;
    }

    /**
     * Returns the permissions.
     *
     * @param basePath base path
     * @return get permissions result
     */
    default Set<NPathPermission> getPermissions(NPath basePath) {
        return Collections.emptySet();
    }

    /**
     * Sets the permissions.
     *
     * @param basePath base path
     * @param permissions permissions
     */
    default void setPermissions(NPath basePath, NPathPermission... permissions) {
        /**
         * N unsupported operation exception.
         *
         * @param supported") supported")
         * @return n unsupported operation exception result
         */
        throw new NUnsupportedOperationException(NMsg.ofC("permissions are not supported"));
    }

    /**
     * Adds the specified permissions.
     *
     * @param basePath base path
     * @param permissions permissions
     */
    default void addPermissions(NPath basePath, NPathPermission... permissions) {
        /**
         * N unsupported operation exception.
         *
         * @param supported") supported")
         * @return n unsupported operation exception result
         */
        throw new NUnsupportedOperationException(NMsg.ofC("permissions are not supported"));
    }

    /**
     * Removes the specified permissions.
     *
     * @param basePath base path
     * @param permissions permissions
     */
    default void removePermissions(NPath basePath, NPathPermission... permissions) {
        /**
         * N unsupported operation exception.
         *
         * @param supported") supported")
         * @return n unsupported operation exception result
         */
        throw new NUnsupportedOperationException(NMsg.ofC("permissions are not supported"));
    }

    /**
     * true if this is the root of the path file system. good examples are: '/'
     * , 'C:\' and 'http://myserver/'
     *
     * @param basePath basePath
     * @return true if this is the root of the path file system
     */
    default Boolean isRoot(NPath basePath) {
        return null;
    }

    /**
     * return null to ask for default implementation
     * return true if this path is a simple name that do not contain '/' or
     * equivalent
     *
     * @param basePath basePath
     * @return true if this path is a simple name that do not contain '/' or
     * equivalent
     */
    default Boolean isName(NPath basePath) {
        return null;
    }

    /**
     * Subpath.
     *
     * @param basePath base path
     * @param beginIndex begin index
     * @param endIndex end index
     * @return subpath result
     */
    default NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return null;
    }


    /**
     * Return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file. The file tree is traversed
     * depth-first, the elements in the stream are Path objects that are
     * obtained as if by resolving the relative path against start.
     *
     * @param basePath basePath
     * @param maxDepth max depth
     * @param options  options
     * @return a Stream that is lazily populated with Path by walking the file
     * tree rooted at a given starting file
     */
    default NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return null;
    }

    /**
     * @param basePath base path
     * @return
     */
    default Integer getNameCount(NPath basePath) {
        return null;
    }

    /**
     * List digest info.
     *
     * @param basePath base path
     * @param algo algo
     * @return list digest info result
     */
    default List<NPathChildDigestInfo> listDigestInfo(NPath basePath, String algo) {
        return null;
    }

    /**
     * Converts to compressed form.
     *
     * @param basePath base path
     * @return to compressed form result
     */
    default NPath toCompressedForm(NPath basePath) {
        return null;
    }


    /**
     * Move to.
     *
     * @param basePath base path
     * @param other other
     * @param options options
     * @return move to result
     */
    default boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
        return false;
    }

    /**
     * Copy to.
     *
     * @param basePath base path
     * @param other other
     * @param options options
     * @return copy to result
     */
    default boolean copyTo(NPath basePath, NPath other, NPathOption... options) {
        return false;
    }

    /**
     * return true if implemented
     *
     * @param basePath basePath
     * @param visitor  visitor
     * @param maxDepth maxDepth
     * @param options  options
     * @return true if implemented, false to trigger default implementation
     */
    default boolean walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        return false;
    }

    /**
     * Compare to.
     *
     * @param basePath base path
     * @param other other
     * @return compare to result
     */
    default Integer compareTo(NPath basePath, NPath other) {
        return null;
    }

    /**
     * Returns the digest.
     *
     * @param basePath base path
     * @param algo algo
     * @return get digest result
     */
    default byte[] getDigest(NPath basePath, String algo) {
        return null;
    }

    /**
     * Reversed lines.
     *
     * @param basePath base path
     * @param charset charset
     * @return reversed lines result
     */
    default NStream<String> reversedLines(NPath basePath, Charset charset) {
        return null;
    }

    /**
     * @since 0.8.9
     */
    default NPathInfo getInfo(NPath basePath) {
        return null;
    }

    /**
     * @since 0.8.9
     */
    default List<NPathInfo> listInfos(NPath basePath) {
        return null;
    }
}
