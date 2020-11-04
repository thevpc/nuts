/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

/**
 * Supported dependency scope lists
 * @author vpc
 * @since 0.5.4
 * @category Descriptor
 */
public enum NutsDependencyScope {
    /**
     * equivalent to maven's compile and to gradle's api
     */
    API,
    /**
     * equivalent to gradle's implementation
     */
    IMPLEMENTATION,
    /**
     * equivalent to maven's provided
     */
    PROVIDED,
    /**
     * equivalent to maven's import
     */
    IMPORT,
    /**
     * equivalent to maven's runtime
     */
    RUNTIME,
    /**
     * equivalent to maven's system
     */
    SYSTEM,
    /**
     * equivalent to maven's test
     */
    TEST_COMPILE,
    /**
     * dependencies needed for test but are provided by container.
     */
    TEST_PROVIDED,
    /**
     * dependencies needed for test execution
     */
    TEST_RUNTIME,
    /**
     * other
     */
    OTHER;

    /**
     * lower-cased identifier for the enum entry
     */
    private String id;

    /**
     * default constructor
     */
    NutsDependencyScope() {
        this.id = name().toLowerCase().replace('_', '-');
    }

    /**
     * lower cased identifier.
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


}
