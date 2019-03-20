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

public class NutsRepositoryRef implements Serializable{

    private static final long serialVersionUID = 1;

    private String name;
    private String location;
    private boolean enabled = true;
    private boolean failSafe = false;

    public NutsRepositoryRef() {
    }

    public NutsRepositoryRef(NutsRepositoryRef other) {
        this.name = other.getName();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
        this.failSafe = other.isEnabled();
    }

    public NutsRepositoryRef(String name,String location, boolean enabled) {
        this.name = name;
        this.location = location;
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsRepositoryRef setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryRef setLocation(String location) {
        this.location = location;
        return this;
    }


    public String getName() {
        return name;
    }

    public NutsRepositoryRef setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public NutsRepositoryRef setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
        return this;
    }

    public NutsRepositoryRef copy() {
        return new NutsRepositoryRef(this);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode(this.name);
        hash = 79 * hash + Objects.hashCode(this.location);
        hash = 79 * hash + (this.enabled ? 1 : 0);
        hash = 79 * hash + (this.failSafe ? 1 : 0);
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
        final NutsRepositoryRef other = (NutsRepositoryRef) obj;
        if (this.enabled != other.enabled) {
            return false;
        }
        if (this.failSafe != other.failSafe) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsRepositoryRef{" + "name=" + name + ", location=" + location + ", enabled=" + enabled + ", failSafe=" + failSafe + '}';
    }

   
    
}
