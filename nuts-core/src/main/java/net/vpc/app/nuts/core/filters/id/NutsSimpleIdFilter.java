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
package net.vpc.app.nuts.core.filters.id;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIdFilter;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.app.nuts.core.util.Simplifiable;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsSimpleIdFilter implements NutsIdFilter, Simplifiable<NutsIdFilter>, NutsJsAwareIdFilter {

    private NutsId id;

    public NutsSimpleIdFilter(NutsId id) {
        this.id = id;
        if (id == null) {
            throw new NutsIllegalArgumentException("Missing id " + id);
        }
    }

    public boolean accept(NutsId id) {
        if (!this.id.getSimpleName().equals(id.getSimpleName())) {
            return false;
        }
        if (!this.id.getVersion().toFilter().accept(id.getVersion())) {
            return false;
        }
        return true;
    }

    @Override
    public String toJsNutsIdFilterExpr() {
        return "id.matches('" + CoreStringUtils.escapeCoteStrings(id.toString()) + "')";
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + this.id.hashCode();
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
        final NutsSimpleIdFilter other = (NutsSimpleIdFilter) obj;
        if (!this.id.equals(other.getId())) {
            return false;
        }
        return true;
    }

    public NutsId getId() {
        return id;
    }

    @Override
    public String toString() {
        return "NutsSimpleIdFilter{" + id + "}";
    }

}
