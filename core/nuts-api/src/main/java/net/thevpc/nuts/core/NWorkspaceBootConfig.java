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
import net.thevpc.nuts.util.NGetter;

import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 * @app.category Config
 */
public interface NWorkspaceBootConfig {
    /**
     * Checks if is immediate location.
     *
     * @return is immediate location result
     */
    @NGetter
    boolean isImmediateLocation();

    /**
     * Effective workspace name.
     *
     * @return effective workspace name result
     */
    @NGetter
    String effectiveWorkspaceName();

    /**
     * Boot path.
     *
     * @return boot path result
     */
    @NGetter
    String bootPath();

    /**
     * Effective workspace.
     *
     * @return effective workspace result
     */
    @NGetter
    String effectiveWorkspace();

    /**
     * Name.
     *
     * @return name result
     */
    @NGetter
    String name();

    /**
     * Workspace.
     *
     * @return workspace result
     */
    @NGetter
    String workspace();

    /**
     * Extensions.
     *
     * @return extensions result
     */
    @NGetter
    List<Extension> extensions();

    /**
     * Boot repositories.
     *
     * @return boot repositories result
     */
    @NGetter
    List<String> bootRepositories();

    /**
     * Store locations.
     *
     * @return store locations result
     */
    @NGetter
    Map<NStoreType, String> storeLocations();

    /**
     * Home locations.
     *
     * @return home locations result
     */
    @NGetter
    Map<NHomeLocation, String> homeLocations();

    /**
     * Store strategy.
     *
     * @return store strategy result
     */
    @NGetter
    NStoreStrategy storeStrategy();

    /**
     * Store layout.
     *
     * @return store layout result
     */
    @NGetter
    NOsFamily storeLayout();

    /**
     * Repository store strategy.
     *
     * @return repository store strategy result
     */
    @NGetter
    NStoreStrategy repositoryStoreStrategy();

    /**
     * Uuid.
     *
     * @return uuid result
     */
    @NGetter
    String uuid();

    /**
     * Checks if is system.
     *
     * @return is system result
     */
    @NGetter
    boolean isSystem();

    /**
     * Returns the store location.
     *
     * @param id id
     * @param folderType folder type
     * @return get store location result
     */
    String getStoreLocation(NId id, NStoreType folderType);

    /**
     * Returns the store location.
     *
     * @param storeLocation store location
     * @return get store location result
     */
    String getStoreLocation(NStoreType storeLocation);

    /**
     * Returns the home location.
     *
     * @param homeLocation home location
     * @return get home location result
     */
    String getHomeLocation(NHomeLocation homeLocation);

    /**
     * Returns the home location.
     *
     * @param storeLocation store location
     * @return get home location result
     */
    String getHomeLocation(NStoreType storeLocation);

    /**
     * @app.category Config
     */
    interface Extension {
        /**
         * Id.
         *
         * @return id result
         */
        @NGetter
        NId id();

        /**
         * Checks if is enabled.
         *
         * @return is enabled result
         */
        boolean isEnabled();
    }
}
