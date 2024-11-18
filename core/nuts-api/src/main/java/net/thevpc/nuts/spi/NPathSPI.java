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

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface NPathSPI {

    NStream<NPath> list(NPath basePath);

    NFormatSPI formatter(NPath basePath);

    String getName(NPath basePath);

    String getProtocol(NPath basePath);

    NPath resolve(NPath basePath, String path);

    NPath resolve(NPath basePath, NPath path);

    NPath resolveSibling(NPath basePath, String path);

    NPath resolveSibling(NPath basePath, NPath path);

    NPath toCompressedForm(NPath basePath);

    NOptional<URL> toURL(NPath basePath);

    NOptional<Path> toPath(NPath basePath);

    boolean isSymbolicLink(NPath basePath);

    boolean isOther(NPath basePath);

    boolean isDirectory(NPath basePath);

    boolean isLocal(NPath basePath);

    boolean isRegularFile(NPath basePath);

    boolean exists(NPath basePath);

    long getContentLength(NPath basePath);

    String getContentEncoding(NPath basePath);

    String getContentType(NPath basePath);

    String getCharset(NPath basePath);

    String toString();

    String getLocation(NPath basePath);

    InputStream getInputStream(NPath basePath, NPathOption... options);

    OutputStream getOutputStream(NPath basePath, NPathOption... options);

    void delete(NPath basePath, boolean recurse);

    void mkdir(boolean parents, NPath basePath);

    Instant getLastModifiedInstant(NPath basePath);

    Instant getLastAccessInstant(NPath basePath);

    Instant getCreationInstant(NPath basePath);

    NPath getParent(NPath basePath);

    NPath toAbsolute(NPath basePath, NPath rootPath);

    NPath normalize(NPath basePath);

    boolean isAbsolute(NPath basePath);

    String owner(NPath basePath);

    String group(NPath basePath);

    Set<NPathPermission> getPermissions(NPath basePath);

    void setPermissions(NPath basePath, NPathPermission... permissions);

    void addPermissions(NPath basePath, NPathPermission... permissions);

    void removePermissions(NPath basePath, NPathPermission... permissions);

    /**
     * return true if this path is a simple name that do not contain '/' or
     * equivalent
     *
     * @param basePath basePath
     * @return true if this path is a simple name that do not contain '/' or
     * equivalent
     */
    boolean isName(NPath basePath);

    /**
     * return path items count
     *
     * @param basePath basePath
     * @return path items count
     */
    int getLocationItemsCount(NPath basePath);

    /**
     * true if this is the root of the path file system. good examples are: '/'
     * , 'C:\' and 'http://myserver/'
     *
     * @param basePath basePath
     * @return true if this is the root of the path file system
     */
    boolean isRoot(NPath basePath);

    /**
     * return the root associated to this path
     *
     * @param basePath basePath
     * @return root or this
     */
    NPath getRoot(NPath basePath);

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
    NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options);

    NPath subpath(NPath basePath, int beginIndex, int endIndex);

    List<String> getLocationItems(NPath basePath);

    void moveTo(NPath basePath, NPath other, NPathOption... options);

    void copyTo(NPath basePath, NPath other, NPathOption... options);

    void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options);

    NPath toRelativePath(NPath basePath, NPath parentPath);

    byte[] getDigest(NPath basePath, String algo);
}
