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

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUpdateCommand extends NWorkspaceCommand {
    static NUpdateCommand of(NSession session) {
        return NExtensions.of(session).createComponent(NUpdateCommand.class).get();
    }

    NUpdateCommand removeId(NId id);

    NUpdateCommand addId(NId id);

    NUpdateCommand removeId(String id);

    NUpdateCommand addId(String id);

    NUpdateCommand addIds(NId... ids);

    NUpdateCommand addIds(String... ids);

    NUpdateCommand clearIds();

    List<NId> getIds();

    NUpdateCommand addLockedId(NId id);

    NUpdateCommand addLockedId(String id);

    NUpdateCommand addLockedIds(NId... ids);

    NUpdateCommand addLockedIds(String... ids);

    NUpdateCommand clearLockedIds();

    List<NId> getLockedIds();

    NUpdateCommand addArg(String arg);

    NUpdateCommand addArgs(Collection<String> args);

    NUpdateCommand addArgs(String... args);

    NUpdateCommand clearArgs();

    List<String> getArgs();

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

    NUpdateCommand setEnableInstall(boolean enableInstall);

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
    NUpdateCommand setOptional(boolean includeOptional);

    /**
     * return target api version required for updating other artifacts
     *
     * @return target api version required for updating other artifacts
     */
    NVersion getApiVersion();

    /**
     * set target api version required for updating other artifacts
     *
     * @param value target api version
     * @return {@code this} instance
     */
    NUpdateCommand setApiVersion(NVersion value);

    /**
     * @return null if no updates
     */
    NUpdateCommand update();

    /**
     * @return null if no updates
     */
    NUpdateCommand checkUpdates();

    /**
     * check for updates.
     *
     * @param applyUpdates if true updates will be applied
     * @return {@code this} instance
     */
    NUpdateCommand checkUpdates(boolean applyUpdates);

    /**
     * execute update check (if not already performed) then return result
     *
     * @return updates result
     */
    NWorkspaceUpdateResult getResult();

    int getResultCount();

    /**
     * update api, runtime, extensions, companions and all installed artifacts
     *
     * @return {@code this} instance
     */
    NUpdateCommand setAll();

    boolean isApi();

    NUpdateCommand setApi(boolean enable);

    boolean isExtensions();

    NUpdateCommand setExtensions(boolean enable);

    boolean isCompanions();

    /**
     * update workspace companion versions
     *
     * @param updateCompanions updateCompanions
     * @return {@code this} instance
     */
    NUpdateCommand setCompanions(boolean updateCompanions);

    boolean isRuntime();

    NUpdateCommand setRuntime(boolean enable);

    boolean isInstalled();

    NUpdateCommand setInstalled(boolean enable);

    NUpdateCommand addScope(NDependencyScope scope);

    NUpdateCommand addScopes(Collection<NDependencyScope> scopes);

    NUpdateCommand addScopes(NDependencyScope... scopes);

    NUpdateCommand clearScopes();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NUpdateCommand setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateCommand copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NUpdateCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateCommand run();
}
