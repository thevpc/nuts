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
package net.vpc.app.nuts;

import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
 * @category Format
 */
public interface NutsVersionFormat extends NutsFormat {

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsVersionFormat setSession(NutsSession session);

    NutsVersionFormat addProperty(String key, String value);

    NutsVersionFormat addProperties(Map<String,String> p);

    /**
     * return version set by {@link #setVersion(net.vpc.app.nuts.NutsVersion) }
     *
     * @return version set by {@link #setVersion(net.vpc.app.nuts.NutsVersion) }
     */
    NutsVersion getVersion();

    /**
     * set version to print. if null, workspace version will be considered.
     *
     * @param version version to print
     * @return {@code this} instance
     */
    NutsVersionFormat setVersion(NutsVersion version);

    /**
     * return true if version is null (default). In such case, workspace version
     * is considered.
     *
     * @return true if version is null (default)
     */
    boolean isWorkspaceVersion();

}
