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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Search command class helps searching multiple artifacts with all of their
 * files.
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NSearchCmd extends NWorkspaceCmd {

    static NSearchCmd of() {
        return NExtensions.of(NSearchCmd.class);
    }

    static NSearchCmd of(String... ids) {
        return NExtensions.of(NSearchCmd.class).addIds(ids);
    }

    static NSearchCmd of(NId... ids) {
        return NExtensions.of(NSearchCmd.class).addIds(ids);
    }

    NOptional<NFetchStrategy> getFetchStrategy();

    NOptional<Boolean> getTransitive();

    NSearchCmd setFetchStrategy(NFetchStrategy fetchStrategy);

    NSearchCmd setTransitive(Boolean transitive);

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
    NSearchCmd setExpireTime(Instant value);


    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    /**
     * reset ids to search for
     *
     * @return {@code this} instance
     */
    NSearchCmd clearIds();

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearchCmd addId(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearchCmd addId(NId id);

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NSearchCmd addIds(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NSearchCmd addIds(NId... ids);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearchCmd removeId(String id);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearchCmd removeId(NId id);

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
    NSearchCmd setRuntime(boolean enable);

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
    NSearchCmd setCompanion(boolean enable);

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
    NSearchCmd setExtension(boolean enable);

    /**
     * app filter
     *
     * @return app filter
     */
    boolean isExec();

    /**
     * set app filter. if true non lib (app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    NSearchCmd setExec(boolean enable);

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
    NSearchCmd setNutsApplication(boolean enable);


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
    NSearchCmd setPlatformApplication(boolean enable);

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
    NSearchCmd setLib(boolean enable);

    /**
     * sort results. Comparator should handle types of the result.
     *
     * @param comparator result comparator
     * @return {@code this}
     */
    NSearchCmd sort(Comparator<?> comparator);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NSearchCmd copyFrom(NSearchCmd other);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NSearchCmd copyFrom(NFetchCmd other);

    /**
     * create new instance copy of this
     *
     * @return new instance
     */
    NSearchCmd copy();

    /**
     * return ids to search for
     *
     * @return ids to search for
     */
    List<NId> getIds();

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NSearchCmd setIds(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NSearchCmd setIds(NId... ids);

    /**
     * return true if sort flag is armed.
     *
     * @return true if sort flag is armed.
     */
    boolean isSorted();

    /**
     * sort result
     *
     * @param sort enable sort
     * @return {@code this} instance
     */
    NSearchCmd setSorted(boolean sort);

    /**
     * return dependency filter
     *
     * @return dependency filter
     */
    NDependencyFilter getDependencyFilter();

    /**
     * define dependency filter. applicable when using
     * {@link #setInlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NSearchCmd setDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter by AND. applicable when using
     * {@link #setInlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NSearchCmd addDependencyFilter(NDependencyFilter filter);

    /**
     * define dependency filter. applicable when using
     * {@link #setInlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NSearchCmd setDependencyFilter(String filter);

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
    NSearchCmd setRepositoryFilter(NRepositoryFilter filter);

    NSearchCmd addRepositoryFilter(NRepositoryFilter filter);

    /**
     * return descriptor filter
     *
     * @return descriptor filter
     */
    NDefinitionFilter getDefinitionFilter();

    /**
     * define descriptor filter.
     *
     * @param filter descriptor filter
     * @return {@code this} instance
     */
    NSearchCmd setDefinitionFilter(NDefinitionFilter filter);

    NSearchCmd addDefinitionFilter(NDefinitionFilter filter);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    /**
     * when true, NNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    boolean isFailFast();

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NNotFoundException.
     *
     * @param enable if true, null replaces NNotFoundException.
     * @return {@code this} instance
     */
    NSearchCmd setFailFast(boolean enable);

    /**
     * result comparator
     *
     * @return result comparator
     */
    NComparator<?> getComparator();

    /**
     * true if duplicates are skipped
     *
     * @return true if duplicates are skipped
     */
    boolean isDistinct();

    /**
     * skip duplicates
     *
     * @param distinct skip duplicates
     * @return {@code this}
     */
    NSearchCmd setDistinct(boolean distinct);

    NSearchCmd distinct();

    /**
     * target api version
     *
     * @return target api version
     */
    NVersion getTargetApiVersion();

    /**
     * set target api version
     *
     * @param targetApiVersion new value
     * @return target api version
     */
    NSearchCmd setTargetApiVersion(NVersion targetApiVersion);

    /**
     * true if base package flag is armed.
     *
     * @return true if base package flag is armed.
     */
    boolean isBasePackage();

    /**
     * include base package when searching for inlined dependencies
     *
     * @param includeBasePackage include Base Package
     * @return {@code this} instance
     */
    NSearchCmd setBasePackage(boolean includeBasePackage);

    /**
     * true if search must return only latest versions for each artifact id
     *
     * @return true if search must return only latest versions for each artifact
     * id
     */
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
    NSearchCmd setLatest(boolean enable);

    NSearchCmd latest();

    /**
     * create fetch command initialized with this instance options.
     *
     * @return fetch command
     */
    NFetchCmd toFetch();

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
    ClassLoader getResultClassLoader();

    /**
     * execute query and return result as class loader
     *
     * @param parent parent class loader
     * @return result as class loader
     */
    ClassLoader getResultClassLoader(ClassLoader parent);

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

    /**
     * true if content is resolved
     *
     * @return true if content is resolved
     */
    boolean isContent();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    /**
     * enable/disable retrieval of content info
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NSearchCmd setContent(boolean enable);

    /**
     * true if descriptor is resolved against its effective value
     *
     * @return true if descriptor is resolved against its effective value
     */
    boolean isEffective();

    /**
     * enable/disable effective descriptor evaluation
     *
     * @param enable if true evaluation is enabled.
     * @return {@code this} instance
     */
    NSearchCmd setEffective(boolean enable);

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
    NSearchCmd setInlineDependencies(boolean enable);

    NSearchCmd failFast();

    /**
     * true if dependencies as list is activated
     *
     * @return true if dependencies as list is activated
     */
    boolean isDependencies();

    /**
     * enable/disable dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NSearchCmd setDependencies(boolean enable);


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
    NSearchCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NSearchCmd run();

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
    NSearchCmd setId(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NSearchCmd setId(NId id);

    NElement getResultQueryPlan();

    boolean isIgnoreCurrentEnvironment();

    NSearchCmd setIgnoreCurrentEnvironment(boolean ignoreCurrentEnvironment);
}
