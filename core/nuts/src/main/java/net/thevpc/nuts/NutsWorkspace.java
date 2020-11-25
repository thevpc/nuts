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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

import java.util.Map;
import java.util.Set;

/**
 * Created by vpc on 1/5/17.
 *
 * @since 0.5.4
 * %category Base
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
    Set<NutsId> companionIds();

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


    NutsWorkspaceEventManager events();

    NutsCommandLineManager commandLine();

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

    NutsWorkspaceLocationManager locations();

    NutsWorkspaceEnvManager env();

    NutsStringBuilder str();
}
