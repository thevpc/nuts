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
package net.thevpc.nuts;

import net.thevpc.nuts.io.NPath;

import java.time.Instant;

/**
 * Information about installed artifact
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.5
 */
public interface NInstallInformation {
    /**
     * installation date
     *
     * @return installation date
     */
    NId getId();

    /**
     * installation date
     *
     * @return installation date
     */
    Instant getCreatedInstant();

    Instant getLastModifiedInstant();

    /**
     * true when the installed artifact is default version
     *
     * @return true when the installed artifact is default version
     */
    boolean isDefaultVersion();

    /**
     * installation formation path.
     *
     * @return installation formation path
     */
    NPath getInstallFolder();


    boolean isWasInstalled();

    boolean isWasRequired();

    /**
     * return the user responsible of the installation
     *
     * @return the user responsible of the installation
     */
    String getInstallUser();

    /**
     * return install status
     *
     * @return install status
     */
    NInstallStatus getInstallStatus();

    /**
     * return true if installed primary or dependency
     *
     * @return true if installed primary or dependency
     */
    boolean isInstalledOrRequired();

    String getSourceRepositoryName();

    String getSourceRepositoryUUID();

    boolean isJustReInstalled();

    boolean isJustInstalled();

    boolean isJustReRequired();

    boolean isJustRequired();
}
