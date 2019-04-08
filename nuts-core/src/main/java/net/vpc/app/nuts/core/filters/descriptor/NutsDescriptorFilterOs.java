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
package net.vpc.app.nuts.core.filters.descriptor;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.Simplifiable;

import java.util.Objects;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsDescriptorFilterOs implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter>, JsNutsDescriptorFilter {

    private final String os;

    public NutsDescriptorFilterOs(String packaging) {
        this.os = packaging;
    }

    public String getOs() {
        return os;
    }

    @Override
    public boolean accept(NutsDescriptor descriptor) {
        return descriptor.matchesOs(os);
    }

    /**
     * @return null if nothing to check after
     */
    @Override
    public NutsDescriptorFilter simplify() {
        if (CoreStringUtils.isBlank(os)) {
            return null;
        }
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        return "descriptor.matchesOs('" + CoreStringUtils.escapeCoteStrings(os) + "')";
    }

    @Override
    public String toString() {
        return "Os{" + os + '}';
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + Objects.hashCode(this.os);
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
        final NutsDescriptorFilterOs other = (NutsDescriptorFilterOs) obj;
        if (!Objects.equals(this.os, other.os)) {
            return false;
        }
        return true;
    }

}
