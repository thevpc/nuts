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

import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * 
 * @author vpc
 * @since 0.5.4
 */
public interface NutsParseManager {

    NutsId parseId(String id);

    NutsDescriptor parseDescriptor(URL url);

    NutsDescriptor parseDescriptor(byte[] bytes);

    NutsDescriptor parseDescriptor(Path file);

    NutsDescriptor parseDescriptor(File file);

    NutsDescriptor parseDescriptor(InputStream stream);

    NutsDescriptor parseDescriptor(InputStream stream, boolean close);

    NutsDescriptor parseDescriptor(String descriptorString);

    NutsDependency parseDependency(String dependency);

    NutsVersion parseVersion(String version);

    NutsVersionFilter parseVersionFilter(String versionFilter);

    NutsId parseRequiredId(String nutFormat);

    /**
     * parseExecutionEntries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] parseExecutionEntries(File file);

    NutsExecutionEntry[] parseExecutionEntries(Path file);

    NutsExecutionEntry[] parseExecutionEntries(InputStream inputStream, String type, String sourceName);

    /**
     * this method removes all {@link NutsFormattedPrintStream}'s special
     * formatting sequences and returns the raw string to be printed on an
     * ordinary {@link PrintStream}
     *
     * @param value input string
     * @return string without any escape sequences so that the text printed
     * correctly on any non formatted {@link PrintStream}
     */
    String filterText(String value);

    /**
     * This method escapes all special characters that are interpreted by
     * {@link NutsFormattedPrintStream} so that this exact string is printed on
     * such print streams When str is null, an empty string is return
     *
     * @param value input string
     * @return string with escaped characters so that the text printed correctly
     * on {@link NutsFormattedPrintStream}
     */
    String escapeText(String value);
}
