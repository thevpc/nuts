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
 *
 * @author vpc
 * @since 0.5.4
 */
public class NutsRepositoryLocation implements Serializable {

    private static final long serialVersionUID = 1;

    private String name;
    private String type;
    private String location;
    private boolean enabled = true;

    public NutsRepositoryLocation() {
    }

    public NutsRepositoryLocation(NutsRepositoryLocation other) {
        this.name = other.getName();
        this.type = other.getType();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
    }

    public NutsRepositoryLocation(String name, String location, String type) {
        this.name = name;
        this.type = type;
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsRepositoryLocation setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsRepositoryLocation setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public NutsRepositoryLocation setType(String type) {
        this.type = type;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryLocation setLocation(String location) {
        this.location = location;
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

        NutsRepositoryLocation that = (NutsRepositoryLocation) o;

        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        return location != null ? location.equals(that.location) : that.location == null;

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NutsRepositoryLocation{" + "name=" + name + ", type=" + type + ", location=" + location + ", enabled=" + enabled + '}';
    }

    public NutsRepositoryLocation copy() {
        return new NutsRepositoryLocation(this);
    }
}
