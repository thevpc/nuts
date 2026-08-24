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
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.util.Collection;
import java.util.List;

/**
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NUpdate extends NWorkspaceCmd {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NUpdate of() {
        return NExtensions.of(NUpdate.class);
    }

    /**
     * Removes the specified id.
     *
     * @param id id
     * @return remove id result
     */
    NUpdate removeId(NId id);

    /**
     * Adds the specified id.
     *
     * @param id id
     * @return add id result
     */
    NUpdate addId(NId id);

    /**
     * Removes the specified id.
     *
     * @param id id
     * @return remove id result
     */
    NUpdate removeId(String id);

    /**
     * Adds the specified id.
     *
     * @param id id
     * @return add id result
     */
    NUpdate addId(String id);

    /**
     * Adds the specified ids.
     *
     * @param ids ids
     * @return add ids result
     */
    NUpdate addIds(NId... ids);

    /**
     * Adds the specified ids.
     *
     * @param ids ids
     * @return add ids result
     */
    NUpdate addIds(String... ids);

    /**
     * Clear ids.
     *
     * @return clear ids result
     */
    NUpdate clearIds();

    /**
     * Ids.
     *
     * @return ids result
     */
    @NGetter
    List<NId> ids();

    /**
     * Adds the specified locked id.
     *
     * @param id id
     * @return add locked id result
     */
    NUpdate addLockedId(NId id);

    /**
     * Adds the specified locked id.
     *
     * @param id id
     * @return add locked id result
     */
    NUpdate addLockedId(String id);

    /**
     * Adds the specified locked ids.
     *
     * @param ids ids
     * @return add locked ids result
     */
    NUpdate addLockedIds(NId... ids);

    /**
     * Adds the specified locked ids.
     *
     * @param ids ids
     * @return add locked ids result
     */
    NUpdate addLockedIds(String... ids);

    /**
     * Clear locked ids.
     *
     * @return clear locked ids result
     */
    NUpdate clearLockedIds();

    /**
     * Locked ids.
     *
     * @return locked ids result
     */
    @NGetter
    List<NId> lockedIds();

    /**
     * Adds the specified arg.
     *
     * @param arg arg
     * @return add arg result
     */
    NUpdate addArg(String arg);

    /**
     * Adds the specified args.
     *
     * @param args args
     * @return add args result
     */
    NUpdate addArgs(Collection<String> args);

    /**
     * Adds the specified args.
     *
     * @param args args
     * @return add args result
     */
    NUpdate addArgs(String... args);

    /**
     * Clear args.
     *
     * @return clear args result
     */
    NUpdate clearArgs();

    /**
     * Args.
     *
     * @return args result
     */
    @NGetter
    List<String> args();

    /**
     * if true enable installing new artifacts when an update is request for
     * non installed packages.
     *
     * @return true if enable install
     */
    @NGetter
    boolean isEnableInstall();

    /**
     * Enable install.
     *
     * @param enableInstall enable install
     * @return enable install result
     */
    @NSetter
    NUpdate enableInstall(boolean enableInstall);

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
    @NSetter
    NUpdate setOptional(boolean includeOptional);

    /**
     * return target api version required for updating other artifacts
     *
     * @return target api version required for updating other artifacts
     */
    @NGetter
    NVersion apiVersion();

    /**
     * set target api version required for updating other artifacts
     *
     * @param value target api version
     * @return {@code this} instance
     */
    @NSetter
    NUpdate apiVersion(NVersion value);

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

    /**
     * Returns the result count.
     *
     * @return get result count result
     */
    int getResultCount();

    /**
     * update api, runtime, extensions, companions and all installed artifacts
     *
     * @return {@code this} instance
     */
    NUpdate all();

    /**
     * Checks if is api.
     *
     * @return is api result
     */
    boolean isApi();

    /**
     * Api.
     *
     * @param enable enable
     * @return api result
     */
    @NSetter
    NUpdate api(boolean enable);

    /**
     * Checks if is extensions.
     *
     * @return is extensions result
     */
    boolean isExtensions();

    /**
     * Extensions.
     *
     * @param enable enable
     * @return extensions result
     */
    @NSetter
    NUpdate extensions(boolean enable);

    /**
     * Checks if is companions.
     *
     * @return is companions result
     */
    boolean isCompanions();

    /**
     * update workspace companion versions
     *
     * @param updateCompanions updateCompanions
     * @return {@code this} instance
     */
    @NSetter
    NUpdate companions(boolean updateCompanions);

    /**
     * Checks if is runtime.
     *
     * @return is runtime result
     */
    boolean isRuntime();

    /**
     * Runtime.
     *
     * @param enable enable
     * @return runtime result
     */
    @NSetter
    NUpdate runtime(boolean enable);

    /**
     * Checks if is installed.
     *
     * @return is installed result
     */
    boolean isInstalled();

    /**
     * Installed.
     *
     * @param enable enable
     * @return installed result
     */
    @NSetter
    NUpdate installed(boolean enable);

    /**
     * Adds the specified scope.
     *
     * @param scope scope
     * @return add scope result
     */
    NUpdate addScope(NDependencyScope scope);

    /**
     * Adds the specified scopes.
     *
     * @param scopes scopes
     * @return add scopes result
     */
    NUpdate addScopes(Collection<NDependencyScope> scopes);

    /**
     * Adds the specified scopes.
     *
     * @param scopes scopes
     * @return add scopes result
     */
    NUpdate addScopes(NDependencyScope... scopes);

    /**
     * Clear scopes.
     *
     * @return clear scopes result
     */
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
    NRepositoryFilter repositoryFilter();

    /**
     * set update repository filter
     * @param repositoryFilter update repository filter
     * @return {@code this} instance
     * @since 0.8.4
     */
    @NSetter
    NUpdate repositoryFilter(NRepositoryFilter repositoryFilter);
}
