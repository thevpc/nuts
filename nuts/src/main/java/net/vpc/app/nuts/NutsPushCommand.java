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

import java.util.Collection;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsPushCommand extends NutsWorkspaceCommand {

    NutsPushCommand id(NutsId id);

    NutsPushCommand id(String id);

    NutsPushCommand removeId(NutsId id);

    NutsPushCommand addId(NutsId id);

    NutsPushCommand removeId(String id);

    NutsPushCommand addId(String id);

    NutsPushCommand addIds(NutsId... ids);

    NutsPushCommand addIds(String... ids);

    NutsPushCommand ids(NutsId... ids);

    NutsPushCommand ids(String... ids);

    NutsPushCommand clearIds();

    NutsId[] getIds();

    NutsPushCommand frozenId(NutsId id);

    NutsPushCommand frozenId(String id);

    NutsPushCommand removeFrozenId(NutsId id);

    NutsPushCommand addFrozenId(NutsId id);

    NutsPushCommand removeFrozenId(String id);

    NutsPushCommand addFrozenId(String id);

    NutsPushCommand addFrozenIds(NutsId... ids);

    NutsPushCommand addFrozenIds(String... ids);

    NutsPushCommand frozenIds(NutsId... ids);

    NutsPushCommand frozenIds(String... ids);

    NutsPushCommand clearFrozenIds();

    NutsId[] getFrozenIds();

    NutsPushCommand arg(String arg);

    NutsPushCommand addArg(String arg);

    NutsPushCommand args(String... args);

    NutsPushCommand addArgs(String... args);

    NutsPushCommand args(Collection<String> args);

    NutsPushCommand addArgs(Collection<String> args);

    NutsPushCommand clearArgs();

    String[] getArgs();


    NutsPushCommand offline();

    NutsPushCommand offline(boolean offline);

    NutsPushCommand setOffline(boolean offline);

    boolean isOffline();

    NutsPushCommand repository(String repository);

    NutsPushCommand setRepository(String repository);

    String getRepository();

    @Override
    NutsPushCommand session(NutsSession session);

    @Override
    NutsPushCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments.
     * This is an override of the {@link NutsConfigurable#configure(java.lang.String...)}
     * to help return a more specific return type;
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand configure(String... args);

    /**
     * execute the command and return this instance
     * @return {@code this} instance
     */
    @Override
    NutsPushCommand run();

}
