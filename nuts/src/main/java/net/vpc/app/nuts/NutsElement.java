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

/**
 * Nuts Element types are generic JSON like parsable objects.
 * @author vpc
 * @since 0.5.6
 */
public interface NutsElement {

    /**
     * element type
     * @return element type
     */
    NutsElementType type();

    /**
     * convert this element to {@link NutsPrimitiveElement} or throw ClassCastException
     * @return {@link NutsPrimitiveElement}
     */
    NutsPrimitiveElement primitive();

    /**
     * convert this element to {@link NutsObjectElement} or throw ClassCastException
     * @return {@link NutsObjectElement}
     */
    NutsObjectElement object();

    /**
     * convert this element to {@link NutsArrayElement} or throw ClassCastException
     * @return {@link NutsArrayElement}
     */
    NutsArrayElement array();

}
