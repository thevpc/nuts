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

import java.util.Map;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 */
@NutsPrototype
public interface NutsWorkspace extends NutsComponent<Object> {

    /**
     * Workspace identifier, guaranteed to be unique cross machines
     * @return uuid
     */
    String getUuid();

    /**
     * equivalent to {@link #getUuid()}
     * @return 
     */
    String uuid();

    /**
     * Open a new workspace with the provided options. When options is null, it
     * i considered as a new empty instance of the options class.
     *
     * @param options creation options
     * @return a new instance of workspace
     */
    NutsWorkspace openWorkspace(NutsWorkspaceOptions options);

    //COMMANDS
    NutsSearchCommand search();

    NutsFetchCommand fetch();

    NutsDeployCommand deploy();

    NutsUndeployCommand undeploy();

    NutsExecCommand exec();

    NutsInstallCommand install();

    NutsUninstallCommand uninstall();

    NutsUpdateCommand update();

    NutsPushCommand push();

    NutsUpdateStatisticsCommand updateStatistics();

    ///////////////////// Environment
    Map<String, Object> getUserProperties();

    ///////////////////// sub system
    NutsWorkspaceExtensionManager extensions();

    NutsWorkspaceConfigManager config();

    NutsWorkspaceSecurityManager security();

    NutsIOManager io();

    NutsParseManager parse();

    NutsFormatManager format();

    ///////////////////// utilities
    String resolveDefaultHelp(Class clazz);

    NutsId resolveId(Class clazz);

    NutsId[] resolveIds(Class clazz);

    ///////////////////// factory
    NutsSession createSession();

    NutsDescriptorBuilder descriptorBuilder();

    NutsDependencyBuilder dependencyBuilder();

    NutsIdBuilder idBuilder();

    ///////////////////// Listeners
    void removeRepositoryListener(NutsRepositoryListener listener);

    void addRepositoryListener(NutsRepositoryListener listener);

    NutsRepositoryListener[] getRepositoryListeners();

    void addUserPropertyListener(NutsMapListener<String, Object> listener);

    void removeUserPropertyListener(NutsMapListener<String, Object> listener);

    NutsMapListener<String, Object>[] getUserPropertyListeners();

    void removeWorkspaceListener(NutsWorkspaceListener listener);

    void addWorkspaceListener(NutsWorkspaceListener listener);

    NutsWorkspaceListener[] getWorkspaceListeners();

}
