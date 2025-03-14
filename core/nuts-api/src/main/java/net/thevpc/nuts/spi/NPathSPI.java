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
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.*;

import java.io.IOException;
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

    long getContentLength(NPath basePath);

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


    NPath toRelativePath(NPath basePath, NPath parentPath);


    /// ////////////////////////////////////////////////
    /// DEFAULT IMPLEMENTATIONS

    default List<String> getNames(NPath basePath) {
        String location = getLocation(basePath);
        return NStringUtils.split(location, "/", true, true);
    }

    default boolean isLocal(NPath basePath) {
        return true;
    }


    default String getLocation(NPath basePath) {
        String str = toString();
        int u = str.indexOf(':');
        if (u > 0) {
            String p = str.substring(0, u);
            if (p.matches("[a-zA-Z][a-zA-Z-_0-9]*")) {
                String a = str.substring(u + 1);
                if (a.startsWith("//")) {
                    return a.substring(1);
                }
                return a;
            }
        }
        return str;
    }

    default String getProtocol(NPath basePath) {
        String str = toString();
        int u = str.indexOf(':');
        if (u > 0) {
            String p = str.substring(0, u);
            if (p.matches("[a-zA-Z][a-zA-Z-_0-9]*")) {
                return p;
            }
        }
        return null;
    }

    default NFormatSPI formatter(NPath basePath) {
        return new NFormatSPI() {
            @Override
            public String getName() {
                return "path";
            }

            @Override
            public void print(NPrintStream out) {
                out.print(basePath.toString());
            }

            @Override
            public boolean configureFirst(NCmdLine cmdLine) {
                return false;
            }
        };
    }

    default NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        if (rootPath == null) {
            return basePath.normalize();
        }
        return rootPath.toAbsolute().resolve(basePath);
    }

    default boolean isAbsolute(NPath basePath) {
        return true;
    }

    default String getName(NPath basePath) {
        List<String> n = getNames(basePath);
        return n.isEmpty() ? "" : n.get(n.size() - 1);
    }


    default NPath resolve(NPath basePath, String path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        return resolve(basePath, NPath.of(path));
    }

    default NPath resolve(NPath basePath, NPath path) {
        if (NBlankable.isBlank(path)) {
            return basePath;
        }
        if (path.isAbsolute()) {
            return path;
        }
        NPath root = basePath;
        for (String item : path.getNames()) {
            root = root.resolve(item);
        }
        return root;
    }

    default NPath resolveSibling(NPath basePath, String path) {
        NPath parent = basePath.getParent();
        return parent.resolve(path);
    }

    default NPath resolveSibling(NPath basePath, NPath path) {
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
        if (isRoot(basePath)) {
            return basePath;
        }
        List<String> names = getNames(basePath);
        NPath root = getRoot(basePath);
        List<String> newNames = new ArrayList<>();
        for (String item : names) {
            switch (item) {
                case ".": {
                    break;
                }
                case "..": {
                    if (newNames.size() > 0) {
                        newNames.remove(newNames.size() - 1);
                    }
                    break;
                }
                default: {
                    newNames.add(item);
                }
            }
        }
        if (newNames.size() != names.size()) {
            for (String item : newNames) {
                root = root.resolve(item);
            }
            return root;
        }
        return basePath;
    }

    default NPath getParent(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        List<String> names = getNames(basePath);
        List<String> items = names.subList(0, names.size() - 1);
        NPath root = getRoot(basePath);
        for (String item : items) {
            root = root.resolve(item);
        }
        return root;
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
    default boolean isRoot(NPath basePath) {
        return getNames(basePath).isEmpty();
    }

    /**
     * return true if this path is a simple name that do not contain '/' or
     * equivalent
     *
     * @param basePath basePath
     * @return true if this path is a simple name that do not contain '/' or
     * equivalent
     */
    default boolean isName(NPath basePath) {
        if (getNameCount(basePath) > 1) {
            return false;
        }
        String v = toString();
        switch (v) {
            case "/":
            case "\\":
            case ".":
            case "..": {
                return false;
            }
        }
        for (char c : v.toCharArray()) {
            switch (c) {
                case '/':
                case '\\': {
                    return false;
                }
            }
        }
        return true;
    }

    default NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        List<String> items = getNames(basePath).subList(beginIndex, endIndex);
        NPath root = getRoot(basePath);
        for (String item : items) {
            root = root.resolve(item);
        }
        return root;
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
        return NStream.of(new Iterator<NPath>() {
            Stack<NPath> stack = new Stack<>();
            NPath curr = null;

            {
                stack.push(basePath);
            }

            @Override
            public boolean hasNext() {
                if (!stack.isEmpty()) {
                    NPath currentPath = stack.pop();
                    if (currentPath.isDirectory()) {
                        if (maxDepth > 0) {
                            for (NPath nPath : currentPath.list()) {
                                stack.push(nPath);
                            }
                        }
                    }
                    curr = currentPath;
                    return true;
                } else {
                    curr = null;
                    return false;
                }
            }

            @Override
            public NPath next() {
                return curr;
            }
        });
    }

    /**
     * @param basePath base path
     * @return
     */
    default int getNameCount(NPath basePath) {
        return getNames(basePath).size();
    }

    default NPath toCompressedForm(NPath basePath) {
        return null;
    }


    default void moveTo(NPath basePath, NPath other, NPathOption... options) {
        copyTo(basePath, other, options);
        delete(basePath, true);
    }

    default void copyTo(NPath basePath, NPath other, NPathOption... options) {
        try (InputStream in = basePath.getInputStream(options)) {
            try (OutputStream out = other.getOutputStream(options)) {
                byte[] buffer = new byte[400 * 1024];
                int len;
                long count = 0;
                try {
                    while ((len = in.read(buffer)) > 0) {
                        count += len;
                        out.write(buffer, 0, len);
                    }
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
            }
        } catch (Exception e) {
            throw new NIOException(e);
        }
    }

    default void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        Stack<NPath> stack = new Stack<>();
        Stack<Boolean> visitedStack = new Stack<>();

        stack.push(basePath);
        visitedStack.push(false);

        while (!stack.isEmpty()) {
            NPath currentPath = stack.pop();
            boolean visited = visitedStack.pop();

            if (visited) {
                visitor.postVisitDirectory(currentPath, null);
                continue;
            }

            if (currentPath.isDirectory()) {
                visitor.preVisitDirectory(currentPath);
                stack.push(currentPath);
                visitedStack.push(true);
                try {
                    if (maxDepth > 0) {
                        for (NPath nPath : currentPath.list()) {
                            stack.push(nPath);
                            visitedStack.push(false); // Mark children for pre-visit first
                        }
                    }
                } catch (RuntimeException e) {
                    visitor.postVisitDirectory(currentPath, e);
                }
            } else {
                visitor.visitFile(currentPath);
            }
        }
    }

    default int compareTo(NPath basePath, NPath other) {
        return toString().compareTo(basePath.toString());
    }

    default byte[] getDigest(NPath basePath, String algo) {
        return null;
    }
}
