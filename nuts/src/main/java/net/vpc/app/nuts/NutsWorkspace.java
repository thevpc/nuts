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
 * @category Base
 */
public interface NutsWorkspace extends NutsComponent<NutsWorkspaceOptions> {

    /**
     * Workspace identifier, guaranteed to be unique cross machines
     *
     * @return uuid
     */
    String getUuid();

    /**
     * equivalent to {@link #getUuid()}
     *
     * @return workspace uuid
     */
    String uuid();

    /**
     * Workspace name
     *
     * @return uuid
     */
    String getName();

    /**
     * equivalent to {@link #getName()}
     *
     * @return workspace name
     */
    String name();

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
    Map<String, Object> userProperties();

    ///////////////////// sub system
    NutsWorkspaceExtensionManager extensions();

    NutsWorkspaceConfigManager config();

    NutsWorkspaceSecurityManager security();

    NutsIOManager io();

    NutsLogManager log();

    ///////////////////// factory
    NutsSession createSession();

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

    void removeInstallListener(NutsInstallListener listener);

    void addInstallListener(NutsInstallListener listener);

    NutsInstallListener[] getInstallListeners();

    NutsCommandLineFormat commandLine();

    /**
     * create json format instance
     *
     * @return json format
     * @since 0.5.5
     */
    NutsJsonFormat json();

    /**
     * create xml format instance
     *
     * @return xml format
     * @since 0.5.5
     */
    NutsXmlFormat xml();

    /**
     * create element format instance
     *
     * @return element format
     * @since 0.5.5
     */
    NutsElementFormat element();

    /**
     * create id format instance
     *
     * @return id format
     * @since 0.5.5
     */
    NutsIdFormat id();

    /**
     * create string format instance
     *
     * @return string format
     * @since 0.5.5
     */
    NutsStringFormat str();

    /**
     * create version format instance
     *
     * @return version format
     * @since 0.5.5
     */
    NutsVersionFormat version();

    /**
     * create info format instance
     *
     * @return info format
     * @since 0.5.5
     */
    NutsInfoFormat info();

    /**
     * create descriptor format instance
     *
     * @return descriptor format
     * @since 0.5.5
     */
    NutsDescriptorFormat descriptor();

    /**
     * create dependency format instance
     *
     * @return dependency format
     * @since 0.5.5
     */
    NutsDependencyFormat dependency();

    /**
     * create tree format instance
     *
     * @return tree format
     * @since 0.5.5
     */
    NutsTreeFormat tree();

    /**
     * create table format instance
     *
     * @return json table
     * @since 0.5.5
     */
    NutsTableFormat table();

    /**
     * create properties format instance
     *
     * @return properties format
     * @since 0.5.5
     */
    NutsPropertiesFormat props();

    /**
     * create object format instance
     *
     * @return object format
     * @since 0.5.5
     */
    NutsObjectFormat object();

    /**
     * create iterable format instance
     *
     * @return iterable format
     * @since 0.5.6
     */
    NutsIterableOutput iter();
}
