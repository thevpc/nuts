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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NMapListener;

import java.util.List;
import java.util.Map;

/**
 * Nuts repository manages a set of packages
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NRepositorySPI {

    /**
     * return repository type
     *
     * @return repository type
     */
    String getRepositoryType();

    /**
     * return repository unique identifier
     *
     * @return repository unique identifier
     */
    String getUuid();

    /**
     * return repository name.
     * equivalent to config().name()
     *
     * @return repository name
     */
    String getName();

    /**
     * return parent workspace
     *
     * @return parent workspace
     */
    NWorkspace getWorkspace();

    /**
     * return parent repository or null
     *
     * @return parent repository or null
     */
    NRepository getParentRepository();

    /**
     * return repository configuration manager
     *
     * @return repository configuration manager
     */
    NRepositoryConfigManager config();

    /**
     * return repository security manager
     *
     * @return repository security manager
     */
    NRepositorySecurityManager security();

    /**
     * create deploy command
     *
     * @return deploy command
     */
    NDeployRepositoryCmd deploy();

    /**
     * create undeploy command
     *
     * @return undeploy command
     */
    NRepositoryUndeployCmd undeploy();

    /**
     * create push command
     *
     * @return push command
     */
    NPushRepositoryCmd push();

    /**
     * create fetchDescriptor command
     *
     * @return fetchDescriptor command
     */
    NFetchDescriptorRepositoryCmd fetchDescriptor();

    /**
     * create fetchContent command
     *
     * @return fetchContent command
     */
    NFetchContentRepositoryCmd fetchContent();

    /**
     * create search command
     *
     * @return search command
     */
    NSearchRepositoryCmd search();

    /**
     * create searchVersions command
     *
     * @return searchVersions command
     */
    NSearchVersionsRepositoryCmd searchVersions();

    /**
     * create update statistics command
     *
     * @return update statistics command
     */
    NUpdateRepositoryStatsCmd updateStatistics();

//    /**
//     * remove repository listener
//     *
//     * @param listener listener
//     */
//    void removeRepositoryListener(NutsRepositoryListener listener);
//
//    /**
//     * add repository listener
//     *
//     * @param listener listener
//     */
//    void addRepositoryListener(NutsRepositoryListener listener);

    /**
     * Repository Listeners
     *
     * @return Repository Listeners
     */
    List<NRepositoryListener> getRepositoryListeners();

    /**
     * return mutable instance of user properties
     *
     * @return mutable instance of user properties
     */
    Map<String, Object> getUserProperties();

//    /**
//     * add listener to user properties
//     * @param listener listener
//     */
//    NutsRepository addUserPropertyListener(NutsMapListener<String, Object> listener);
//
//    /**
//     * remove listener from user properties
//     * @param listener listener
//     */
//    NutsRepository removeUserPropertyListener(NutsMapListener<String, Object> listener);

    /**
     * return array of registered user properties listeners
     *
     * @return array of registered user properties listeners
     */
    List<NMapListener<String, Object>> getUserPropertyListeners();

    /**
     * enabled if config is enabled and runtime is enabled
     *
     * @return true if config is enabled and runtime is enabled
     */
    boolean isEnabled(NSession session);

    /**
     * set runtime enabled
     *
     * @param enabled runtime enabled value
     * @return {@code this} instance
     */
    NRepository setEnabled(boolean enabled, NSession session);

    /**
     * true if fetch mode is accepted
     *
     * @param mode    fetch mode
     * @param session session
     * @return true if fetch mode is accepted
     */
    boolean isAcceptFetchMode(NFetchMode mode, NSession session);
}
