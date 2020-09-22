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
package net.vpc.app.nuts;

/**
 * Push Command
 * @author vpc
 * @category SPI Base
 */
public interface NutsPushRepositoryCommand extends NutsRepositoryCommand {

    /**
     * local only (installed or not)
     * @param offline enable offline mode
     * @return {@code this} instance
     */
    NutsPushRepositoryCommand setOffline(boolean offline);

    /**
     * set id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushRepositoryCommand setId(NutsId id);

    /**
     * return id to push.
     *
     * @return id to push
     */
    NutsId getId();

    /**
     * set session
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsPushRepositoryCommand setSession(NutsSession session);

    /**
     * run this command and return {@code this} instance
     * @return {@code this} instance
     */
    @Override
    NutsPushRepositoryCommand run();

    /**
     * return all arguments to pass to the push command
     * @return all arguments to pass to the push command
     */
    String[] getArgs();

    /**
     * args args to push
     * @param args args to push
     * @return {@code this} instance
     */
    NutsPushRepositoryCommand setArgs(String[] args);

    /**
     * true if offline mode is activated
     * @return true if offline mode is activated
     */
    boolean isOffline();

    /**
     * repository to push from
     * @param repository repository to push from
     * @return {@code this} instance
     */
    NutsPushRepositoryCommand setRepository(String repository);

    /**
     * repository to push from
     * @return repository to push from
     */
    String getRepository();
}
