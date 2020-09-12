/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
 * I/O Manager supports a set of operations to manipulate terminals and files in a
 * handy manner that is monitorable and Workspace aware.
 *
 * @author vpc
 * @since 0.5.4
 * @category Input Output
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

    /**
     * return new terminal bound to system terminal
     *
     * @return new terminal
     */
    NutsSessionTerminal createTerminal();

    /**
     * return new terminal bound to the given {@code parent}
     *
     * @param parent parent terminal or null
     * @return new terminal
     */
    NutsSessionTerminal createTerminal(NutsTerminalBase parent);

    /**
     * create temp file in the workspace's temp folder
     *
     * @param name file name
     * @return new file path
     */
    Path createTempFile(String name);

    /**
     * create temp file in the repository's temp folder
     *
     * @param name       file name
     * @param repository repository
     * @return new file path
     */
    Path createTempFile(String name, NutsRepository repository);

    /**
     * create temp folder in the workspace's temp folder
     *
     * @param name folder name
     * @return new folder path
     */
    Path createTempFolder(String name);

    /**
     * create temp folder in the repository's temp folder
     *
     * @param name       folder name
     * @param repository repository
     * @return new folder path
     */
    Path createTempFolder(String name, NutsRepository repository);


    /**
     * create a new instance of {@link NutsApplicationContext}
     *
     * @param args            application arguments
     * @param appClass        application class
     * @param storeId         application store id or null
     * @param startTimeMillis application start time
     * @return new instance of {@link NutsApplicationContext}
     */
    NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis);

    /**
     * return terminal format that handles metrics and format/escape methods
     *
     * @return terminal format that handles metrics and format/escape methods
     */
    NutsTerminalFormat terminalFormat();

    /**
     * return terminal format that handles metrics and format/escape methods.
     *
     * @return terminal format that handles metrics and format/escape methods
     */
    NutsTerminalFormat getTerminalFormat();

    /**
     * return terminal format that handles metrics and format/escape methods.
     *
     * @return terminal format that handles metrics and format/escape methods
     */
    NutsSystemTerminal systemTerminal();

    /**
     * return workspace system terminal.
     *
     * @return workspace system terminal
     */
    NutsSystemTerminal getSystemTerminal();

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NutsSessionTerminal terminal();

    /**
     * return workspace default terminal
     *
     * @return workspace default terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * update workspace wide system terminal
     *
     * @param terminal system terminal
     * @return {@code this} instance
     */
    NutsIOManager setSystemTerminal(NutsSystemTerminalBase terminal);

    /**
     * update workspace wide terminal
     *
     * @param terminal terminal
     * @return {@code this} instance
     */
    NutsIOManager setTerminal(NutsSessionTerminal terminal);

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(File file);

    /**
     * parse Execution Entries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(Path file);

    /**
     * parse Execution Entries
     *
     * @param inputStream stream
     * @param type        stream type
     * @param sourceName  stream source name (optional)
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type, String sourceName);

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
     * create new {@link NutsIOLockAction} instance
     *
     * @return create new {@link NutsIOLockAction} instance
     */
    NutsIOLockAction lock();

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

    /**
     * return non null executor service
     *
     * @return non null executor service
     */
    ExecutorService executorService();
}
