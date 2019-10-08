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

/**
 * Artifacts are organized according to {@code NutsIdType} to reflect how the artifact
 * should be managed by the workspace.
 * This information is available in {@link NutsDefinition}
 */
public enum NutsIdType {
    /**
     * the id denotes a nuts api id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_API}
     */
    API,

    /**
     * the id denotes a nuts runtime id artifact.
     * an artifact is a valid nuts api if it corresponds to {@link NutsConstants.Ids#NUTS_RUNTIME}
     */
    RUNTIME,

    /**
     * the id denotes a nuts known extension artifact.
     * an artifact is a valid nuts extension if it contains a property "nuts-extension" equal to
     * true in its descriptor.
     */
    EXTENSION,

    /**
     * the id denotes a nuts companion id artifact.
     * Default companions are
     * <ul>
     *     <li>net.vpc.app.nuts.toolbox:nsh</li>
     *     <li>net.vpc.app.nuts.toolbox:nadmin</li>
     *     <li>net.vpc.app.nuts.toolbox:ndi</li>
     * </ul>
     *
     */
    COMPANION,

    /**
     * the id is none of the previous types
     */
    REGULAR
}
