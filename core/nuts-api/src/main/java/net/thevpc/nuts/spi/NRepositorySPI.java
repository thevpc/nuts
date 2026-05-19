/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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

import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfigManager;
import net.thevpc.nuts.core.NRepositoryListener;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NObservableMapListener;
import net.thevpc.nuts.util.NSetter;

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
    @NGetter
    String repositoryType();

    /**
     * return repository unique identifier
     *
     * @return repository unique identifier
     */
    @NGetter
    String uuid();

    /**
     * return repository name.
     * equivalent to config().name()
     *
     * @return repository name
     */
    @NGetter
    String name();

    /**
     * return parent workspace
     *
     * @return parent workspace
     */
    @NGetter
    NWorkspace workspace();

    /**
     * return parent repository or null
     *
     * @return parent repository or null
     */
    @NGetter
    NRepository parentRepository();

    /**
     * return repository configuration manager
     *
     * @return repository configuration manager
     */
    NRepositoryConfigManager config();

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
    NUndeployRepositoryCmd undeploy();

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

    /**
     * Repository Listeners
     *
     * @return Repository Listeners
     */
    List<NRepositoryListener> repositoryListeners();

    /**
     * return mutable instance of user properties
     *
     * @return mutable instance of user properties
     */
    @NGetter
    Map<String, Object> userProperties();

    /**
     * return array of registered user properties listeners
     *
     * @return array of registered user properties listeners
     */
    List<NObservableMapListener<String, Object>> userPropertyListeners();

    /**
     * enabled if config is enabled and runtime is enabled
     *
     * @return true if config is enabled and runtime is enabled
     */
    boolean isEnabled();

    /**
     * set runtime enabled
     *
     * @param enabled runtime enabled value
     * @return {@code this} instance
     */
    @NSetter
    NRepository enabled(boolean enabled);

    /**
     * true if fetch mode is accepted
     *
     * @param mode fetch mode
     * @return true if fetch mode is accepted
     */
    boolean isAcceptFetchMode(NFetchMode mode);
}
