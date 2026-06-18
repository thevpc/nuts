/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.reflect.NClassLoader;
import net.thevpc.nuts.util.*;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

/**
 * Search command class helps searching multiple artifacts with all of their
 * files.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NSearch extends NWorkspaceCmd {

    static NSearch of() {
        return NExtensions.of(NSearch.class);
    }

    static NSearch of(String... ids) {
        return NExtensions.of(NSearch.class).addIds(ids);
    }

    static NSearch of(NId... ids) {
        return NExtensions.of(NSearch.class).addIds(ids);
    }

    @NGetter
    NOptional<NFetchStrategy> fetchStrategy();

    @NGetter
    NOptional<Boolean> transitive();

    @NSetter
    NSearch fetchStrategy(NFetchStrategy fetchStrategy);

    @NSetter
    NSearch transitive(Boolean transitive);

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
    NSearch expireTime(Instant value);

    /**
     * reset ids to search for
     *
     * @return {@code this} instance
     */
    NSearch clearIds();

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch addId(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch addId(NId id);

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NSearch addIds(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NSearch addIds(NId... ids);


    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NSearch addIds(List<NId> ids);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch removeId(String id);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch removeId(NId id);

    /**
     * return true when runtime id is included in search
     *
     * @return true when runtime id is included in search
     */
    boolean isRuntime();

    /**
     * add runtime id to search
     *
     * @param enable when true include runtime id in search
     * @return {@code this} instance
     */
    @NSetter
    NSearch runtime(boolean enable);

    /**
     * companion filter
     *
     * @return companion filter
     */
    boolean isCompanion();

    /**
     * set companions filter. if true companions only are retrieved.
     *
     * @param enable companions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    @NSetter
    NSearch companion(boolean enable);

    /**
     * extension filter
     *
     * @return extension filter
     */
    boolean isExtension();

    /**
     * set extensions filter. if true extensions only are retrieved.
     *
     * @param enable extensions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    @NSetter
    NSearch extension(boolean enable);

    /**
     * app filter
     *
     * @return app filter
     */
    boolean isExecutable();

    /**
     * set app filter. if true non lib (app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch executable(boolean enable);

    /**
     * nuts app filter
     *
     * @return nuts app filter
     */
    boolean isNutsApplication();

    /**
     * set nuts app filter. if true Nuts app (implementing NApplication) only
     * are retrieved.
     *
     * @param enable ap filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch nutsApplication(boolean enable);


    /**
     * nuts app filter
     *
     * @return nuts app filter
     */
    boolean isPlatformApplication();

    /**
     * set nuts app filter. if true Nuts app (implementing NApplication) only
     * are retrieved.
     *
     * @param enable ap filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch platformApplication(boolean enable);

    /**
     * lib filter
     *
     * @return lib filter
     */
    boolean isLib();

    /**
     * set lib filter. if true lib (non app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch setLib(boolean enable);

    /**
     * sort results. Comparator should handle types of the result.
     *
     * @param comparator result comparator
     * @return {@code this}
     */
    NSearch sort(Comparator<?> comparator);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NSearch copyFrom(NSearch other);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NSearch copyFrom(NFetch other);

    /**
     * create new instance copy of this
     *
     * @return new instance
     */
    NSearch copy();

    /**
     * return ids to search for
     *
     * @return ids to search for
     */
    @NGetter
    List<NId> ids();

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NSearch ids(String... ids);

    @NSetter
    NSearch ids(List<NId> ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NSearch ids(NId... ids);

    /**
     * return true if sort flag is armed.
     *
     * @return true if sort flag is armed.
     */
    @NGetter
    boolean isSorted();

    /**
     * sort result
     *
     * @param sort enable sort
     * @return {@code this} instance
     */
    @NSetter
    NSearch sorted(boolean sort);

    /**
     * return dependency filter
     *
     * @return dependency filter
     */
    @NGetter
    NDependencyFilter dependencyFilter();

    /**
     * define dependency filter. applicable when using
     * {@link #inlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch dependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter by AND. applicable when using
     * {@link #inlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NSearch addDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter. applicable when using
     * {@link #inlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch dependencyFilter(String filter);

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
    NSearch repositoryFilter(NRepositoryFilter filter);

    NSearch addRepositoryFilter(NRepositoryFilter filter);

    /**
     * return descriptor filter
     *
     * @return descriptor filter
     */
    @NGetter
    NDefinitionFilter definitionFilter();

    /**
     * define descriptor filter.
     *
     * @param filter descriptor filter
     * @return {@code this} instance
     */
    NSearch definitionFilter(NDefinitionFilter filter);

    NSearch addDefinitionFilter(NDefinitionFilter filter);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    /**
     * when true, NArtifactNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    @NGetter
    boolean isFailFast();

    /**
     * result comparator
     *
     * @return result comparator
     */
    @NGetter
    NComparator<?> comparator();

    /**
     * true if duplicates are skipped
     *
     * @return true if duplicates are skipped
     */
    @NGetter
    boolean isDistinct();

    /**
     * skip duplicates
     *
     * @param distinct skip duplicates
     * @return {@code this}
     */
    @NSetter
    NSearch distinct(boolean distinct);

    /**
     * target api version
     *
     * @return target api version
     */
    @NGetter
    NVersion targetApiVersion();

    /**
     * set target api version
     *
     * @param targetApiVersion new value
     * @return target api version
     */
    @NSetter
    NSearch targetApiVersion(NVersion targetApiVersion);

    /**
     * true if base package flag is armed.
     *
     * @return true if base package flag is armed.
     */
    @NGetter
    boolean isBasePackage();

    /**
     * include base package when searching for inlined dependencies
     *
     * @param includeBasePackage include Base Package
     * @return {@code this} instance
     */
    @NSetter
    NSearch basePackage(boolean includeBasePackage);

    /**
     * true if search must return only latest versions for each artifact id
     *
     * @return true if search must return only latest versions for each artifact
     * id
     */
    @NGetter
    boolean isLatest();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////

    /**
     * if true search must return only latest versions for each artifact id
     *
     * @param enable enable latest artifact id filter
     * @return {@code this} instance
     */
    @NSetter
    NSearch latest(boolean enable);

    /**
     * create fetch command initialized with this instance options.
     *
     * @return fetch command
     */
    NFetch toFetch();

    /**
     * execute query and return result as ids
     *
     * @return result as ids
     */
    NStream<NId> getResultIds();

    /**
     * execute query and return result as dependencies
     *
     * @return result as dependencies
     */
    NStream<NDependencies> getResultDependencies();

    /**
     * execute query and return result as inlined dependencies
     *
     * @return result as dependencies
     */
    NStream<NDependency> getResultInlineDependencies();

    /**
     * execute query and return result as definitions
     *
     * @return result as definitions
     */
    NStream<NDefinition> getResultDefinitions();

    NStream<NDescriptor> getResultDescriptors();

    /**
     * execute query and return result as class loader
     *
     * @return result as class loader
     */
    NClassLoader getResultClassLoader();

    /**
     * execute query and return result as class loader
     *
     * @param parent parent class loader
     * @return result as class loader
     */
    NClassLoader getResultClassLoader(ClassLoader parent);

    NClassLoader getResultIntoClassLoader(NClassLoader classLoader);

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    /**
     * execute query and return result as nuts path string
     *
     * @return result as nuts path string
     */
    String getResultNutsPath();

    /**
     * execute query and return result as class path string
     *
     * @return result as class path string
     */
    String getResultClassPath();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

//    /**
//     * true if descriptor is resolved against its effective value
//     *
//     * @return true if descriptor is resolved against its effective value
//     */
//    boolean isEffective();
//
//    /**
//     * enable/disable effective descriptor evaluation
//     *
//     * @param enable if true evaluation is enabled.
//     * @return {@code this} instance
//     */
//    NSearchCmd setEffective(boolean enable);

    /**
     * true if dependencies are inlined
     *
     * @return true if dependencies are inlined
     */
    boolean isInlineDependencies();

    /**
     * enable/disable inlined dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    @NSetter
    NSearch inlineDependencies(boolean enable);

    @NSetter
    NSearch failFast(boolean failFast);

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
    NSearch configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NSearch run();

    /**
     * return result as content paths
     *
     * @return result as content paths
     */
    NStream<String> getResultPaths();

    /**
     * return result as content path names
     *
     * @return result as content path names
     */
    NStream<String> getResultPathNames();

    /**
     * execute query and return install dates
     *
     * @return query result
     */
    NStream<Instant> getResultInstallDates();

    /**
     * execute query and return install users
     *
     * @return query result
     */
    NStream<String> getResultInstallUsers();

    /**
     * execute query and return install folders
     *
     * @return query result
     */
    NStream<NPath> getResultInstallFolders();

    /**
     * execute query and return store location path
     *
     * @param location location type to return
     * @return query result
     */
    NStream<NPath> getResultStoreLocations(NStoreType location);

    /**
     * execute query and return the selected columns. Supported columns are :
     * <ul>
     * <li>all</li>
     * <li>long</li>
     * <li>status</li>
     * <li>install-date</li>
     * <li>install-user</li>
     * <li>install-folder</li>
     * <li>repository</li>
     * <li>repository-id</li>
     * <li>id</li>
     * <li>name</li>
     * <li>arch</li>
     * <li>packaging</li>
     * <li>platform</li>
     * <li>os</li>
     * <li>osDist</li>
     * <li>exec-entry</li>
     * <li>file-name</li>
     * <li>file</li>
     * <li>var-location</li>
     * <li>temp-folder</li>
     * <li>config-folder</li>
     * <li>lib-folder</li>
     * <li>log-folder</li>
     * <li>cache-folder</li>
     * <li>apps-folder</li>
     * </ul>
     *
     * @param columns columns to return
     * @return query result
     */
    NStream<String[]> getResultStrings(String[] columns);

    /**
     * return result as artifact names
     *
     * @return result as artifact names
     */
    NStream<String> getResultNames();

    /**
     * return result as operating system names
     *
     * @return result as operating system names
     */
    NStream<String> getResultOs();

    /**
     * return result as execution entries
     *
     * @return result as execution entries
     */
    NStream<NExecutionEntry> getResultExecutionEntries();

    /**
     * return result as osDist names
     *
     * @return result as osDist names
     */
    NStream<String> getResultOsDist();

    /**
     * return result as packaging
     *
     * @return result as packaging
     */
    NStream<String> getResultPackaging();

    /**
     * return result as platforms
     *
     * @return result as platforms
     */
    NStream<String> getResultPlatform();

    /**
     * return result as profiles
     *
     * @return result as profiles
     */
    NStream<String> getResultProfile();

    /**
     * return result as desktop environments
     *
     * @return result as desktop environments
     * @since 0.8.3
     */
    NStream<String> getResultDesktopEnvironment();

    /**
     * return result as archs
     *
     * @return result as archs
     */
    NStream<String> getResultArch();

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch id(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearch id(NId id);

    NElement getResultQueryPlan();

    boolean isIgnoreCurrentEnvironment();

    @NSetter
    NSearch ignoreCurrentEnvironment(boolean ignoreCurrentEnvironment);
}
