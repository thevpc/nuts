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

import java.nio.file.Path;
import java.util.Objects;


/**
 * Default Content implementation.
 * @author vpc
 * @since 0.5.4
 * @category Descriptor
 */
public class NutsDefaultContent implements NutsContent {

    private final Path file;
    private final boolean cached;
    private final boolean temporary;

    /**
     * Default Content implementation constructor
     * @param file content file path
     * @param cached true if the file is cached (may be not up to date)
     * @param temporary true if file is temporary (should be deleted later)
     */
    public NutsDefaultContent(Path file, boolean cached, boolean temporary) {
        this.file = file;
        this.cached = cached;
        this.temporary = temporary;
    }

    /**
     * content path location
     * @return content path location
     */
    @Override
    public Path getPath() {
        return file;
    }

    /**
     * true if the file is cached (may be not up to date)
     * @return true if the file is cached (may be not up to date)
     */
    @Override
    public boolean isCached() {
        return cached;
    }

    /**
     * true if file is temporary (should be deleted later)
     * @return true if file is temporary (should be deleted later)
     */
    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public String toString() {
        return "Content{" + "file=" + file + ", cached=" + cached + ", temporary=" + temporary + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsDefaultContent that = (NutsDefaultContent) o;
        return cached == that.cached &&
                temporary == that.temporary &&
                Objects.equals(file, that.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(file, cached, temporary);
    }
}
