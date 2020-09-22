/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Class describing executable command.
 * @author vpc
 * @since 0.5.4
 * @category Descriptor
 */
public interface NutsExecutableInformation {

    /**
     * return executable type
     * @return executable type
     */
    NutsExecutableType getType();

    /**
     * executable artifact id
     * @return executable artifact id
     */
    NutsId getId();

    /**
     * executable name
     * @return executable name
     */
    String getName();

    /**
     * versatile executable name
     * @return versatile executable name
     */
    String getValue();

    /**
     * executable description
     * @return executable description
     */
    String getDescription();

    /**
     * executable help string
     * @return executable help string
     */
    String getHelpText();


}
