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
 * NutsBootWorkspace is responsible of loading initial nuts-core.jar and its
 * dependencies and for creating workspaces using the method
 * {@link #openWorkspace(String, NutsWorkspaceCreateOptions)}. NutsBootWorkspace
 * is also responsible of managing local jar cache folder located at
 * $root/bootstrap where $root is the nuts root folder (~/.nuts) defined by
 * {@link #getNutsHomeLocation()}.
 * <pre>
 *   ~/.nuts/bootstrap ({@link #getBootstrapLocation})
 *       └── net
 *           └── vpc
 *               └── app
 *                   └── nuts
 *                       ├── nuts
 *                       │   └── 0.3.8
 *                       │   │   └── nuts.properties
 *                       │   └── LATEST
 *                       │       └── nuts.properties
 *                       └── nuts-core
 *                           └── 0.3.8.0
 *                               └── nuts-core.properties
 * </pre> Created by vpc on 1/6/17.
 */
public interface NutsBootWorkspace {

    /**
     * loaded (at runtime) nuts id for the net.vpc.app:nuts component. The
     * nuts id includes the version.
     *
     * @return a string representing the boot nuts id in the form
     * net.vpc.app:nuts#VERSION
     */
    String getBootId();

    /**
     * loaded (at runtime) nuts id for the net.vpc.app.nuts:nuts-core component.
     * The nuts id includes the version. It may return another implementation
     * component other then net.vpc.app.nuts:nuts-core
     *
     * @return a string representing the boot nuts id in the form
     * net.vpc.app.nuts-core:nuts#VERSION
     */
    String getRuntimeId();

    NutsBootOptions getBootOptions();

    /**
     * nuts root folder. It defaults to "~/.nuts"
     *
     * @return nuts root folder
     */
    String getNutsHomeLocation();

    /**
     * nuts bootstrap folder. It defaults to "~/.nuts/bootstrap" bootstrap
     * folder contains jars and configuration files about nuts dependencies.
     *
     * @return nuts bootstrap folder
     */
    String getBootstrapLocation();

    /**
     * opens (and create if necessary) a new workspace at
     * <code>workspaceLocation</code> location and according to the given
     * creation options. workspaceLocation may be absolute or relative in which
     * case it will be resolved as a sub folder of nuts root folder (see
     * {@link #getNutsHomeLocation()}). If no options are provided (options==null)
     * workspace will be created and saved it not found null     (<code>new NutsWorkspaceCreateOptions().setCreateIfNotFound(true).setSaveIfCreated(true)<code>)
     * Please note that it may not be safe to create several instances of the same workspace.
     *
     * @param options creation options
     * @return a valid and initialized Workspace implementation
     */
    NutsWorkspace openWorkspace(NutsWorkspaceCreateOptions options);
}
