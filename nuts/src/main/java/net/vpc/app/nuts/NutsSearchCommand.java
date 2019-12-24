/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 * Search command class helps searching multiple artifacts with all of their
 * files.
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsSearchCommand extends NutsWorkspaceCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
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
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand id(String id);

    /**
     * set lib filter. lib (non app) only are retrieved.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand lib();

    /**
     * set lib filter. if true lib (non app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    NutsSearchCommand lib(boolean enable);

    /**
     * set lib filter. if true lib (non app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    NutsSearchCommand setLib(boolean enable);

    /**
     * set extensions filter. extensions only are retrieved.
     *
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand extensions();

    /**
     * set extensions filter. if true extensions only are retrieved.
     *
     * @param enable extensions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand extensions(boolean enable);

    /**
     * set extensions filter. if true extensions only are retrieved.
     *
     * @param enable extensions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand setExtension(boolean enable);


    /**
     * set companions filter. companions only are retrieved.
     *
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand companion();

    /**
     * set companions filter. if true companions only are retrieved.
     *
     * @param enable companions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand companion(boolean enable);

    /**
     * set companions filter. if true companions only are retrieved.
     *
     * @param enable companions filter
     * @return {@code this} instance
     * @since 0.5.7
     */
    NutsSearchCommand setCompanion(boolean enable);

    /**
     * set app filter. non lib (app) only are retrieved.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand exec();

    /**
     * set app filter. if true non lib (app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    NutsSearchCommand exec(boolean enable);

    /**
     * set app filter. if true non lib (app) only are retrieved.
     *
     * @param enable lib filter
     * @return {@code this} instance
     */
    NutsSearchCommand setExec(boolean enable);

    NutsSearchCommand runtime();

    NutsSearchCommand runtime(boolean enable);

    NutsSearchCommand setRuntime(boolean enable);

    /**
     * set nuts app filter. nuts app (implementing NutsApplication) only are
     * retrieved.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand applications();

    /**
     * set nuts app filter. if true nuts app (implementing NutsApplication) only
     * are retrieved.
     *
     * @param enable ap filter
     * @return {@code this} instance
     */
    NutsSearchCommand applications(boolean enable);

    /**
     * set nuts app filter. if true nuts app (implementing NutsApplication) only
     * are retrieved.
     *
     * @param enable ap filter
     * @return {@code this} instance
     */
    NutsSearchCommand setApplication(boolean enable);

    boolean isRuntime();

    /**
     * companion filter
     *
     * @return companion filter
     */
    boolean isCompanion();

    /**
     * extension filter
     *
     * @return extension filter
     */
    boolean isExtension();

    /**
     * app filter
     *
     * @return app filter
     */
    boolean isExec();

    /**
     * nuts app filter
     *
     * @return nuts app filter
     */
    boolean isApplication();

    /**
     * lib filter
     *
     * @return lib filter
     */
    boolean isLib();

    /**
     * add id to search.
     *
     * @param id id to search
     * @return {@code this} instance
     */
    NutsSearchCommand id(NutsId id);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NutsSearchCommand ids(String... ids);

    /**
     * add ids to search.
     *
     * @param ids ids to search
     * @return {@code this} instance
     */
    NutsSearchCommand ids(NutsId... ids);

    /**
     * add javascript filter.
     *
     * @param value javascript filter
     * @return {@code this} instance
     */
    NutsSearchCommand script(String value);

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
    NutsSearchCommand scripts(Collection<String> value);

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
    NutsSearchCommand scripts(String... value);

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
    String[] getScripts();

    NutsSearchCommand clearArchs();

    NutsSearchCommand frozenIds(String... values);

    NutsSearchCommand addFrozenIds(String... values);

    NutsSearchCommand frozenIds(NutsId... values);

    NutsSearchCommand addFrozenIds(NutsId... value);

    NutsSearchCommand clearFrozenIds();

    NutsSearchCommand addArch(String value);

    NutsSearchCommand removeArch(String value);

    NutsSearchCommand addArchs(Collection<String> value);

    NutsSearchCommand addArchs(String... value);

    NutsSearchCommand arch(String value);

    NutsSearchCommand archs(Collection<String> value);

    NutsSearchCommand archs(String... value);

    NutsSearchCommand packaging(String value);

    NutsSearchCommand packagings(Collection<String> value);

    NutsSearchCommand packagings(String... value);

    NutsSearchCommand clearPackagings();

    NutsSearchCommand addPackagings(Collection<String> value);

    NutsSearchCommand addPackagings(String... value);

    NutsSearchCommand addPackaging(String value);

    NutsSearchCommand removePackaging(String value);

    NutsSearchCommand clearRepositories();

    NutsSearchCommand repositories(Collection<String> value);

    NutsSearchCommand addRepositories(Collection<String> value);

    NutsSearchCommand addRepositories(String... value);

    NutsSearchCommand addRepository(String value);

    NutsSearchCommand removeRepository(String value);

    NutsSearchCommand repository(String value);

    NutsSearchCommand repositories(String... value);

    /**
     * equivalent to {@code setSort(true)}
     *
     * @return sort mode
     */
    NutsSearchCommand sort();

    NutsSearchCommand sort(boolean sort);

    NutsSearchCommand setSorted(boolean sort);

    /**
     * search must return only latest versions for each artifact id
     *
     * @return {@code this} instance
     */
    NutsSearchCommand latest();

    /**
     * if true search must return only latest versions for each artifact id
     * @param enable enable latest artifact id filter
     * @return {@code this} instance
     */
    NutsSearchCommand latest(boolean enable);

    /**
     * if true search must return only latest versions for each artifact id
     * @param enable enable latest artifact id filter
     * @return {@code this} instance
     */
    NutsSearchCommand setLatest(boolean enable);

    NutsSearchCommand dependencyFilter(NutsDependencyFilter filter);

    NutsSearchCommand frozenId(NutsId id);

    NutsSearchCommand addFrozenId(NutsId id);

    NutsSearchCommand removeFrozenId(NutsId id);

    NutsSearchCommand frozenId(String id);

    NutsSearchCommand removeFrozenId(String id);

    NutsSearchCommand addFrozenId(String id);

    NutsId[] getFrozenIds();

    NutsSearchCommand setDependencyFilter(NutsDependencyFilter filter);

    NutsSearchCommand dependencyFilter(String filter);

    NutsSearchCommand setDependencyFilter(String filter);

    NutsSearchCommand repositoryFilter(NutsRepositoryFilter filter);

    NutsSearchCommand setRepositoryFilter(NutsRepositoryFilter filter);

    NutsSearchCommand setRepository(String filter);

    NutsSearchCommand descriptorFilter(NutsDescriptorFilter filter);

    NutsSearchCommand setDescriptorFilter(NutsDescriptorFilter filter);

    NutsSearchCommand descriptorFilter(String filter);

    NutsSearchCommand setDescriptorFilter(String filter);

    NutsSearchCommand idFilter(NutsIdFilter filter);

    NutsSearchCommand setIdFilter(NutsIdFilter filter);

    NutsSearchCommand idFilter(String filter);

    NutsSearchCommand setIdFilter(String filter);

    /**
     * set armed (true) fail safe mode. null replaces NutsNotFoundException.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand failFast();

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NutsNotFoundException.
     *
     * @param enable if true, null replaces NutsNotFoundException.
     * @return {@code this} instance
     */
    NutsSearchCommand failFast(boolean enable);

    /**
     * set armed (or disarmed) fail safe mode. if true, null replaces
     * NutsNotFoundException.
     *
     * @param enable if true, null replaces NutsNotFoundException.
     * @return {@code this} instance
     */
    NutsSearchCommand setFailFast(boolean enable);

    NutsSearchCommand sort(Comparator<NutsId> comparator);

