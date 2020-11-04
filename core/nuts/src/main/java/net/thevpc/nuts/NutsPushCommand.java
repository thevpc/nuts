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

import java.util.Collection;

/**
 * Push command
 * @author vpc
 * @since 0.5.4
 * @category Commands
 */
public interface NutsPushCommand extends NutsWorkspaceCommand {

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand id(NutsId id);

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand id(String id);

    /**
     * remove id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand removeId(NutsId id);

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand addId(NutsId id);

    /**
     * remove id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand removeId(String id);

    /**
     * add id to push.
     *
     * @param id id to push
     * @return {@code this} instance
     */
    NutsPushCommand addId(String id);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NutsPushCommand addIds(NutsId... ids);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NutsPushCommand addIds(String... ids);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NutsPushCommand ids(NutsId... ids);

    /**
     * add ids to push.
     *
     * @param ids id to push
     * @return {@code this} instance
     */
    NutsPushCommand ids(String... ids);

    /**
     * reset ids to push for
     * @return {@code this} instance
     */
    NutsPushCommand clearIds();

    /**
     * return ids to push for
     * @return ids to push for
     */
    NutsId[] getIds();

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsPushCommand lockedId(NutsId id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsPushCommand lockedId(String id);

    /**
     * remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to unlock
     * @return {@code this} instance
     */
    NutsPushCommand removeLockedId(NutsId id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsPushCommand addLockedId(NutsId id);

    /**
     * remove locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to unlock
     * @return {@code this} instance
     */
    NutsPushCommand removeLockedId(String id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsPushCommand addLockedId(String id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param values id to lock
     * @return {@code this} instance
     */
    NutsPushCommand addLockedIds(NutsId... values);

    /**
     * define locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param values ids
     * @return {@code this} instance
     */
    NutsPushCommand addLockedIds(String... values);

    /**
     * define locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param values ids
     * @return {@code this} instance
     */
    NutsPushCommand lockedIds(NutsId... values);

    /**
     * define locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @param values ids
     * @return {@code this} instance
     */
    NutsPushCommand lockedIds(String... values);

    /**
     * reset locked ids
     * @return {@code this} instance
     */
    NutsPushCommand clearLockedIds();

    /**
     * return locked ids to prevent them to be updated or the force other ids to use them (the installed version).
     * @return locked ids
     */
    NutsId[] getLockedIds();

    /**
     * add argument to pass to the push command
     * @param arg argument
     * @return {@code this} instance
     */
    NutsPushCommand arg(String arg);

    /**
     * add argument to pass to the push command
     * @param arg argument
     * @return {@code this} instance
     */
    NutsPushCommand addArg(String arg);

    /**
     * add arguments to pass to the push command
     * @param args argument
     * @return {@code this} instance
     */
    NutsPushCommand args(String... args);

    /**
     * add arguments to pass to the push command
     * @param args argument
     * @return {@code this} instance
     */
    NutsPushCommand addArgs(String... args);

    /**
     * add arguments to pass to the push command
     * @param args argument
     * @return {@code this} instance
     */
    NutsPushCommand args(Collection<String> args);

    /**
     * add arguments to pass to the push command
     * @param args argument
     * @return {@code this} instance
     */
    NutsPushCommand addArgs(Collection<String> args);

    /**
     * clear all arguments to pass to the push command
     * @return {@code this} instance
     */
    NutsPushCommand clearArgs();

    /**
     * return all arguments to pass to the push command
     * @return all arguments to pass to the push command
     */
    String[] getArgs();

    /**
     * local only (installed or not)
     *
     * @return {@code this} instance
     */
    NutsPushCommand offline();

    /**
     * local only (installed or not)
     * @param offline enable offline mode
     * @return {@code this} instance
     */
    NutsPushCommand offline(boolean offline);

    /**
     * local only (installed or not)
     * @param offline enable offline mode
     * @return {@code this} instance
     */
    NutsPushCommand setOffline(boolean offline);

    /**
     * true when offline mode
     * @return true when offline mode
     */
    boolean isOffline();

    /**
     * repository to push from
     * @param repository repository to push from
     * @return {@code this} instance
     */
    NutsPushCommand repository(String repository);

    /**
     * repository to push from
     * @param repository repository to push from
     * @return {@code this} instance
     */
    NutsPushCommand setRepository(String repository);

    /**
     * repository to push from
     * @return repository to push from
     */
    String getRepository();

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand copySession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand run();

}
