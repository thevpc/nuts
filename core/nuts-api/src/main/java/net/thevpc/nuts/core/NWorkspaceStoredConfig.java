/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.core;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.platform.NHomeLocation;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;

import java.util.List;
import java.util.Map;

/**
 * Nuts read-only configuration
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public interface NWorkspaceStoredConfig {

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Store strategy.
     *
     * @return store strategy result
     */
    NStoreStrategy storeStrategy();

    /**
     * Repository store strategy.
     *
     * @return repository store strategy result
     */
    NStoreStrategy repositoryStoreStrategy();

    /**
     * Store layout.
     *
     * @return store layout result
     */
    NOsFamily storeLayout();

    /**
     * all home locations key/value map where keys are in the form "location"
     * and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NStoreType, String> storeLocations();

    /**
     * all home locations key/value map where keys are in the form
     * "osfamily:location" and values are absolute paths.
     *
     * @return home locations mapping
     */
    Map<NHomeLocation, String> homeLocations();

    /**
     * Returns the store location.
     *
     * @param folderType folder type
     * @return get store location result
     */
    String getStoreLocation(NStoreType folderType);

    /**
     * Returns the home location.
     *
     * @param homeLocation home location
     * @return get home location result
     */
    String getHomeLocation(NHomeLocation homeLocation);

    /**
     * Api id.
     *
     * @return api id result
     */
    NId apiId();

    /**
     * Runtime id.
     *
     * @return runtime id result
     */
    NId runtimeId();

    /**
     * Runtime dependencies.
     *
     * @return runtime dependencies result
     */
    String runtimeDependencies();

    /**
     * Boot repositories.
     *
     * @return boot repositories result
     */
    List<String> bootRepositories();

    /**
     * Java command.
     *
     * @return java command result
     */
    String javaCommand();

    /**
     * Java options.
     *
     * @return java options result
     */
    String javaOptions();

    /**
     * Checks if is system.
     *
     * @return is system result
     */
    boolean isSystem();
}
