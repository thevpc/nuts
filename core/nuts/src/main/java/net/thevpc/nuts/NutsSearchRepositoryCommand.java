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
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.Iterator;

/**
 *
 * @author vpc
 * @category SPI Base
 */
public interface NutsSearchRepositoryCommand extends NutsRepositoryCommand {

    NutsSearchRepositoryCommand setFilter(NutsIdFilter filter);

    NutsIdFilter getFilter();

    /**
     * this method should return immediately after initializing a valid iterator to be
     * retrieved by {@code getResult()}
     * @return {@code this} instance
     */
    @Override
    NutsSearchRepositoryCommand run();

    @Override
    NutsSearchRepositoryCommand setSession(NutsSession session);

    /**
     * fetchMode
     * @param fetchMode fetchMode
     * @return {@code this} instance
     */
    NutsSearchRepositoryCommand setFetchMode(NutsFetchMode fetchMode);

    /**
     * get fetchMode
     * @return {@code this} instance
     */
    NutsFetchMode getFetchMode();

    /**
     * this method should return immediately and returns valid iterator.
     * visiting iterator may be blocking but not this method call.
     * If {@code run()} method has not been called yet, it will be called.
     * @return {@code this} instance
     */
    Iterator<NutsId> getResult();

}
