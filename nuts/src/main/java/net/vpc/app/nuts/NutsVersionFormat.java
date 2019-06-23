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
package net.vpc.app.nuts;

import java.util.Properties;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsVersionFormat extends NutsFormat {

    @Override
    NutsVersionFormat session(NutsSession session);

    @Override
    NutsVersionFormat setSession(NutsSession session);

    NutsVersionFormat addProperty(String key, String value);

    NutsVersionFormat addProperties(Properties p);

    /**
     * return version set by {@link #setVersion(net.vpc.app.nuts.NutsVersion) }
     * @return version set by {@link #setVersion(net.vpc.app.nuts.NutsVersion) }
     */
    NutsVersion getVersion();

    /**
     * set version to print. if null, workspace version will be considered.
     * @param version version to print
     * @return {@code this} instance
     */
    NutsVersionFormat setVersion(NutsVersion version);

    /**
     * return version interval set by {@link #setVersionInterval(net.vpc.app.nuts.NutsVersionInterval)}
     * @return version interval set by {@link #setVersionInterval(net.vpc.app.nuts.NutsVersionInterval)}
     */
    NutsVersionInterval getVersionInterval();

    /**
     * set version interval to print. if null, workspace version will be considered.
     * version will be reset to null.
     * @param versionInterval versionInterval to print
     * @return {@code this} instance
     */
    NutsVersionFormat setVersionInterval(NutsVersionInterval versionInterval);

    /**
     * print methods will display workspace version instead of user defined one.
     * version and versionInterval will be reset to null.
     * @return {@code this} instance
     */
    NutsVersionFormat setWorkspaceVersion();

    /**
     * return true if both version and version interval are null (default)
     * @return true if both version and version interval are null (default) 
     */
    boolean isWorkspaceVersion();

    /**
     * return version instance representing the {@code version} string
     * @param version string (may be null)
     * @return version instance representing the {@code version} string 
     */
    NutsVersion parse(String version);

    NutsVersionFilter parseFilter(String versionFilter);

}
