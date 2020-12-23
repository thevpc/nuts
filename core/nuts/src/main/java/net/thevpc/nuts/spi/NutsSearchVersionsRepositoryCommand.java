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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsFetchMode;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsSession;

import java.util.Iterator;

/**
 *
 * @author thevpc
 * @category SPI Base
 */
public interface NutsSearchVersionsRepositoryCommand extends NutsRepositoryCommand {

    NutsSearchVersionsRepositoryCommand setFilter(NutsIdFilter filter);

    NutsIdFilter getFilter();

    NutsSearchVersionsRepositoryCommand setId(NutsId id);

    NutsId getId();

    /**
     * run this command and return {@code this} instance
     * @return {@code this} instance
     */
    @Override
    NutsSearchVersionsRepositoryCommand run();

    @Override
    NutsSearchVersionsRepositoryCommand setSession(NutsSession session);

    /**
     * fetchMode
     * @param fetchMode fetchMode
     * @return {@code this} instance
     */
    NutsSearchVersionsRepositoryCommand setFetchMode(NutsFetchMode fetchMode);

    /**
     * get fetchMode
     * @return {@code this} instance
     */
    NutsFetchMode getFetchMode();

    Iterator<NutsId> getResult();
}
