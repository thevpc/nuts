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
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.util.Collection;

/**
 * @author thevpc
 * @since 0.5.4
 * @app.category Commands
 */
public interface NutsUpdateCommand extends NutsWorkspaceCommand {

    NutsUpdateCommand removeId(NutsId id);

    NutsUpdateCommand addId(NutsId id);

    NutsUpdateCommand removeId(String id);

    NutsUpdateCommand addId(String id);

    NutsUpdateCommand addIds(NutsId... ids);

    NutsUpdateCommand addIds(String... ids);

    NutsUpdateCommand clearIds();

    NutsId[] getIds();

    NutsUpdateCommand addLockedId(NutsId id);

    NutsUpdateCommand addLockedId(String id);

    NutsUpdateCommand addLockedIds(NutsId... ids);

    NutsUpdateCommand addLockedIds(String... ids);

    NutsUpdateCommand clearLockedIds();

    NutsId[] getLockedIds();

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand addArgs(Collection<String> args);

    NutsUpdateCommand addArgs(String... args);

    NutsUpdateCommand clearArgs();

    String[] getArgs();

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    /**
     * return true when include optional dependencies
     *
     * @return return true when include optional dependencies
     */
    boolean isOptional();

    /**
     * when true include optional dependencies
     *
     * @param includeOptional include optional
     * @return {@code this} instance
     */
    NutsUpdateCommand setOptional(boolean includeOptional);

    /**
     * return target api version required for updating other artifacts
     *
     * @return target api version required for updating other artifacts
     */
    NutsVersion getApiVersion();

    /**
     * set target api version required for updating other artifacts
     *
     * @param value target api version
     * @return {@code this} instance
     */
    NutsUpdateCommand setApiVersion(NutsVersion value);

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
     * update api, runtime, extensions, companions and all installed artifacts
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand setAll();

    boolean isApi();

    NutsUpdateCommand setApi(boolean enable);

    boolean isExtensions();

    NutsUpdateCommand setExtensions(boolean enable);

    boolean isCompanions();

    /**
     * update workspace companion versions
     * @param updateCompanions updateCompanions
     * @return {@code this} instance
     */
    NutsUpdateCommand setCompanions(boolean updateCompanions);

    boolean isRuntime();

    NutsUpdateCommand setRuntime(boolean enable);

    boolean isInstalled();

    NutsUpdateCommand setInstalled(boolean enable);

    NutsUpdateCommand addScope(NutsDependencyScope scope);

    NutsUpdateCommand addScopes(Collection<NutsDependencyScope> scopes);

    NutsUpdateCommand addScopes(NutsDependencyScope... scopes);

    NutsUpdateCommand clearScopes();

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsUpdateCommand copySession();

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
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
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
