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
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsParseManager {

    NutsId requiredId(String nutFormat);

    NutsId id(String id);

    NutsCommandLine commandLine(String line);

    NutsCommandLine command(String... arguments);

    NutsCommandLine command(Collection<String> arguments);

    NutsDescriptor descriptor(URL url);

    NutsDescriptor descriptor(byte[] bytes);

    NutsDescriptor descriptor(Path file);

    NutsDescriptor descriptor(File file);

    NutsDescriptor descriptor(InputStream stream);

    NutsDescriptor descriptor(InputStream stream, boolean close);

    NutsDescriptor descriptor(String descriptorString);

    NutsDependency dependency(String dependency);

    NutsVersion version(String version);

    NutsVersionFilter versionFilter(String versionFilter);

    /**
     * executionEntries
     *
     * @param file jar file
     * @return execution entries (class names with main method)
     */
    NutsExecutionEntry[] executionEntries(File file);

    NutsExecutionEntry[] executionEntries(Path file);

    NutsExecutionEntry[] executionEntries(InputStream inputStream, String type, String sourceName);

}
