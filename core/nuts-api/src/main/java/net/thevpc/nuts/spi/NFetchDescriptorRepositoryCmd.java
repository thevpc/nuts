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

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NFetchMode;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;

/**
 * Repository command used to fetch an artifact descriptor from a specific repository.
 *
 * @author thevpc
 * @app.category SPI Base
 * @since 0.5.5
 */
public interface NFetchDescriptorRepositoryCmd extends NRepositoryCmd {

    /**
     * return fetch result. if the command is not yet executed, it will be executed first.
     *
     * @return return fetch result.
     */
    NDescriptor getResult();

    /**
     * id to fetch
     *
     * @return id to fetch
     */
    NId getId();

    /**
     * set id to fetch
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NFetchDescriptorRepositoryCmd setId(NId id);

    /**
     * preform command. Should be called after setting all parameters.
     * Result is retrievable with {@link #getResult()}.
     *
     * @return {@code this} instance
     */
    @Override
    NFetchDescriptorRepositoryCmd run();

    /**
     * get fetchMode
     *
     * @return {@code this} instance
     */
    NFetchMode getFetchMode();

    /**
     * fetchMode
     *
     * @param fetchMode fetchMode
     * @return {@code this} instance
     */
    NFetchDescriptorRepositoryCmd setFetchMode(NFetchMode fetchMode);
}
