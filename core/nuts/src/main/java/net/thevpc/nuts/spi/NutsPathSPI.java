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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Set;

public interface NutsPathSPI {

    NutsStream<NutsPath> list(NutsPath basePath);

    NutsFormatSPI formatter(NutsPath basePath);

    String getName(NutsPath basePath);

    String getProtocol(NutsPath basePath);

    NutsPath resolve(NutsPath basePath, String path);

    NutsPath resolve(NutsPath basePath, NutsPath path);

    NutsPath resolveSibling(NutsPath basePath, String path);

    NutsPath resolveSibling(NutsPath basePath, NutsPath path);

    NutsPath toCompressedForm(NutsPath basePath);

    URL toURL(NutsPath basePath);

    Path toFile(NutsPath basePath);

    boolean isSymbolicLink(NutsPath basePath);

    boolean isOther(NutsPath basePath);

    boolean isDirectory(NutsPath basePath);

    boolean isLocal(NutsPath basePath);

    boolean isRegularFile(NutsPath basePath);

    boolean exists(NutsPath basePath);

    long getContentLength(NutsPath basePath);

    String getContentEncoding(NutsPath basePath);

    String getContentType(NutsPath basePath);

    String toString();

    String getLocation(NutsPath basePath);

    InputStream getInputStream(NutsPath basePath);

    OutputStream getOutputStream(NutsPath basePath);

    NutsSession getSession();

    void delete(NutsPath basePath, boolean recurse);

    void mkdir(boolean parents, NutsPath basePath);

    Instant getLastModifiedInstant(NutsPath basePath);

    Instant getLastAccessInstant(NutsPath basePath);

    Instant getCreationInstant(NutsPath basePath);

    NutsPath getParent(NutsPath basePath);

    NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath);

    NutsPath normalize(NutsPath basePath);

    boolean isAbsolute(NutsPath basePath);

    String owner(NutsPath basePath);

    String group(NutsPath basePath);

    Set<NutsPathPermission> getPermissions(NutsPath basePath);

    void setPermissions(NutsPath basePath, NutsPathPermission... permissions);

    void addPermissions(NutsPath basePath, NutsPathPermission... permissions);

    void removePermissions(NutsPath basePath, NutsPathPermission... permissions);

    /**
     * return true if this path is a simple name that do not contain '/' or
     * equivalent
     *
     * @param basePath basePath
     * @return true if this path is a simple name that do not contain '/' or
     * equivalent
     */
    boolean isName(NutsPath basePath);

    /**
     * return path items count
     *
     * @param basePath basePath
     * @return path items count
     */
    int getPathCount(NutsPath basePath);

    /**
     * true if this is the root of the path file system. good examples are: '/'
     * , 'C:\' and 'http://myserver/'
     *
     * @param basePath basePath
     * @return true if this is the root of the path file system
     */
    boolean isRoot(NutsPath basePath);

    /**
     * return the root associated to this path
     *
     * @param basePath basePath
     * @return root or this
     */
    NutsPath getRoot(NutsPath basePath);

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
    NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options);

    NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex);

    String[] getItems(NutsPath basePath);

    void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options);

    void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options);

    void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options);

    NutsPath toRelativePath(NutsPath basePath, NutsPath parentPath);
}
