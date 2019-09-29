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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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

import java.util.Objects;

/**
 * Default and dummy NutsSupportLevelContext implementation
 * @author vpc
 * @param <T> support level type
 */
public class NutsDefaultSupportLevelContext<T> implements NutsSupportLevelContext<T> {

    private final NutsWorkspace ws;
    private final T constraints;

    /**
     * default constructor
     * @param ws workspace
     * @param constraints constraints
     */
    public NutsDefaultSupportLevelContext(NutsWorkspace ws, T constraints) {
        this.ws = ws;
        this.constraints = constraints;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public T getConstraints() {
        return constraints;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsDefaultSupportLevelContext<?> that = (NutsDefaultSupportLevelContext<?>) o;
        return Objects.equals(ws, that.ws) &&
                Objects.equals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ws, constraints);
    }

    @Override
    public String toString() {
        return "NutsDefaultSupportLevelContext{" +
                "constraints=" + constraints +
                '}';
    }
}
