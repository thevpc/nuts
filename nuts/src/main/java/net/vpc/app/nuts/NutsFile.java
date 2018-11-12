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

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by vpc on 1/6/17.
 */
public class NutsFile implements Serializable,Comparable{

    private NutsDescriptor descriptor;
    private String file;
    private NutsId id;
    private boolean cached;
    private boolean temporary;
    private boolean installed;
    private String installFolder;

    public NutsFile(NutsId id, NutsDescriptor descriptor, String file, boolean cached, boolean temporary, String installFolder) {
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

    public String getInstallFolder() {
        return installFolder;
    }

    public NutsFile setInstallFolder(String installFolder) {
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

    public String getFile() {
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.descriptor);
        hash = 67 * hash + Objects.hashCode(this.file);
        hash = 67 * hash + Objects.hashCode(this.id);
        hash = 67 * hash + (this.cached ? 1 : 0);
        hash = 67 * hash + (this.temporary ? 1 : 0);
        hash = 67 * hash + (this.installed ? 1 : 0);
        hash = 67 * hash + Objects.hashCode(this.installFolder);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsFile other = (NutsFile) obj;
        if (this.cached != other.cached) {
            return false;
        }
        if (this.temporary != other.temporary) {
            return false;
        }
        if (this.installed != other.installed) {
            return false;
        }
        if (!Objects.equals(this.descriptor, other.descriptor)) {
            return false;
        }
        if (!Objects.equals(this.file, other.file)) {
            return false;
        }
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        if (!Objects.equals(this.installFolder, other.installFolder)) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Object n2) {
        if (n2 == null) {
            return 1;
        }
        if (!(n2 instanceof NutsFile)) {
            return -1;
        }
        NutsId o1=getId();
        NutsId o2=((NutsFile)n2).getId();
        if (o1 == null || o2 == null) {
            if (o1 == o2) {
                return 0;
            }
            if (o1 == null) {
                return -1;
            }
            return 1;
        }
        return o1.toString().compareTo(o2.toString());
    }
}
