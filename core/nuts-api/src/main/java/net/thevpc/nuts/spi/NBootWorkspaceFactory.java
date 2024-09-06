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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.boot.NBootOptions;

/**
 * Class responsible of creating and initializing Workspace
 * Created by vpc on 1/5/17.
 *
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NBootWorkspaceFactory {

    /**
     * when multiple factories are available, the best one is selected according to
     * the maximum value of {@code getBootSupportLevel(options)}.
     * Note that default value (for the reference implementation) is {@code NutsComponent.DEFAULT_SUPPORT}.
     * Any value less or equal to zero is ignored (and the factory is discarded)
     *
     * @param options command line options
     * @return support level
     */
    int getBootSupportLevel(NBootOptions options);

    /**
     * create workspace with the given options
     *
     * @param options boot init options
     * @return initialized workspace
     */
    NWorkspace createWorkspace(NBootOptions options);

}
