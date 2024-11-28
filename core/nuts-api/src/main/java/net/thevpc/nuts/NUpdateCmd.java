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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.ext.NExtensions;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUpdateCmd extends NWorkspaceCmd {
    static NUpdateCmd of() {
        return NExtensions.of(NUpdateCmd.class);
    }

    NUpdateCmd removeId(NId id);

    NUpdateCmd addId(NId id);

    NUpdateCmd removeId(String id);

    NUpdateCmd addId(String id);

    NUpdateCmd addIds(NId... ids);

    NUpdateCmd addIds(String... ids);

    NUpdateCmd clearIds();

    List<NId> getIds();

    NUpdateCmd addLockedId(NId id);

    NUpdateCmd addLockedId(String id);

    NUpdateCmd addLockedIds(NId... ids);

    NUpdateCmd addLockedIds(String... ids);

    NUpdateCmd clearLockedIds();

    List<NId> getLockedIds();

    NUpdateCmd addArg(String arg);

    NUpdateCmd addArgs(Collection<String> args);

    NUpdateCmd addArgs(String... args);

    NUpdateCmd clearArgs();

    List<String> getArgs();

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

    NUpdateCmd setEnableInstall(boolean enableInstall);

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
    NUpdateCmd setOptional(boolean includeOptional);

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
    NUpdateCmd setApiVersion(NVersion value);

    /**
     * @return null if no updates
     */
    NUpdateCmd update();

    /**
     * @return null if no updates
     */
    NUpdateCmd checkUpdates();

    /**
     * check for updates.
     *
     * @param applyUpdates if true updates will be applied
     * @return {@code this} instance
     */
    NUpdateCmd checkUpdates(boolean applyUpdates);

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
    NUpdateCmd setAll();

    boolean isApi();

    NUpdateCmd setApi(boolean enable);

    boolean isExtensions();

    NUpdateCmd setExtensions(boolean enable);

    boolean isCompanions();

    /**
     * update workspace companion versions
     *
     * @param updateCompanions updateCompanions
     * @return {@code this} instance
     */
    NUpdateCmd setCompanions(boolean updateCompanions);

    boolean isRuntime();

    NUpdateCmd setRuntime(boolean enable);

    boolean isInstalled();

    NUpdateCmd setInstalled(boolean enable);

    NUpdateCmd addScope(NDependencyScope scope);

    NUpdateCmd addScopes(Collection<NDependencyScope> scopes);

    NUpdateCmd addScopes(NDependencyScope... scopes);

    NUpdateCmd clearScopes();

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
    NUpdateCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdateCmd run();


    /**
     * return update repository filter
     * @return update repository filter
     * @since 0.8.4
     */
    NRepositoryFilter getRepositoryFilter();

    /**
     * set update repository filter
     * @param repositoryFilter update repository filter
     * @return {@code this} instance
     * @since 0.8.4
     */
    NUpdateCmd setRepositoryFilter(NRepositoryFilter repositoryFilter);
}
