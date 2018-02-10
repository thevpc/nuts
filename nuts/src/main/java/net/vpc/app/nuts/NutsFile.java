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

/**
 * Created by vpc on 1/6/17.
 */
public class NutsFile {

    private NutsDescriptor descriptor;
    private File file;
    private NutsId id;
    private boolean cached;
    private boolean temporary;
    private boolean installed;
    private File installFolder;

    public NutsFile(NutsId id, NutsDescriptor descriptor, File file, boolean cached, boolean temporary, File installFolder) {
        this.descriptor = descriptor;
        this.file = file;
        this.id = id;
        this.cached = cached;
        this.temporary = temporary;
        this.installFolder = installFolder;
    }

    public NutsFile(NutsFile other) {
        if (other != null) {
            this.descriptor = other.descriptor;
            this.file = other.file;
            this.id = other.id;
            this.cached = other.cached;
            this.temporary = other.temporary;
            this.installFolder = other.installFolder;
        }
    }

    public File getInstallFolder() {
        return installFolder;
    }

    public NutsFile setInstallFolder(File installFolder) {
        this.installFolder = installFolder;
        return this;
    }

    public boolean isInstalled() {
        return installed;
    }

    public NutsFile setInstalled(boolean installed) {
        this.installed = installed;
        return this;
    }

    public void setId(NutsId id) {
        this.id = id;
    }

    public NutsId getId() {
        return id;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public NutsDescriptor getDescriptor() {
        return descriptor;
    }

    public File getFile() {
        return file;
    }

    public boolean isCached() {
        return cached;
    }

    @Override
    public String toString() {
        return "NutsFile{"
                + " id=" + id
                + ", file=" + file
                + '}';
    }

    public NutsFile copy() {
        return new NutsFile(this);
    }
}
