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
 * <br>
 *
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

import net.thevpc.nuts.spi.NutsComponent;

import java.io.*;

/**
 * I/O Manager supports a set of operations to manipulate terminals and files in a
 * handy manner that is monitorable and Workspace aware.
 *
 * @author thevpc
 * %category Input Output
 * @since 0.5.4
 */
public interface NutsIOManager extends NutsComponent<Object/* any object or null */> {

    /**
     * expand path to Workspace Location
     *
     * @param path path to expand
     * @return expanded path
     */
    String expandPath(String path);

    /**
     * expand path to {@code baseFolder}.
     * Expansion mechanism supports '~' prefix (linux like) and will expand path to {@code baseFolder}
     * if it was resolved as a relative path.
     *
     * @param path       path to expand
     * @param baseFolder base folder to expand relative paths to
     * @return expanded path
     */
    String expandPath(String path, String baseFolder);

    /**
     * create a null input stream instance
     *
     * @return null input stream instance
     */
    InputStream nullInputStream();

    /**
     * create a null print stream instance
     *
     * @return null print stream instance
     */
    PrintStream nullPrintStream();

    /**
     * create print stream that supports the given {@code mode}.
     * If the given {@code out} is a PrintStream that supports {@code mode}, it should be
     * returned without modification.
     *
     * @param out  stream to wrap
     * @param mode mode to support
     * @param session
     * @return {@code mode} supporting PrintStream
     */
    PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode, NutsSession session);

    NutsTempManager tmp();


    NutsTerminalManager term();

    /**
     * create new {@link NutsIOCopyAction} instance
     *
     * @return create new {@link NutsIOCopyAction} instance
     */
    NutsIOCopyAction copy();

    /**
     * create new {@link NutsIOProcessAction} instance
     *
     * @return create new {@link NutsIOProcessAction} instance
     */
    NutsIOProcessAction ps();

    /**
     * create new {@link NutsIOCompressAction} instance
     *
     * @return create new {@link NutsIOCompressAction} instance
     */
    NutsIOCompressAction compress();

    /**
     * create new {@link NutsIOUncompressAction} instance
     *
     * @return create new {@link NutsIOUncompressAction} instance
     */
    NutsIOUncompressAction uncompress();

    /**
     * create new {@link NutsIODeleteAction} instance
     *
     * @return create new {@link NutsIODeleteAction} instance
     */
    NutsIODeleteAction delete();

    /**
     * create new {@link NutsMonitorAction} instance that helps
     * monitoring streams.
     *
     * @return create new {@link NutsIOLockAction} instance
     */
    NutsMonitorAction monitor();

    /**
     * create new {@link NutsIOHashAction} instance that helps
     * hashing streams and files.
     *
     * @return create new {@link NutsIOHashAction} instance
     */
    NutsIOHashAction hash();

    NutsInputManager input();

    NutsOutputManager output();
}
