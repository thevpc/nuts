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

import java.util.Iterator;

/**
 * Classes implementations of {@code NutsIndexStore} handle
 * indexing of repositories to enable faster search.
 * @author thevpc
 * @since 0.5.4
 * @category Base
 */
public interface NutsIndexStore {

    /**
     * search all versions of the given artifact
     * @param id artifact to search for
     * @param session current session
     * @return all available versions (in the index)
     */
    Iterator<NutsId> searchVersions(NutsId id, NutsSession session);

    /**
     * search all artifacts matching the given filter
     * @param filter filter or null for all
     * @param session current session
     * @return all available versions (in the index)
     */
    Iterator<NutsId> search(NutsIdFilter filter, NutsSession session);

    /**
     * return true if the index is enabled
     * @return true if the index is enabled
     */
    boolean isEnabled();

    /**
     * enable of disable ot index
     * @param enabled new value
     * @return {@code this} instance
     */
    NutsIndexStore setEnabled(boolean enabled);

    /**
     * enable of disable ot index
     * @param enabled new value
     * @return {@code this} instance
     */
    NutsIndexStore enabled(boolean enabled);

    /**
     * enable index
     * @return {@code this} instance
     */
    NutsIndexStore enabled();

    /**
     * invalidate the artifact from the index
     * @param id id to invalidate
     * @param session session
     * @return {@code this} instance
     */
    NutsIndexStore invalidate(NutsId id, NutsSession session);

    /**
     * invalidate the artifact from the index and re-index it
     * @param id id to re-index
     * @param session session
     * @return {@code this} instance
     */
    NutsIndexStore revalidate(NutsId id, NutsSession session);

    /**
     * subscribe the current repository so the indexing
     * is processed.
     * @return {@code this} instance
     * @param session session
     */
    NutsIndexStore subscribe(NutsSession session);

    /**
     * unsubscribe the current repository so that the indexing
     * is disabled and the index is removed.
     * @return {@code this} instance
     * @param session session
     */
    NutsIndexStore unsubscribe(NutsSession session);

    /**
     * return true if the current repository is registered
     * @return true if the current repository is registered
     * @param session session
     */
    boolean isSubscribed(NutsSession session);
}
