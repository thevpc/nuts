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
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NId;

import java.util.List;

/**
 * package update result
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUpdateResult {

    /**
     * artifact id
     *
     * @return package id
     */
    NId getId();

    boolean isInstalled();

    /**
     * return installed/local definition or null
     *
     * @return installed/local definition or null
     */
    NDefinition getInstalled();

    /**
     * return available definition or null
     *
     * @return available definition or null
     */
    NDefinition getAvailable();

    /**
     * return true if the update was forced
     *
     * @return true if the update was forced
     */
    boolean isUpdateForced();

    /**
     * return true if the update was applied
     *
     * @return true if the update was applied
     */
    boolean isUpdateApplied();

    /**
     * return true if any update is available.
     * equivalent to {@code isUpdateVersionAvailable() || isUpdateStatusAvailable()}
     *
     * @return true if any update is available
     */
    boolean isUpdatable();

    /**
     * return true if artifact has newer available version
     *
     * @return true if artifact has newer available version
     * @since 0.5.7
     */
    boolean isUpdateVersionAvailable();

    /**
     * return true if artifact has no version update
     * but still have status (default) to be updated
     *
     * @return artifact should have its status updated.
     * @since 0.5.7
     */
    boolean isUpdateStatusAvailable();

    /**
     * return update dependencies
     *
     * @return update dependencies
     */
    List<NId> getDependencies();

}
