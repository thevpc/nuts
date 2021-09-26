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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.*;

/**
 * Repository command bound to FetchCommand used to fetch an artifact content from a specific repository.
 * @author thevpc
 * @since 0.5.5
 * @app.category SPI Base
 */
public interface NutsFetchContentRepositoryCommand extends NutsRepositoryCommand {

    /**
     * return fetch result. if the command is not yet executed, it will be executed first.
     * @return return fetch result.
     */
    NutsContent getResult();

    /**
     * get id to fetch
     * @return id to fetch
     */
    NutsId getId();

    /**
     * set id to fetch.
     * @param id id to fetch
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setId(NutsId id);

    /**
     * set current session.
     * @param session current session
     * @return {@code this} instance
     */
    @Override
    NutsFetchContentRepositoryCommand setSession(NutsSession session);

    /**
     * preform command. Should be called after setting all parameters.
     * Result is retrievable with {@link #getResult()}.
     * @return {@code this} instance
     */
    @Override
    NutsFetchContentRepositoryCommand run();

    /**
     * get fetchMode
     * @return {@code this} instance
     */
    NutsFetchMode getFetchMode();

    /**
     * fetchMode
     * @param fetchMode fetchMode
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setFetchMode(NutsFetchMode fetchMode);

    /**
     * path to store to
     * @return path to store to
     */
    String getLocalPath();

    /**
     * set localPath to store to.
     * @param localPath localPath to store to
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setLocalPath(String localPath);

    /**
     * description
     * @return description
     */
    NutsDescriptor getDescriptor();

    /**
     * set descriptor to fetch.
     * @param descriptor descriptor to fetch
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setDescriptor(NutsDescriptor descriptor);

}
