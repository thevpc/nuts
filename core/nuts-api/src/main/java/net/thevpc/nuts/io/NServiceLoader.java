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
package net.thevpc.nuts.io;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.util.List;

/**
 * Component service class loader.
 *
 * @param <T> component type
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.4
 */
public interface NServiceLoader<T extends NComponent> {

    /**
     * load all NutsComponent instances matching criteria
     *
     * @param criteria criteria to match
     * @return load all NutsComponent instances matching criteria
     */
    List<T> loadAll(Object criteria);

    /**
     * load best NutsComponent instance matching criteria
     *
     * @param criteria criteria to match
     * @return load best NutsComponent instance matching criteria
     */
    NOptional<T> loadBest(Object criteria);
}
