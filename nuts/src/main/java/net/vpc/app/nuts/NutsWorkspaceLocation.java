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

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 * @since 0.5.4
 */
public class NutsWorkspaceLocation implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private String location;
    private boolean enabled = true;

    public NutsWorkspaceLocation() {
    }

    public NutsWorkspaceLocation(NutsWorkspaceLocation other) {
        this.name = other.uuid;
        this.name = other.getName();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
    }

    public NutsWorkspaceLocation(String uuid, String name, String location) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsWorkspaceLocation setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceLocation setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsWorkspaceLocation setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceLocation setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NutsWorkspaceLocation that = (NutsWorkspaceLocation) o;

        return location != null ? location.equals(that.location) : that.location == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceLocation{"
                + "uuid='" + uuid + '\''
                + ", name='" + name + '\''
                + ", location='" + location + '\''
                + ", enabled=" + enabled
                + '}';
    }

    public NutsWorkspaceLocation copy() {
        return new NutsWorkspaceLocation(this);
    }
}
