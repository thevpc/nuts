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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NOptional;

import java.time.Instant;
import java.util.Set;

/**
 * Fetch command class helps fetching/retrieving a artifact with all of its
 * files.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NFetchCmd extends NWorkspaceCmd {

    static NFetchCmd of() {
        return NExtensions.of().createComponent(NFetchCmd.class).get();
    }

    static NFetchCmd of(NId id) {
        return of().setId(id);
    }

    static NFetchCmd of(String id) {
        return of().setId(id);
    }

    static NFetchCmd ofNutsApi() {
        return of().setId(NWorkspace.of().get().getApiId());
    }

    static NFetchCmd ofNutsRuntime() {
        return of().setId(NWorkspace.of().get().getRuntimeId());
    }

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    NOptional<NFetchStrategy> getFetchStrategy();

    NOptional<Boolean> getTransitive();

    NFetchCmd setFetchStrategy(NFetchStrategy fetchStrategy);

    NFetchCmd setTransitive(Boolean transitive);

    /**
     * return expired date/time or zero if not set. Expire time is used to
     * expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    NOptional<Instant> getExpireTime();

    /**
     * set expire instant. Expire time is used to expire any cached file that
     * was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NFetchCmd setExpireTime(Instant value);

    /**
     * when true, NutsNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    boolean isFailFast();

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NutsNotFoundException.
     *
     * @param enable if true, null replaces NutsNotFoundException.
     * @return {@code this} instance
     */
    NFetchCmd setFailFast(boolean enable);

    /**
     * id to fetch
     *
     * @return id to fetch
     */
    NId getId();

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NFetchCmd setId(String id);

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NFetchCmd setId(NId id);

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
    NFetchCmd copy();

    ///////////////////////
    // REDIFNIED
    ///////////////////////

    /**
     * copy into {@code this} from {@code other} fetch command
     *
     * @param other copy into {@code this} from {@code other} fetch command
     * @return {@code this} instance
     */
    NFetchCmd setAll(NFetchCmd other);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd addScope(NDependencyScopePattern scope);

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd addScope(NDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd addScopes(NDependencyScope... scope);

    /**
     * add dependency scope filter. Only relevant with {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd addScopes(NDependencyScopePattern... scope);

    /**
     * remove dependency scope filter.
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd removeScope(NDependencyScope scope);

    /**
     * remove dependency scope filter.
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NFetchCmd removeScope(NDependencyScopePattern scope);

    /**
     * remove all dependency scope filters.
     *
     * @return {@code this} instance
     */
    NFetchCmd clearScopes();

    /**
     * dependencies scope filters
     *
     * @return dependencies scope filters
     */
    Set<NDependencyScope> getScope();

    /**
     * optional filter
     *
     * @return optional filter
     */
    Boolean getOptional();

    /**
     * set option filter. if null filter is removed. if false only non optional
     * will be retrieved. if true, only optional will be retrieved.
     *
     * @param enable option filter
     * @return {@code this} instance
     */
    NFetchCmd setOptional(Boolean enable);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    /**
     * content filter
     *
     * @return content filter
     */
    boolean isContent();

    /**
     * enable/disable retrieval of content info
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NFetchCmd setContent(boolean enable);

    /**
     * effective filter
     *
     * @return effective filter
     */
    boolean isEffective();

    /**
     * enable/disable effective descriptor evaluation
     *
     * @param enable if true evaluation is enabled.
     * @return {@code this} instance
     */
    NFetchCmd setEffective(boolean enable);

    /**
     * if true dependencies list is retrieved
     *
     * @return dependencies list retrieval status
     */
    boolean isDependencies();

    /**
     * enable/disable dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NFetchCmd setDependencies(boolean enable);

    NFetchCmd dependencies();

    /**
     * return repository filter
     *
     * @return repository filter
     */
    NRepositoryFilter getRepositoryFilter();

    /**
     * define repository filter.
     *
     * @param filter repository filter
     * @return {@code this} instance
     */
    NFetchCmd setRepositoryFilter(NRepositoryFilter filter);

    NFetchCmd addRepositoryFilter(NRepositoryFilter filter);

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
    NFetchCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NFetchCmd run();

//    /**
//     * search for installed packages
//     * @return {@code this} instance
//     */
//    NutsFetchCommand installed();
//
//    /**
//     * search for non installed packages
//     * @return {@code this} instance
//     */
//    NutsFetchCommand notInstalled();
//
//    /**
//     * search for installed/non installed packages
//     * @param value new value
//     * @return {@code this} instance
//     */
//    NutsFetchCommand installed(Boolean value);
//
//    /**
//     * search for installed/non installed packages
//     * @param value new value
//     * @return {@code this} instance
//     */
//    NutsFetchCommand setInstalled(Boolean value);
//
//    /**
//     * return installed/non installed packages flag
//     * @return {@code this} instance
//     */
//    Boolean getInstalled();

    /**
     * return dependency filter
     *
     * @return dependency filter
     */
    NDependencyFilter getDependencyFilter();

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NFetchCmd setDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NFetchCmd setDependencyFilter(String filter);

    NFetchCmd failFast();

    NFetchCmd content();

    NFetchCmd effective();
}