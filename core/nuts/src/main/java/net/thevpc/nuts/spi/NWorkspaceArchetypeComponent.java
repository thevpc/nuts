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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSession;

/**
 * Archetype implementation of this component define the behaviour of
 * the workspace upon creation. By default thread implementations are provided by nuts:
 * 'minimal' archetype does little to nothing and hence does not populate the
 * configuration with popular repositories for instance.
 * 'default' archetype prepares the workspace for usual usage and populates with maven
 * and local repositories.
 * 'server' archetype prepares is intended for network exposed workspace.
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NWorkspaceArchetypeComponent extends NComponent/*archetype id*/ {

    /**
     * archetype identifier name
     *
     * @return archetype identifier name
     */
    String getName();

    /**
     * called to initialize the workspace
     *
     * @param session session
     */
    void initializeWorkspace(NSession session);

    /**
     * called after the workspace starts to perform extra configuration such
     * as installing packages.
     *
     * @param session session
     */
    void startWorkspace(NSession session);
}
