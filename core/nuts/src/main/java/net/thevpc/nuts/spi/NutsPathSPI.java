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

    NutsStream<NutsPath> list();

    NutsFormatSPI getFormatterSPI();

    String getName();

    String getProtocol();

    NutsPath resolve(String path);

    NutsPath resolve(NutsPath path);

    NutsPath resolveSibling(String path);

    NutsPath resolveSibling(NutsPath path);

    NutsPath toCompressedForm();

    URL toURL();

    Path toFile();

    boolean isSymbolicLink();

    boolean isOther();

    boolean isDirectory();

    boolean isRegularFile();

    boolean exists();

    long getContentLength();

    String getContentEncoding();

    String getContentType();

    String toString();

    String getLocation();

    InputStream getInputStream();

    OutputStream getOutputStream();

    NutsSession getSession();

    void delete(boolean recurse);

    void mkdir(boolean parents);

    Instant getLastModifiedInstant();

    Instant getLastAccessInstant();

    Instant getCreationInstant();

    NutsPath getParent();

    NutsPath toAbsolute(NutsPath basePath);

    NutsPath normalize();

    boolean isAbsolute();

    String owner();

    String group();

    Set<NutsPathPermission> permissions();

    void setPermissions(NutsPathPermission... permissions);

    void addPermissions(NutsPathPermission... permissions);

    void removePermissions(NutsPathPermission... permissions);

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
    int getPathCount();

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
    NutsStream<NutsPath> walk(int maxDepth, NutsPathVisitOption[] options);

    NutsPath subpath(int beginIndex, int endIndex);

    String[] getItems();
}
