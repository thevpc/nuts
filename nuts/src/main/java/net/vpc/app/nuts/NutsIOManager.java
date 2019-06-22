/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.*;
import java.nio.file.Path;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsIOManager extends NutsComponent<Object> {

    NutsMonitorCommand monitor();

    String expandPath(Path path);

    String expandPath(String path);

    String expandPath(String path, String baseFolder);

    String loadHelpString(String resource, Class cls, String defaultValue);

    NutsHashCommand hash();

    InputStream nullInputStream();

    PrintStream nullPrintStream();

    PrintStream createPrintStream(Path out);

    PrintStream createPrintStream(File out);

    PrintStream createPrintStream(OutputStream out, NutsTerminalMode mode);

    NutsSessionTerminal createTerminal();

    NutsSessionTerminal createTerminal(NutsTerminalBase parent);

    Path createTempFile(String name);

    Path createTempFolder(String name);

    Path createTempFile(String name, NutsRepository repository);

    Path createTempFolder(String name, NutsRepository repository);

    NutsPathCopyAction copy();

    Path path(String first, String... more);

    NutsApplicationContext createApplicationContext(String[] args, Class appClass, String storeId, long startTimeMillis);

    NutsTerminalFormat terminalFormat();

    NutsTerminalFormat getTerminalFormat();

    NutsSystemTerminal systemTerminal();

    NutsSystemTerminal getSystemTerminal();

    NutsSessionTerminal terminal();

    NutsSessionTerminal getTerminal();

    NutsIOManager setSystemTerminal(NutsSystemTerminalBase term);

    NutsIOManager setTerminal(NutsSessionTerminal terminal);

    /**
     * parseExecutionEntries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(File file);

    NutsExecutionEntry[] parseExecutionEntries(Path file);

    NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type, String sourceName);
}
