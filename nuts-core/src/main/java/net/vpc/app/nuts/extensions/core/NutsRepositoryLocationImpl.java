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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsRepositoryLocation;

public class NutsRepositoryLocationImpl implements NutsRepositoryLocation {

    @JsonTransient
    private static final long serialVersionUID = 1;

    private String id;
    private String type;
    private String location;
    private boolean enabled = true;
    private long instanceSerialVersionUID = serialVersionUID;

    public NutsRepositoryLocationImpl() {
    }

    public NutsRepositoryLocationImpl(String id, String location, String type) {
        this.id = id;
        this.type = type;
        this.location = location;
    }

    @Override
    public long getInstanceSerialVersionUID() {
        return instanceSerialVersionUID;
    }

    @Override
    public void setInstanceSerialVersionUID(long instanceSerialVersionUID) {
        this.instanceSerialVersionUID = instanceSerialVersionUID;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public NutsRepositoryLocation setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String getLocation() {
        return location;
    }

    @Override
    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NutsRepositoryLocationImpl that = (NutsRepositoryLocationImpl) o;

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
        return "NutsRepositoryLocation{" + "id=" + id + ", type=" + type + ", location=" + location + ", enabled=" + enabled + ", instanceSerialVersionUID=" + instanceSerialVersionUID + '}';
    }
    
}
