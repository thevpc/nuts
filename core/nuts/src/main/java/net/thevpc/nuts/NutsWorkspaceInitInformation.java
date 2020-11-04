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

import java.io.Serializable;
import java.net.URL;
import java.util.Set;

/**
 * workspace initialization options.
 *
 * Created by vpc on 1/23/17.
 *
 * @since 0.5.7
 * @category SPI Base
 */
public interface NutsWorkspaceInitInformation extends Serializable {

    NutsWorkspaceOptions getOptions();

    String getWorkspaceLocation();

    String getApiVersion();

    String getRuntimeId();

    String getRuntimeDependencies();

    String getExtensionDependencies();

    String getBootRepositories();

    NutsBootWorkspaceFactory getBootWorkspaceFactory();

    URL[] getClassWorldURLs();

    ClassLoader getClassWorldLoader();

    String getName();

    String getUuid();

    String getApiId();

    Set<String> getRuntimeDependenciesSet();

    Set<String> getExtensionDependenciesSet();

    String getJavaCommand();

    String getJavaOptions();

    NutsStoreLocationStrategy getStoreLocationStrategy();

    NutsOsFamily getStoreLocationLayout();

    NutsStoreLocationStrategy getRepositoryStoreLocationStrategy();

    String getStoreLocation(NutsStoreLocation location);
}
