/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

/**
 * Repository command used to fetch an artifact descriptor from a specific repository.
 * @author thevpc
 * @since 0.5.5
 * %category SPI Base
 */
public interface NutsFetchDescriptorRepositoryCommand extends NutsRepositoryCommand {

    /**
     * preform command. Should be called after setting all parameters.
     * Result is retrievable with {@link #getResult()}.
     * @return {@code this} instance
     */
    @Override
    NutsFetchDescriptorRepositoryCommand run();

    /**
     * return fetch result. if the command is not yet executed, it will be executed first.
     * @return return fetch result.
     */
    NutsDescriptor getResult();

    /**
     * set id to fetch
     * @param id id to fetch
     * @return {@code this} instance
     */
    NutsFetchDescriptorRepositoryCommand setId(NutsId id);

    /**
     * id to fetch
     * @return id to fetch
     */
    NutsId getId();

    @Override
    NutsFetchDescriptorRepositoryCommand setSession(NutsSession session);

    /**
     * fetchMode
     * @param fetchMode fetchMode
     * @return {@code this} instance
     */
    NutsFetchDescriptorRepositoryCommand setFetchMode(NutsFetchMode fetchMode);

    /**
     * get fetchMode
     * @return {@code this} instance
     */
    NutsFetchMode getFetchMode();
}
