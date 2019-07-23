/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Collection;

/**
 * @author vpc
 * @since 0.5.4
 */
public interface NutsUpdateCommand extends NutsWorkspaceCommand {

    NutsUpdateCommand id(NutsId id);

    NutsUpdateCommand id(String id);

    NutsUpdateCommand removeId(NutsId id);

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand removeId(String id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand ids(NutsId... id);

    NutsUpdateCommand ids(String... id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsUpdateCommand clearIds();

    NutsId[] getIds();

    NutsUpdateCommand frozenId(NutsId id);

    NutsUpdateCommand frozenId(String id);

    NutsUpdateCommand addFrozenId(NutsId id);

    NutsUpdateCommand addFrozenId(String id);

    NutsUpdateCommand frozenIds(NutsId... id);

    NutsUpdateCommand frozenIds(String... id);

    NutsUpdateCommand addFrozenIds(NutsId... ids);

    NutsUpdateCommand addFrozenIds(String... ids);

    NutsUpdateCommand clearFrozenIds();

    NutsId[] getFrozenIds();

    NutsUpdateCommand arg(String arg);

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand args(String... arg);

    NutsUpdateCommand addArgs(Collection<String> args);

    NutsUpdateCommand args(Collection<String> arg);

    NutsUpdateCommand addArgs(String... args);

    NutsUpdateCommand clearArgs();

    String[] getArgs();

    NutsUpdateCommand enableInstall();

    NutsUpdateCommand enableInstall(boolean enableInstall);

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    /**
     * if true enable installing new components when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

    /**
     * include optional dependencies.
     * equivalent to @code optional(true)}
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand optional();

    /**
     * when true include optional dependencies
     *
     * @param includeOptional include optional
     * @return {@code this} instance
     */
    NutsUpdateCommand optional(boolean includeOptional);

    /**
     * when true include optional dependencies
     *
     * @param includeOptional include optional
     * @return {@code this} instance
     */
    NutsUpdateCommand setOptional(boolean includeOptional);

    /**
     * return true when include optional dependencies
     *
     * @return return true when include optional dependencies
     */
    boolean isOptional();

    /**
     * set target api version required for updating other components
     *
     * @param value target api version
     * @return {@code this} instance
     */
    NutsUpdateCommand apiVersion(String value);

    /**
     * set target api version required for updating other components
     *
     * @param value target api version
     * @return {@code this} instance
     */
    NutsUpdateCommand setApiVersion(String value);

    /**
     * return target api version required for updating other components
     *
     * @return target api version required for updating other components
     */
    String getApiVersion();

    /**
     * @return null if no updates
     */
    NutsUpdateCommand update();

    /**
     * @return null if no updates
     */
    NutsUpdateCommand checkUpdates();

    /**
     * check for updates.
     *
     * @param applyUpdates if true updates will be applied
     * @return {@code this} instance
     */
    NutsUpdateCommand checkUpdates(boolean applyUpdates);

    /**
     * execute update check (if not already performed) then return result
     *
     * @return updates result
     */
    NutsWorkspaceUpdateResult getResult();

    int getResultCount();

    /**
     * update api, runtime, extensions and companions
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand workspace();

    /**
     * update api, runtime, extensions, companions and all installed components
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand all();

    /**
     * update workspace api version
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand api();

    NutsUpdateCommand api(boolean enable);

    NutsUpdateCommand setApi(boolean enable);

    boolean isApi();


    /**
     * update workspace extension components
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand extensions();

    NutsUpdateCommand extensions(boolean enable);

    NutsUpdateCommand setExtensions(boolean enable);

    boolean isExtensions();


    /**
     * update workspace companion versions
     * @return {@code this} instance
     */
    NutsUpdateCommand companions();

    NutsUpdateCommand companions(boolean enable);

    NutsUpdateCommand setCompanions(boolean updateCompanions);

    boolean isCompanions();

    /**
     * update workspace runtime version
     * @return {@code this} instance
     */
    NutsUpdateCommand runtime();

    NutsUpdateCommand setRuntime(boolean enable);

    NutsUpdateCommand runtime(boolean enable);

    boolean isRuntime();

    /**
     * update installed components
     * @return {@code this} instance
     */
    NutsUpdateCommand installed();

    NutsUpdateCommand setInstalled(boolean enable);

    boolean isInstalled();

    NutsUpdateCommand installed(boolean enable);


    NutsUpdateCommand scope(NutsDependencyScope scope);

    NutsUpdateCommand addScope(NutsDependencyScope scope);

    NutsUpdateCommand scopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand scopes(NutsDependencyScope... scopes);

    NutsUpdateCommand addScopes(NutsDependencyScope... scopes);

    NutsUpdateCommand clearScopes();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUpdateCommand session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsUpdateCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsUpdateCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsUpdateCommand run();
}
