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
 *
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

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.time.Instant;

/**
 * Fetch command class helps fetching/retrieving a artifact with all of its
 * files.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NFetch extends NWorkspaceCmd {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NFetch of() {
        return NExtensions.of(NFetch.class);
    }

    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @return of result
     */
    static NFetch of(NId id) {
        /**
         * Creates a new instance of of.
         *
         * @param ).id(id ).id(id
         * @return of result
         */
        return of().id(id);
    }

    /**
     * Creates a new instance of of.
     *
     * @param id id
     * @return of result
     */
    static NFetch of(String id) {
        /**
         * Creates a new instance of of.
         *
         * @param ).id(id ).id(id
         * @return of result
         */
        return of().id(id);
    }

    /**
     * Creates a new instance of of nuts api.
     *
     * @return of nuts api result
     */
    static NFetch ofNutsApi() {
        /**
         * Creates a new instance of of.
         *
         * @param ).id(NWorkspace.of().apiId() ).id(n workspace.of().api id()
         * @return of result
         */
        return of().id(NWorkspace.of().apiId());
    }

    /**
     * Creates a new instance of of nuts app.
     *
     * @return of nuts app result
     */
    static NFetch ofNutsApp() {
        /**
         * Creates a new instance of of.
         *
         * @param ).id(NWorkspace.of().appId() ).id(n workspace.of().app id()
         * @return of result
         */
        return of().id(NWorkspace.of().appId());
    }

    /**
     * Creates a new instance of of nuts runtime.
     *
     * @return of nuts runtime result
     */
    static NFetch ofNutsRuntime() {
        /**
         * Creates a new instance of of.
         *
         * @param ).id(NWorkspace.of().runtimeId() ).id(n workspace.of().runtime id()
         * @return of result
         */
        return of().id(NWorkspace.of().runtimeId());
    }

    ////////////////////////////////////////////////////////
    // Setters

    /// /////////////////////////////////////////////////////

    /**
     * Fetch strategy.
     *
     * @return fetch strategy result
     */
    NOptional<NFetchStrategy> fetchStrategy();

    /**
     * Transitive.
     *
     * @return transitive result
     */
    NOptional<Boolean> transitive();

    /**
     * Fetch strategy.
     *
     * @param fetchStrategy fetch strategy
     * @return fetch strategy result
     */
    NFetch fetchStrategy(NFetchStrategy fetchStrategy);

    /**
     * Transitive.
     *
     * @param transitive transitive
     * @return transitive result
     */
    NFetch transitive(Boolean transitive);

    /**
     * return expired date/time or zero if not set. Expire time is used to
     * expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    @NGetter
    NOptional<Instant> expireTime();

    /**
     * set expire instant. Expire time is used to expire any cached file that
     * was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    @NSetter
    NFetch expireTime(Instant value);

    /**
     * when true, NArtifactNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    @NGetter
    boolean isFailFast();

    /**
     * id to fetch
     *
     * @return id to fetch
     */
    @NGetter
    NId id();

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NFetch id(String id);

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    @NSetter
    NFetch id(NId id);

//    NutsFetch copyFrom(NutsFetch other);
    ////////////////////////////////////////////////////////
    // Getter
    ////////////////////////////////////////////////////////

    /**
     * return result as content
     *
     * @return result as content
     */
    NPath getResultContent();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////

    /**
     * return result as id
     *
     * @return result as id
     */
    NId getResultId();

    /**
     * return result as content hash string
     *
     * @return result as content hash string
     */
    String getResultContentHash();

    /**
     * return result as descriptor hash string
     *
     * @return result as descriptor hash string
     */
    String getResultDescriptorHash();

    /**
     * return result as artifact definition
     *
     * @return result as artifact definition
     */
    NDefinition getResultDefinition();

    /**
     * return result as descriptor
     *
     * @return result as descriptor
     */
    NDescriptor getResultDescriptor();

    /**
     * Returns the result effective descriptor.
     *
     * @return get result effective descriptor result
     */
    NDescriptor getResultEffectiveDescriptor();

    /**
     * return result as InstallInformation
     *
     * @return result as InstallInformation
     * @since 0.8.0
     */
    NInstallInformation getResultInstallInformation();

    /**
     * return result as content path
     *
     * @return result as content path
     */
    NPath getResultPath();

    /**
     * create copy (new instance) of {@code this} command
     *
     * @return copy (new instance) of {@code this} command
     */
    NFetch copy();

    ///////////////////////
    // REDIFNIED
    ///////////////////////

    /**
     * copy into {@code this} from {@code other} fetch command
     *
     * @param other copy into {@code this} from {@code other} fetch command
     * @return {@code this} instance
     */
    NFetch copyFrom(NFetch other);

    ///////////////////////
    // SHARED
    ///////////////////////

    /**
     * return repository filter
     *
     * @return repository filter
     */
    @NGetter
    NRepositoryFilter repositoryFilter();

    /**
     * define repository filter.
     *
     * @param filter repository filter
     * @return {@code this} instance
     */
    @NSetter
    NFetch repositoryFilter(NRepositoryFilter filter);

    /**
     * Adds the specified repository filter.
     *
     * @param filter filter
     * @return add repository filter result
     */
    NFetch addRepositoryFilter(NRepositoryFilter filter);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NFetch configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NFetch run();

    /**
     * return dependency filter
     *
     * @return dependency filter
     */
    @NGetter
    NDependencyFilter dependencyFilter();

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    @NSetter
    NFetch dependencyFilter(NDependencyFilter filter);

    /**
     * Adds the specified dependency filter.
     *
     * @param filter filter
     * @return add dependency filter result
     */
    NFetch addDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NFetch dependencyFilter(String filter);

    /**
     * Fail fast.
     *
     * @param failFast fail fast
     * @return fail fast result
     */
    @NSetter
    NFetch failFast(boolean failFast);

    /**
     * Checks if is ignore current environment.
     *
     * @return is ignore current environment result
     */
    @NGetter
    boolean isIgnoreCurrentEnvironment();

    /**
     * Ignore current environment.
     *
     * @param ignoreCurrentEnvironment ignore current environment
     * @return ignore current environment result
     */
    @NSetter
    NFetch ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment);

    /**
     * Returns target environment for condition evaluation
     *
     * @return target environment
     * @since 0.8.4
     */
    @NGetter
    net.thevpc.nuts.platform.NEnv targetEnv();

    /**
     * Sets target environment for condition evaluation
     *
     * @param targetEnv target environment
     * @return {@code this} instance
     * @since 0.8.4
     */
    @NSetter
    NFetch targetEnv(net.thevpc.nuts.platform.NEnv targetEnv);

    /**
     * Sets target environment from connection string
     *
     * @param connectionString connection string
     * @return {@code this} instance
     * @since 0.8.4
     */
    default NFetch targetEnv(net.thevpc.nuts.net.NConnectionString connectionString) {
        return targetEnv(connectionString == null ? null : net.thevpc.nuts.platform.NEnv.of(connectionString));
    }

    /**
     * Sets target environment from connection string
     *
     * @param connectionString connection string
     * @return {@code this} instance
     * @since 0.8.4
     */
    default NFetch targetEnv(String connectionString) {
        return targetEnv(connectionString == null ? null : net.thevpc.nuts.platform.NEnv.of(connectionString));
    }
}
