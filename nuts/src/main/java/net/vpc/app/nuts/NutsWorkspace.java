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
import java.util.Set;

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
    String uuid();

    /**
     * Workspace name
     *
     * @return uuid
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
    Set<String> companionIds();

    NutsWorkspaceAppsManager apps();

    NutsWorkspaceExtensionManager extensions();

    NutsWorkspaceConfigManager config();

    NutsRepositoryManager repos();

    NutsWorkspaceSecurityManager security();

    NutsFilterManager filters();

    NutsIOManager io();

    NutsLogManager log();

    ///////////////////// factory
    NutsSession createSession();


    NutsWorkspaceEvents events();

    NutsCommandLineFormat commandLine();

    /**
     * create id format instance
     *
     * @return id format
     * @since 0.5.5
     */
    NutsIdManager id();

    /**
     * create version format instance
     *
     * @return version format
     * @since 0.5.5
     */
    NutsVersionManager version();

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
    NutsDescriptorManager descriptor();

    /**
     * return dependency manager
     *
     * @return dependency manager
     * @since 0.8.0
     */
    NutsDependencyManager dependency();

    NutsFormatManager formats();

    NutsConcurrentManager concurrent();

    String getApiVersion();

    NutsId getApiId();

    NutsId getRuntimeId();

    NutsSdkManager sdks();

    NutsImportManager imports();

    NutsCommandAliasManager aliases();

    NutsWorkspaceEnvManager env();
}
