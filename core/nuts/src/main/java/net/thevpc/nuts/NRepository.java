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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NMapListener;

import java.util.List;
import java.util.Map;

/**
 * Nuts repository manages a set of packages
 *
 * @app.category Base
 * @since 0.5.4
 */
public interface NRepository {

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
     * remove repository listener
     *
     * @param listener listener
     * @return this
     */
    NRepository removeRepositoryListener(NRepositoryListener listener);

    /**
     * add repository listener
     *
     * @param listener listener
     * @return this
     */
    NRepository addRepositoryListener(NRepositoryListener listener);

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

    /**
     * add listener to user properties
     *
     * @param listener listener
     * @return this
     */
    NRepository addUserPropertyListener(NMapListener<String, Object> listener);

    /**
     * remove listener from user properties
     *
     * @param listener listener
     * @return this
     */
    NRepository removeUserPropertyListener(NMapListener<String, Object> listener);

    /**
     * return array of registered user properties listeners
     *
     * @return array of registered user properties listeners
     */
    List<NMapListener<String, Object>> getUserPropertyListeners();

    /**
     * available if local and the folder exists or remote and could ping the repository
     *
     * @return true if config is enabled and runtime is enabled
     * @param session session
     */
    boolean isAvailable(NSession session);

    /**
     * available if local and the folder exists or remote and could ping the repository
     *
     * @param force when force, check immediate availability and do not rely on cache
     * @param session session
     * @return true if config is enabled and runtime is enabled
     */
    boolean isAvailable(boolean force, NSession session);

    /**
     * available if local or remote repo exists and could deploy to
     *
     * @return true if config is enabled and runtime is enabled
     * @param session session
     */
    boolean isSupportedDeploy(NSession session);

    /**
     * available if local or remote repo exists and could deploy to
     *
     * @param force when force, check immediate availability and do not rely on cache
     * @param session session
     * @return true if config is enabled and runtime is enabled
     */
    boolean isSupportedDeploy(boolean force, NSession session);

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
     * @param session
     * @return {@code this} instance
     */
    NRepository setEnabled(boolean enabled, NSession session);

    boolean isRemote();
}
