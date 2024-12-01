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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NIterator;

/**
 * Classes implementations of {@code NutsIndexStore} handle
 * indexing of repositories to enable faster search.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NIndexStore {

    /**
     * search all versions of the given artifact
     *
     * @param id artifact to search for
     * @return all available versions (in the index)
     */
    NIterator<NId> searchVersions(NId id);

    /**
     * search all artifacts matching the given filter
     *
     * @param filter filter or null for all
     * @return all available versions (in the index)
     */
    NIterator<NId> search(NIdFilter filter);

    /**
     * return true if the index is enabled
     *
     * @return true if the index is enabled
     */
    boolean isEnabled();

    /**
     * enable of disable ot index
     *
     * @param enabled new value
     * @return {@code this} instance
     */
    NIndexStore setEnabled(boolean enabled);

    /**
     * invalidate the artifact from the index
     *
     * @param id id to invalidate
     * @return {@code this} instance
     */
    NIndexStore invalidate(NId id);

    /**
     * invalidate the artifact from the index and re-index it
     *
     * @param id id to re-index
     * @return {@code this} instance
     */
    NIndexStore revalidate(NId id);

    /**
     * subscribe the current repository so the indexing
     * is processed.
     *
     * @return {@code this} instance
     */
    NIndexStore subscribe();

    /**
     * unsubscribe the current repository so that the indexing
     * is disabled and the index is removed.
     *
     * @return {@code this} instance
     */
    NIndexStore unsubscribe();

    /**
     * return true if the current repository is registered
     *
     * @return true if the current repository is registered
     */
    boolean isSubscribed();
}
