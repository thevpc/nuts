/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NutsCommandLineConfigurable;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.util.NutsComparator;
import net.thevpc.nuts.util.NutsStream;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
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
public interface NutsSearchCommand extends NutsWorkspaceCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////

    /**
     * reset ids to search for
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearIds();

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand addId(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand addId(NutsId id);

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NutsSearchCommand addIds(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NutsSearchCommand addIds(NutsId... ids);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand removeId(String id);

    /**
     * remove id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand removeId(NutsId id);

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
    NutsSearchCommand setRuntime(boolean enable);

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
    NutsSearchCommand setCompanion(boolean enable);

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
    NutsSearchCommand setExtension(boolean enable);

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
    NutsSearchCommand setExec(boolean enable);

    /**
     * nuts app filter
     *
     * @return nuts app filter
     */
    boolean isApplication();

    /**
     * set nuts app filter. if true nuts app (implementing NutsApplication) only
     * are retrieved.
     *
     * @param enable ap filter
     * @return {@code this} instance
     */
    NutsSearchCommand setApplication(boolean enable);

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
    NutsSearchCommand setLib(boolean enable);

    /**
     * add javascript filter.
     *
     * @param value javascript filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScript(String value);

    /**
     * remove javascript filter.
     *
     * @param value javascript filter
     * @return {@code this} instance
     */
    NutsSearchCommand removeScript(String value);

    /**
     * add javascript filter.
     *
     * @param value javascript filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScripts(Collection<String> value);

    /**
     * add javascript filter.
     *
     * @param value javascript filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScripts(String... value);

    /**
     * remove all javascript filters
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearScripts();

    /**
     * return javascript filters
     *
     * @return javascript filters
     */
    List<String> getScripts();

    /**
     * reset searched for archs
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearArch();

    /**
     * define locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param values ids
     * @return {@code this} instance
     */
    NutsSearchCommand addLockedIds(String... values);

    /**
     * define locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param values ids
     * @return {@code this} instance
     */
    NutsSearchCommand addLockedIds(NutsId... values);

    NutsSearchCommand addLockedIds(List<NutsId> values);

    /**
     * reset locked ids
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearLockedIds();

    /**
     * add arch to search
     *
     * @param value arch to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addArch(String value);

    /**
     * remove arch to search
     *
     * @param value arch to remove
     * @return {@code this} instance
     */
    NutsSearchCommand removeArch(String value);

    /**
     * add archs to search
     *
     * @param values arch to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addArch(Collection<String> values);

    /**
     * add arch to search
     *
     * @param values arch to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addArch(String... values);

    /**
     * reset packaging to search
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearPackaging();

    /**
     * add packagings to search
     *
     * @param values packagings to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addPackaging(Collection<String> values);

    /**
     * add packagings to search
     *
     * @param values packagings to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addPackaging(String... values);

    /**
     * add packaging to search
     *
     * @param value packaging to search for
     * @return {@code this} instance
     */
    NutsSearchCommand addPackaging(String value);

    /**
     * remove packaging from search
     *
     * @param value packaging to remove
     * @return {@code this} instance
     */
    NutsSearchCommand removePackaging(String value);

    /**
     * add locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsSearchCommand addLockedId(NutsId id);

    /**
     * remove locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param id id to unlock
     * @return {@code this} instance
     */
    NutsSearchCommand removeLockedId(NutsId id);

    /**
     * remove locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param id id to unlock
     * @return {@code this} instance
     */
    NutsSearchCommand removeLockedId(String id);

    /**
     * add locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @param id id to lock
     * @return {@code this} instance
     */
    NutsSearchCommand addLockedId(String id);

    /**
     * return locked ids to prevent them to be updated or the force other ids to
     * use them (the installed version).
     *
     * @return locked ids
     */
    List<NutsId> getLockedIds();

    /**
     * define repository filter.
     *
     * @param filter repository filter
     * @return {@code this} instance
     */
    NutsSearchCommand setRepositoryFilter(String filter);

    /**
     * sort results. Comparator should handle types of the result.
     *
     * @param comparator result comparator
     * @return {@code this}
     */
    NutsSearchCommand sort(NutsComparator comparator);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NutsSearchCommand setAll(NutsSearchCommand other);

    /**
     * copy content from given {@code other}
     *
     * @param other other instance
     * @return {@code this}
     */
    NutsSearchCommand setAll(NutsFetchCommand other);

    /**
     * create new instance copy of this
     *
     * @return new instance
     */
    NutsSearchCommand copy();

    /**
     * return ids to search for
     *
     * @return ids to search for
     */
    List<NutsId> getIds();

    /**
     * add ids to search.
     *
     * @param ids id to search
     * @return {@code this} instance
     */
    NutsSearchCommand setIds(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NutsSearchCommand setIds(NutsId... ids);

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
    NutsSearchCommand setSorted(boolean sort);

    /**
     * return dependency filter
     *
     * @return dependency filter
     */
    NutsDependencyFilter getDependencyFilter();

    /**
     * define dependency filter. applicable when using
     * {@link #setInlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NutsSearchCommand setDependencyFilter(NutsDependencyFilter filter);

    /**
     * define dependency filter. applicable when using
     * {@link #setInlineDependencies(boolean)}
     *
     * @param filter dependency filter
     * @return {@code this} instance
     */
    NutsSearchCommand setDependencyFilter(String filter);

    /**
     * return repository filter
     *
     * @return repository filter
     */
    NutsRepositoryFilter getRepositoryFilter();

    /**
     * define repository filter.
     *
     * @param filter repository filter
     * @return {@code this} instance
     */
    NutsSearchCommand setRepositoryFilter(NutsRepositoryFilter filter);

    NutsSearchCommand addRepositoryFilter(NutsRepositoryFilter filter);

    /**
     * return descriptor filter
     *
     * @return descriptor filter
     */
    NutsDescriptorFilter getDescriptorFilter();

    /**
     * define descriptor filter.
     *
     * @param filter descriptor filter
     * @return {@code this} instance
     */
    NutsSearchCommand setDescriptorFilter(NutsDescriptorFilter filter);

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////

    /**
     * define descriptor filter.
     *
     * @param filter descriptor filter
     * @return {@code this} instance
     */
    NutsSearchCommand setDescriptorFilter(String filter);

    /**
     * return id filter
     *
     * @return id filter
     */
    NutsIdFilter getIdFilter();

    /**
     * define id filter.
     *
     * @param filter id filter
     * @return {@code this} instance
     */
    NutsSearchCommand setIdFilter(NutsIdFilter filter);

    /**
     * define id filter.
     *
     * @param filter id filter
     * @return {@code this} instance
     */
    NutsSearchCommand setIdFilter(String filter);

    List<String> getArch();

    List<String> getPackaging();

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
    NutsSearchCommand setFailFast(boolean enable);

    /**
     * result comparator
     *
     * @return result comparator
     */
    NutsComparator getComparator();

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
    NutsSearchCommand setDistinct(boolean distinct);

    /**
     * target api version
     *
     * @return target api version
     */
    NutsVersion getTargetApiVersion();

    /**
     * set target api version
     *
     * @param targetApiVersion new value
     * @return target api version
     */
    NutsSearchCommand setTargetApiVersion(NutsVersion targetApiVersion);

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
    NutsSearchCommand setBasePackage(boolean includeBasePackage);

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
    NutsSearchCommand setLatest(boolean enable);

    /**
     * create fetch command initialized with this instance options.
     *
     * @return fetch command
     */
    NutsFetchCommand toFetch();

    /**
     * execute query and return result as ids
     *
     * @return result as ids
     */
    NutsStream<NutsId> getResultIds();

    /**
     * execute query and return result as dependencies
     *
     * @return result as dependencies
     */
    NutsStream<NutsDependencies> getResultDependencies();

    /**
     * execute query and return result as inlined dependencies
     *
     * @return result as dependencies
     */
    NutsStream<NutsDependency> getResultInlineDependencies();

    /**
     * execute query and return result as definitions
     *
     * @return result as definitions
     */
    NutsStream<NutsDefinition> getResultDefinitions();

    NutsStream<NutsDescriptor> getResultDescriptors();

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
     * remove all dependency scope filters.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearScopes();

    /**
     * add dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScopes(NutsDependencyScope... scope);

    /**
     * add dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand removeScope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScope(NutsDependencyScopePattern scope);

    /**
     * add dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScopes(NutsDependencyScopePattern... scope);

    /**
     * remove dependency scope filter. Only relevant with
     * {@link #setDependencies(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand removeScope(NutsDependencyScopePattern scope);

    /**
     * unset location to store to fetched id and fall back to default location.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand setDefaultLocation();

    /**
     * get locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @return location path
     */
    Path getLocation();

    /**
     * set locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @param fileOrFolder path to store to
     * @return {@code this} instance
     */
    NutsSearchCommand setLocation(Path fileOrFolder);

    /**
     * scope filter filter. applicable with
     * {@link #setInlineDependencies(boolean)}
     *
     * @return optional filter
     */
    Set<NutsDependencyScope> getScope();

    /**
     * optional filter. When non null will filter dependencies from
     * {@link #setInlineDependencies(boolean)}
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
    NutsSearchCommand setOptional(Boolean enable);

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
    NutsSearchCommand setContent(boolean enable);

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
    NutsSearchCommand setEffective(boolean enable);

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
    NutsSearchCommand setInlineDependencies(boolean enable);

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
    NutsSearchCommand setDependencies(boolean enable);

    //
    // NutsWorkspaceCommand overridden methods
    //    

    /**
     * search for default versions status.
     * <ul>
     * <li>return true of only default values are searched for</li>
     * <li>return false of only default values are searched for</li>
     * <li>return null of both default values and non default ones are searched
     * for</li>
     * </ul>
     *
     * @return search for default versions status
     * @since v0.5.5
     */
    Boolean getDefaultVersions();

    /**
     * default version only filter
     *
     * @param enable if non null apply filter
     * @return {@code this} instance
     * @since v0.5.5
     */
    NutsSearchCommand setDefaultVersions(Boolean enable);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand setSession(NutsSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand copySession();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand run();

    /**
     * return result as content paths
     *
     * @return result as content paths
     */
    NutsStream<String> getResultPaths();

    /**
     * return result as content path names
     *
     * @return result as content path names
     */
    NutsStream<String> getResultPathNames();

    /**
     * execute query and return install dates
     *
     * @return query result
     */
    NutsStream<Instant> getResultInstallDates();

    /**
     * execute query and return install users
     *
     * @return query result
     */
    NutsStream<String> getResultInstallUsers();

    /**
     * execute query and return install folders
     *
     * @return query result
     */
    NutsStream<NutsPath> getResultInstallFolders();

    /**
     * execute query and return store location path
     *
     * @param location location type to return
     * @return query result
     */
    NutsStream<NutsPath> getResultStoreLocations(NutsStoreLocation location);

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
    NutsStream<String[]> getResultStrings(String[] columns);

    /**
     * return result as artifact names
     *
     * @return result as artifact names
     */
    NutsStream<String> getResultNames();

    /**
     * return result as operating system names
     *
     * @return result as operating system names
     */
    NutsStream<String> getResultOs();

    /**
     * return result as execution entries
     *
     * @return result as execution entries
     */
    NutsStream<NutsExecutionEntry> getResultExecutionEntries();

    /**
     * return result as osDist names
     *
     * @return result as osDist names
     */
    NutsStream<String> getResultOsDist();

    /**
     * return result as packaging
     *
     * @return result as packaging
     */
    NutsStream<String> getResultPackaging();

    /**
     * return result as platforms
     *
     * @return result as platforms
     */
    NutsStream<String> getResultPlatform();

    /**
     * return result as profiles
     *
     * @return result as profiles
     */
    NutsStream<String> getResultProfile();

    /**
     * return result as desktop environments
     *
     * @return result as desktop environments
     * @since 0.8.3
     */
    NutsStream<String> getResultDesktopEnvironment();

    /**
     * return result as archs
     *
     * @return result as archs
     */
    NutsStream<String> getResultArch();

    /**
     * return the defined installStatus
     *
     * @return {@code this} instance
     */
    NutsInstallStatusFilter getInstallStatus();

    /**
     * search for non packages with the given {@code installStatus}
     *
     * @param installStatus new status
     * @return {@code this} instance
     */
    NutsSearchCommand setInstallStatus(NutsInstallStatusFilter installStatus);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand setId(String id);

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand setId(NutsId id);

    NutsElement getResultQueryPlan();
}
