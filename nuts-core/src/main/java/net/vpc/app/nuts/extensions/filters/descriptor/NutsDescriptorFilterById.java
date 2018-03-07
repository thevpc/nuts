/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.filters.descriptor;

import java.util.Objects;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFilter;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.extensions.filters.id.JsNutsIdFilter;
import net.vpc.app.nuts.extensions.util.Simplifiable;

/**
 *
 * @author vpc
 */
public class NutsDescriptorFilterById implements NutsDescriptorFilter, Simplifiable<NutsDescriptorFilter>, JsNutsDescriptorFilter {

    private NutsIdFilter id;

    public NutsDescriptorFilterById(NutsIdFilter id) {
        this.id = id;
    }

    @Override
    public boolean accept(NutsDescriptor descriptor) {
        if (id != null) {
            return id.accept(descriptor.getId());
        }
        return true;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        if (id != null && id instanceof Simplifiable) {
            NutsIdFilter id2 = ((Simplifiable<NutsIdFilter>) id).simplify();
            if (id2 != id) {
                if (id2 == null) {
                    return null;
                }
                return new NutsDescriptorFilterById(id2);
            }
        }
        return this;
    }

    @Override
    public String toJsNutsDescriptorFilterExpr() {
        if (id == null) {
            return "true";
        }
        if (id instanceof JsNutsIdFilter) {
            return ((JsNutsIdFilter) id).toJsNutsIdFilterExpr();
        }
        return null;
    }

    @Override
    public String toString() {
        return "Id{" + id + '}';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final NutsDescriptorFilterById other = (NutsDescriptorFilterById) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
