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

import java.nio.file.Path;

/**
 * Repository command bound to FetchCommand used to fetch an artifact content from a specific repository.
 * @author vpc
 * @since 0.5.5
 */
public interface NutsFetchContentRepositoryCommand extends NutsRepositoryCommand {

    /**
     * preform command. Should be called after setting all parameters.
     * Result is retrievable with {@link #getResult()}.
     * @return {@code this} instance
     */
    @Override
    NutsFetchContentRepositoryCommand run();

    /**
     * return fetch result. if the command is not yet executed, it will be executed first.
     * @return return fetch result.
     */
    NutsContent getResult();

    /**
     * set id to fetch.
     * @param id id to fetch
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setId(NutsId id);

    /**
     * get id to fetch
     * @return id to fetch
     */
    NutsId getId();



    /**
     * set current session.
     * @param session current session
     * @return {@code this} instance
     */
    @Override
    NutsFetchContentRepositoryCommand setSession(NutsRepositorySession session);


    /**
     * path to store to
     * @return path to store to
     */
    Path getLocalPath();

    /**
     * set localPath to store to.
     * @param localPath localPath to store to
     * @return {@code this} instance
     */
    NutsFetchContentRepositoryCommand setLocalPath(Path localPath);

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
