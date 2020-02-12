/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Iterator;
import java.util.List;

/**
 * Classes implementations of {@code NutsIndexStore} handle
 * indexing of repositories to enable faster search.
 * @author vpc
 * @since 0.5.4
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
     * @return {@code this} instance
     */
    NutsIndexStore invalidate(NutsId id);

    /**
     * invalidate the artifact from the index and re-index it
     * @param id id to re-index
     * @return {@code this} instance
     */
    NutsIndexStore revalidate(NutsId id);

    /**
     * subscribe the current repository so the indexing
     * is processed.
     * @return {@code this} instance
     */
    NutsIndexStore subscribe();

    /**
     * unsubscribe the current repository so that the indexing
     * is disabled and the index is removed.
     * @return {@code this} instance
     */
    NutsIndexStore unsubscribe();

    /**
     * return true if the current repository is registered
     * @return true if the current repository is registered
     */
    boolean isSubscribed();
}
