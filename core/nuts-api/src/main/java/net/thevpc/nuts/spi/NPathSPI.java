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

import net.thevpc.nuts.NUnsupportedOperationException;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public interface NPathSPI {

    NStream<NPath> list(NPath basePath);

    NPathType type(NPath basePath);

    boolean exists(NPath basePath);

    long contentLength(NPath basePath);

    String toString();

    InputStream getInputStream(NPath basePath, NPathOption... options);

    OutputStream getOutputStream(NPath basePath, NPathOption... options);

    void delete(NPath basePath, boolean recurse);

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

    default NOptional<NPath> toRelative(NPath basePath, NPath parentPath) {
        return null;
    }


    default List<String> getNames(NPath basePath) {
        return null;
    }

    default boolean isLocal(NPath basePath) {
        return true;
    }


    default String getLocation(NPath basePath) {
        return null;
    }

    default String getProtocol(NPath basePath) {
        return null;
    }

    default NFormatSPI formatter(NPath basePath) {
        return null;
    }

    default NPath toAbsolute(NPath basePath, NPath rootPath) {
        return null;
    }

    default boolean isAbsolute(NPath basePath) {
        return true;
    }

    default String getName(NPath basePath) {
        return null;
    }


    default NPath resolve(NPath basePath, String path) {
        return null;
    }

    default NPath resolveSibling(NPath basePath, String path) {
        NPath parent = basePath.getParent();
        return parent.resolve(path);
    }


    default NOptional<URL> toURL(NPath basePath) {
        return NOptional.ofNamedEmpty("url");
    }

    default NOptional<Path> toPath(NPath basePath) {
        return NOptional.ofNamedEmpty("path");
    }


    default NPath normalize(NPath basePath) {
        return null;
    }

    default NPath getParent(NPath basePath) {
        return null;
    }


    default String getContentEncoding(NPath basePath) {
        return null;
    }

    default String getContentType(NPath basePath) {
        return null;
    }

    default String getCharset(NPath basePath) {
        return null;
    }

    default Instant getLastModifiedInstant(NPath basePath) {
        return null;
    }

    default Instant getLastAccessInstant(NPath basePath) {
        return null;
    }

    default Instant getCreationInstant(NPath basePath) {
        return null;
    }

    default String owner(NPath basePath) {
        return null;
    }

    default String group(NPath basePath) {
        return null;
    }

    default Set<NPathPermission> getPermissions(NPath basePath) {
        return Collections.emptySet();
    }

    default void setPermissions(NPath basePath, NPathPermission... permissions) {
        throw new NUnsupportedOperationException(NMsg.ofC("permissions are not supported"));
    }

    default void addPermissions(NPath basePath, NPathPermission... permissions) {
        throw new NUnsupportedOperationException(NMsg.ofC("permissions are not supported"));
    }

    default void removePermissions(NPath basePath, NPathPermission... permissions) {
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

    default NPath toCompressedForm(NPath basePath) {
        return null;
    }


    default boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
        return false;
    }

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

    default Integer compareTo(NPath basePath, NPath other) {
        return null;
    }

    default byte[] getDigest(NPath basePath, String algo) {
        return null;
    }
}
