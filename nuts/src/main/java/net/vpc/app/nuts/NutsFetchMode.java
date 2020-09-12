/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * fetch mode defines if the artifact should be looked for withing the "installed" meta repository, "local" (offline)
 * machine repositories or over the wire (remote repositories).
 *
 * <p>
 * "installed" artifacts are stored in a pseudo-repository called "installed" which include all installed
 * (using command install) artifacts. Effective storage may (should?) remain in a local repository though.
 * Actually pseudo-repository "installed" manages references to these storages.
 * </p>
 * <p>
 * local repositories include all local folder based repositories. Semantically they should define machine/node based
 * storage that is independent from LAN/WAN/Cloud networks. A local database based repository may be considered as local
 * though not recommended as the server may be down.
 * Il all ways, local repositories are considered fast according to fetch/deploy commands.
 * </p>
 * <p>
 * remote repositories include all non local repositories which may present slow access and connectivity issues.
 * Typically this include server based repositories (http, ...).
 * </p>
 * <p>
 * It is important to say that a repository may serve both local and remote artifacts as usually remote repositories
 * enable cache support; in which case, if the artifact si cached, it will be accessed locally.
 * </p>
 * @since 0.5.4
 * @category Commands
 */
public enum NutsFetchMode {
    /**
     * artifacts fetched (locally)
     */
    LOCAL,

    /**
     * artifacts not fetched (remote)
     */
    REMOTE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NutsFetchMode() {
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
