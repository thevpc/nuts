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
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NDependencyScope;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.core.NWorkspaceUpdateResult;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.core.NRepositoryFilter;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUpdate extends NWorkspaceCmd {
    static NUpdate of() {
        return NExtensions.of(NUpdate.class);
    }

    NUpdate removeId(NId id);

    NUpdate addId(NId id);

    NUpdate removeId(String id);

    NUpdate addId(String id);

    NUpdate addIds(NId... ids);

    NUpdate addIds(String... ids);

    NUpdate clearIds();

    List<NId> getIds();

    NUpdate addLockedId(NId id);

    NUpdate addLockedId(String id);

    NUpdate addLockedIds(NId... ids);

    NUpdate addLockedIds(String... ids);

    NUpdate clearLockedIds();

    List<NId> getLockedIds();

    NUpdate addArg(String arg);

    NUpdate addArgs(Collection<String> args);

    NUpdate addArgs(String... args);

    NUpdate clearArgs();

    List<String> getArgs();

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    boolean isEnableInstall();

    NUpdate setEnableInstall(boolean enableInstall);

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
    NUpdate setOptional(boolean includeOptional);

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
    NUpdate setApiVersion(NVersion value);

    /**
     * @return null if no updates
     */
    NUpdate update();

    /**
     * @return null if no updates
     */
    NUpdate checkUpdates();

    /**
     * check for updates.
     *
     * @param applyUpdates if true updates will be applied
     * @return {@code this} instance
     */
    NUpdate checkUpdates(boolean applyUpdates);

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
    NUpdate setAll();

    boolean isApi();

    NUpdate setApi(boolean enable);

    boolean isExtensions();

    NUpdate setExtensions(boolean enable);

    boolean isCompanions();

    /**
     * update workspace companion versions
     *
     * @param updateCompanions updateCompanions
     * @return {@code this} instance
     */
    NUpdate setCompanions(boolean updateCompanions);

    boolean isRuntime();

    NUpdate setRuntime(boolean enable);

    boolean isInstalled();

    NUpdate setInstalled(boolean enable);

    NUpdate addScope(NDependencyScope scope);

    NUpdate addScopes(Collection<NDependencyScope> scopes);

    NUpdate addScopes(NDependencyScope... scopes);

    NUpdate clearScopes();

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
    NUpdate configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NUpdate run();


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
    NUpdate setRepositoryFilter(NRepositoryFilter repositoryFilter);
}
