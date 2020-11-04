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

/**
 * Repository command used to fetch an artifact descriptor from a specific repository.
 * @author vpc
 * @since 0.5.5
 * @category SPI Base
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
