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
import net.thevpc.nuts.util.NOptional;

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

    static NFetch of() {
        return NExtensions.of(NFetch.class);
    }

    static NFetch of(NId id) {
        return of().setId(id);
    }

    static NFetch of(String id) {
        return of().setId(id);
    }

    static NFetch ofNutsApi() {
        return of().setId(NWorkspace.of().getApiId());
    }

    static NFetch ofNutsApp() {
        return of().setId(NWorkspace.of().getAppId());
    }

    static NFetch ofNutsRuntime() {
        return of().setId(NWorkspace.of().getRuntimeId());
    }

    ////////////////////////////////////////////////////////
    // Setters

    /// /////////////////////////////////////////////////////

    NOptional<NFetchStrategy> getFetchStrategy();

    NOptional<Boolean> getTransitive();

    NFetch setFetchStrategy(NFetchStrategy fetchStrategy);

    NFetch setTransitive(Boolean transitive);

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
    NFetch setExpireTime(Instant value);

    /**
     * when true, NArtifactNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    boolean isFailFast();

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NArtifactNotFoundException.
     *
     * @param enable if true, null replaces NArtifactNotFoundException.
     * @return {@code this} instance
     */
    NFetch setFailFast(boolean enable);

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
    NFetch setId(String id);

    /**
     * set id to fetch.
     *
     * @param id id to fetch
     * @return {@code this} instance
     */
    NFetch setId(NId id);

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
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////


    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

//    /**
//     * effective filter
//     *
//     * @return effective filter
//     */
//    boolean isEffective();
//
//    /**
//     * enable/disable effective descriptor evaluation
//     *
//     * @param enable if true evaluation is enabled.
//     * @return {@code this} instance
//     */
//    NFetchCmd setEffective(boolean enable);

//    /**
//     * if true dependencies list is retrieved
//     *
//     * @return dependencies list retrieval status
//     */
//    boolean isDependencies();
//
//    /**
//     * enable/disable dependencies list retrieval
//     *
//     * @param enable if true retrieval is enabled.
//     * @return {@code this} instance
//     */
//    NFetchCmd setDependencies(boolean enable);

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
    NFetch setRepositoryFilter(NRepositoryFilter filter);

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
    NDependencyFilter getDependencyFilter();

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NFetch setDependencyFilter(NDependencyFilter filter);

    NFetch addDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter.
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NFetch setDependencyFilter(String filter);

    NFetch failFast();

    boolean isIgnoreCurrentEnvironment();

    NFetch setIgnoreCurrentEnvironment(boolean ignoreCurrentEnvironment);

}
