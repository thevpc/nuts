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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
import java.util.concurrent.ExecutorService;

/**
 * I/O Manager supports a set of operations to manipulate terminals and files in a
 * handy manner that is monitorable and Workspace aware.
 *
 * @author vpc
 * @category Input Output
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
     * load resource as a formatted string to be used mostly as a help string.
     *
     * @param reader      resource reader
     * @param classLoader class loader
     * @return formatted string (in Nuts Stream Format)
     */
    String loadFormattedString(Reader reader, ClassLoader classLoader);

    /**
     * load resource as a formatted string to be used mostly as a help string.
     *
     * @param resourcePath resource path
     * @param classLoader  class loader
     * @param defaultValue default value if the loading fails
     * @return formatted string (in Nuts Stream Format)
     */
    String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue);

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
     * @return {@code mode} supporting PrintStream
     */
    PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode);

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