//    NutsSearchCommand duplicates();
//
//    NutsSearchCommand duplicates(boolean includeDuplicates);
//
//    NutsSearchCommand setDuplicateVersions(boolean includeDuplicateVersions);

    NutsSearchCommand distinct();

    NutsSearchCommand distinct(boolean distinct);

    NutsSearchCommand setDistinct(boolean distinct);

    NutsSearchCommand copyFrom(NutsSearchCommand other);

    NutsSearchCommand copyFrom(NutsFetchCommand other);

    NutsSearchCommand copy();

    NutsSearchCommand setMain(boolean includeMain);

    NutsSearchCommand main(boolean includeMain);

    NutsSearchCommand main();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    NutsId[] getIds();

    boolean isSorted();

    NutsDependencyFilter getDependencyFilter();

    NutsRepositoryFilter getRepositoryFilter();

    NutsDescriptorFilter getDescriptorFilter();

    NutsIdFilter getIdFilter();

    String[] getArch();

    String[] getPackaging();

    String[] getRepositories();

    /**
     * when true, NutsNotFoundException instances are ignored
     *
     * @return true if armed FailFast mode
     */
    boolean isFailFast();

    Comparator getComparator();

    boolean isDistinct();

    String getTargetApiVersion();

    NutsSearchCommand setTargetApiVersion(String targetApiVersion);

    NutsSearchCommand targetApiVersion(String targetApiVersion);

    boolean isMain();

    /**
     * true if search must return only latest versions for each artifact id
     * @return true if search must return only latest versions for each artifact id
     */
    boolean isLatest();

    NutsFetchCommand toFetch();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsSearchResult<NutsId> getResultIds();

    NutsSearchResult<NutsDefinition> getResultDefinitions();

    ClassLoader getResultClassLoader();

    ClassLoader getResultClassLoader(ClassLoader parent);

    String getResultNutsPath();

    String getResultClassPath();

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    /**
     * set fetch strategy.
     *
     * @param fetchStrategy fetch strategy
     * @return {@code this} instance
     */
    NutsSearchCommand fetchStrategy(NutsFetchStrategy fetchStrategy);

    /**
     * set fetch strategy.
     *
     * @param fetchStrategy fetch strategy
     * @return {@code this} instance
     */
    NutsSearchCommand setFetchStrategy(NutsFetchStrategy fetchStrategy);

    /**
     * set or unset transitive mode
     *
     * @param enable if true, transitive mode is armed
     * @return {@code this} instance
     */
    NutsSearchCommand setTransitive(boolean enable);

    /**
     * set or unset transitive mode
     *
     * @param enable if true, transitive mode is armed
     * @return {@code this} instance
     */
    NutsSearchCommand transitive(boolean enable);

    /**
     * set transitive mode to true
     *
     * @return {@code this} instance
     */
    NutsSearchCommand transitive();

    /**
     * remote only
     *
     * @return {@code this} instance
     */
    NutsSearchCommand remote();

    /**
     * local only (installed or not)
     *
     * @return {@code this} instance
     */
    NutsSearchCommand offline();

    /**
     * local or remote. If local result found will not fetch remote.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand online();

    /**
     * only installed artifacts
     *
     * @return {@code this} instance
     */
    NutsSearchCommand installed();

    /**
     * all artifacts (local and remote). If local result found will any way
     * fetch remote.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand anyWhere();

    /**
     * remove all dependency scope filters.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand clearScopes();

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand scope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand scopes(NutsDependencyScope... scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScopes(NutsDependencyScope... scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand removeScope(NutsDependencyScope scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand scope(NutsDependencyScopePattern scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScope(NutsDependencyScopePattern scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand scopes(NutsDependencyScopePattern... scope);

    /**
     * add dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand addScopes(NutsDependencyScopePattern... scope);

    /**
     * remove dependency scope filter. Only relevant with {@link #dependencies(boolean)
     * } and {@link #dependenciesTree(boolean)}
     *
     * @param scope scope filter
     * @return {@code this} instance
     */
    NutsSearchCommand removeScope(NutsDependencyScopePattern scope);

    /**
     * default version only
     *
     * @return {@code this} instance
     * @since v0.5.5
     */
    NutsSearchCommand defaultVersions();

    /**
     * default version only filter
     *
     * @param enable if non null apply filter
     * @since v0.5.5
     * @return {@code this} instance
     */
    NutsSearchCommand defaultVersions(Boolean enable);

    /**
     * default version only filter
     *
     * @param enable if non null apply filter
     * @since v0.5.5
     * @return {@code this} instance
     */
    NutsSearchCommand setDefaultVersions(Boolean enable);

    /**
     * retrieve optional only
     *
     * @return {@code this} instance
     */
    NutsSearchCommand optional();

    /**
     * set option filter. if null filter is removed. if false only non optional
     * will be retrieved. if true, only optional will be retrieved.
     *
     * @param enable option filter
     * @return {@code this} instance
     */
    NutsSearchCommand optional(Boolean enable);

    /**
     * set option filter. if null filter is removed. if false only non optional
     * will be retrieved. if true, only optional will be retrieved.
     *
     * @param enable option filter
     * @return {@code this} instance
     */
    NutsSearchCommand setOptional(Boolean enable);

    /**
     * set index filter.if null index is removed. if false do not consider
     * index. if true, consider index.
     *
     * @param enable index filter.
     * @return {@code this} instance
     */
    NutsSearchCommand setIndexed(Boolean enable);

    /**
     * set index filter to true.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand indexed();

    /**
     * set index filter.if null index is removed. if false do not consider
     * index. if true, consider index.
     *
     * @param enable index filter.
     * @return {@code this} instance
     */
    NutsSearchCommand indexed(boolean enable);

    /**
     * enable inlined dependencies list retrieval
     *
     * @return {@code this} instance
     */
    NutsSearchCommand inlineDependencies();

    /**
     * enable/disable inlined dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand inlineDependencies(boolean enable);

    /**
     * enable/disable inlined dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setInlineDependencies(boolean enable);

    /**
     * enable dependencies list retrieval
     *
     * @return {@code this} instance
     */
    NutsSearchCommand dependencies();

    /**
     * enable/disable dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand dependencies(boolean enable);

    /**
     * enable/disable dependencies list retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setDependencies(boolean enable);

    /**
     * enable dependencies tree retrieval
     *
     * @return {@code this} instance
     */
    NutsSearchCommand dependenciesTree();

    /**
     * enable/disable dependencies tree retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand dependenciesTree(boolean enable);

    /**
     * enable/disable dependencies tree retrieval
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setDependenciesTree(boolean enable);

    /**
     * enable/disable effective descriptor evaluation
     *
     * @param enable if true evaluation is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setEffective(boolean enable);

    /**
     * enable/disable effective descriptor evaluation
     *
     * @param enable if true evaluation is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand effective(boolean enable);

    /**
     * enable effective descriptor evaluation
     *
     * @return {@code this} instance
     */
    NutsSearchCommand effective();

    /**
     * enable retrieval from cache
     *
     * @return {@code this} instance
     */
    NutsSearchCommand cached();

    /**
     * enable/disable retrieval from cache
     *
     * @param enable if true cache is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand cached(boolean enable);

    /**
     * enable/disable retrieval from cache
     *
     * @param enable if true cache is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setCached(boolean enable);

    /**
     * enable retrieval of content info
     *
     * @return {@code this} instance
     */
    NutsSearchCommand content();

    /**
     * enable/disable retrieval of content info
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand content(boolean enable);

    /**
     * enable/disable retrieval of content info
     *
     * @param enable if true retrieval is enabled.
     * @return {@code this} instance
     */
    NutsSearchCommand setContent(boolean enable);

    /**
     * set locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @param fileOrFolder path to store to
     * @return {@code this} instance
     */
    NutsSearchCommand setLocation(Path fileOrFolder);

    /**
     * set locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @param fileOrFolder path to store to
     * @return {@code this} instance
     */
    NutsSearchCommand location(Path fileOrFolder);

    /**
     * unset location to store to fetched id and fall back to default location.
     *
     * @return {@code this} instance
     */
    NutsSearchCommand setDefaultLocation();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    /**
     * get locating where to fetch the artifact. If the location is a folder, a
     * new name will be generated.
     *
     * @return location path
     */
    Path getLocation();

    NutsFetchStrategy getFetchStrategy();

    boolean isIndexed();

    Set<NutsDependencyScope> getScope();

    Boolean getOptional();

    boolean isContent();

    boolean isEffective();

    boolean isInlineDependencies();

    boolean isDependencies();

    boolean isDependenciesTree();

    boolean isTransitive();

    boolean isCached();

    /**
     * search for default versions status.
     * <ul>
     * <li>return true of only default values are searched for</li>
     * <li>return false of only default values are searched for</li>
     * <li>return null of both default values and non default ones are searched for</li>
     * </ul>
     *
     *
     *
     * @since v0.5.5
     * @return search for default versions status
     */
    Boolean getDefaultVersions();

    //
    // NutsWorkspaceCommand overridden methods
    //    
    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsSearchCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
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
    NutsSearchResult<String> getResultPaths();

    /**
     * return result as content path names
     *
     * @return result as content path names
     */
    NutsSearchResult<String> getResultPathNames();

    NutsSearchResult<Instant> getResultInstallDates();

    NutsSearchResult<String> getResultInstallUsers();

    NutsSearchResult<Path> getResultInstallFolders();

    NutsSearchResult<Path> getResultStoreLocations(NutsStoreLocation location);
    
    NutsSearchResult<String[]> getResultStrings(String[] columns);
    
    /**
     * return result as artifact names
     *
     * @return result as artifact names
     */
    NutsSearchResult<String> getResultNames();

    /**
     * return result as operating system names
     *
     * @return result as operating system names
     */
    NutsSearchResult<String> getResultOses();

    /**
     * return result as execution entries
     *
     * @return result as execution entries
     */
    NutsSearchResult<NutsExecutionEntry> getResultExecutionEntries();

    /**
     * return result as osdist names
     *
     * @return result as osdist names
     */
    NutsSearchResult<String> getResultOsdists();

    /**
     * return result as packagings
     *
     * @return result as packagings
     */
    NutsSearchResult<String> getResultPackagings();

    /**
     * return result as platforms
     *
     * @return result as platforms
     */
    NutsSearchResult<String> getResultPlatforms();

    /**
     * return result as archs
     *
     * @return result as archs
     */
    NutsSearchResult<String> getResultArchs();
}
