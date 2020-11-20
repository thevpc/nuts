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
 *
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
 * @author vpc
 * @since 0.5.4
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

    NutsUpdateCommand lockedId(NutsId id);

    NutsUpdateCommand lockedId(String id);

    NutsUpdateCommand addLockedId(NutsId id);

    NutsUpdateCommand addLockedId(String id);

    NutsUpdateCommand lockedIds(NutsId... id);

    NutsUpdateCommand lockedIds(String... id);

    NutsUpdateCommand addLockedIds(NutsId... ids);

    NutsUpdateCommand addLockedIds(String... ids);

    NutsUpdateCommand clearLockedIds();

    NutsId[] getLockedIds();

    NutsUpdateCommand addArg(String arg);

    NutsUpdateCommand addArgs(Collection<String> args);

    NutsUpdateCommand addArgs(String... args);

    NutsUpdateCommand clearArgs();

    String[] getArgs();

    NutsUpdateCommand setEnableInstall(boolean enableInstall);

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

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
     * set target api version required for updating other artifacts
     *
     * @param value target api version
     * @return {@code this} instance
     */
    NutsUpdateCommand setApiVersion(String value);

    /**
     * return target api version required for updating other artifacts
     *
     * @return target api version required for updating other artifacts
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
     * update api, runtime, extensions, companions and all installed artifacts
     *
     * @return {@code this} instance
     */
    NutsUpdateCommand setAll();

    NutsUpdateCommand setApi(boolean enable);

    boolean isApi();

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
     * update installed artifacts
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