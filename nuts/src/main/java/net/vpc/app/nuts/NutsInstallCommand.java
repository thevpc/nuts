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
public interface NutsInstallCommand extends NutsWorkspaceCommand{

    NutsInstallCommand id(NutsId id);

    NutsInstallCommand id(String id);

    NutsInstallCommand removeId(NutsId id);

    NutsInstallCommand removeId(String id);

    NutsInstallCommand ids(NutsId... ids);

    NutsInstallCommand ids(String... ids);

    NutsInstallCommand addId(NutsId id);

    NutsInstallCommand addId(String id);

    NutsInstallCommand addIds(NutsId... ids);

    NutsInstallCommand addIds(String... ids);

    NutsInstallCommand clearIds();

    NutsId[] getIds();

    NutsInstallCommand arg(String arg);

    NutsInstallCommand addArg(String arg);

    NutsInstallCommand args(Collection<String> args);

    NutsInstallCommand addArgs(Collection<String> args);

    NutsInstallCommand addArgs(String... args);

    NutsInstallCommand args(String... args);

    NutsInstallCommand clearArgs();

    String[] getArgs();

    NutsInstallCommand defaultVersion();

    NutsInstallCommand defaultVersion(boolean defaultVersion);

    /**
     *
     * @param defaultVersion when true, the installed version will be defined as
     * default
     * @return
     */
    NutsInstallCommand setDefaultVersion(boolean defaultVersion);

    boolean isDefaultVersion();

    boolean isIncludeCompanions();

    NutsInstallCommand includeCompanions();

    NutsInstallCommand includeCompanions(boolean includecompanions);

    NutsInstallCommand setIncludeCompanions(boolean includecompanions);

    int getResultCount();

    NutsDefinition[] getResult();

    @Override
    NutsInstallCommand session(NutsSession session);

    @Override
    NutsInstallCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments.
     * This is an override of the {@link NutsConfigurable#configure(java.lang.String...)}
     * to help return a more specific return type;
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsInstallCommand configure(String... args);

    /**
     * execute the command and return this instance
     * @return {@code this} instance
     */
    @Override
    NutsInstallCommand run();
}
