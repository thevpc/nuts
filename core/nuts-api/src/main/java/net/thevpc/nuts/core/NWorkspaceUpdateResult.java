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

import net.thevpc.nuts.command.NUpdateResult;

import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.5
 */
public interface NWorkspaceUpdateResult {

    /**
     * Api.
     *
     * @return api result
     */
    NUpdateResult api();

    /**
     * Runtime.
     *
     * @return runtime result
     */
    NUpdateResult runtime();

    /**
     * Extensions.
     *
     * @return extensions result
     */
    List<NUpdateResult> extensions();

    /**
     * Artifacts.
     *
     * @return artifacts result
     */
    List<NUpdateResult> artifacts();

    /**
     * Checks if is updatable api.
     *
     * @return is updatable api result
     */
    boolean isUpdatableApi();

    /**
     * Checks if is updatable runtime.
     *
     * @return is updatable runtime result
     */
    boolean isUpdatableRuntime();

    /**
     * Checks if is updatable extensions.
     *
     * @return is updatable extensions result
     */
    boolean isUpdatableExtensions();

    /**
     * Checks if is update available.
     *
     * @return is update available result
     */
    boolean isUpdateAvailable();

    /**
     * Updates count.
     *
     * @return updates count result
     */
    int updatesCount();

    /**
     * Updatable.
     *
     * @return updatable result
     */
    List<NUpdateResult> updatable();

    /**
     * All results.
     *
     * @return all results result
     */
    List<NUpdateResult> allResults();
}
