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
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsSearchCommand extends NutsWorkspaceCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsSearchCommand clearIds();

    NutsSearchCommand addId(String id);

    NutsSearchCommand addId(NutsId id);

    NutsSearchCommand addIds(String... ids);

    NutsSearchCommand addIds(NutsId... ids);

    NutsSearchCommand removeId(String id);

    NutsSearchCommand removeId(NutsId id);

    NutsSearchCommand id(String id);

    NutsSearchCommand lib();

    NutsSearchCommand lib(boolean enable);

    NutsSearchCommand setLib(boolean enable);

    NutsSearchCommand app();

    NutsSearchCommand app(boolean enable);

    NutsSearchCommand setApp(boolean enable);

    NutsSearchCommand nutsApp();

    NutsSearchCommand nutsApp(boolean enable);

    NutsSearchCommand setNutsApp(boolean enable);

    boolean isApp();

    boolean isNutsApp();

    boolean isLib();

    NutsSearchCommand id(NutsId id);

    NutsSearchCommand ids(String... ids);

    NutsSearchCommand ids(NutsId... ids);

    NutsSearchCommand script(String value);

    NutsSearchCommand addScript(String value);

    NutsSearchCommand removeScript(String value);

    NutsSearchCommand scripts(Collection<String> value);

    NutsSearchCommand addScripts(Collection<String> value);

    NutsSearchCommand scripts(String... value);

    NutsSearchCommand addScripts(String... value);

    NutsSearchCommand clearScripts();

    String[] getScripts();

    NutsSearchCommand clearArchs();

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
     * <code>setSort(true)</code>
     *
     * @return
     */
    NutsSearchCommand sort();

    NutsSearchCommand sort(boolean sort);

    NutsSearchCommand setSort(boolean sort);

    /**
     * latest version only
     *
     * @return
     */
    NutsSearchCommand latest();

    NutsSearchCommand latest(boolean enable);

    NutsSearchCommand setLatest(boolean enable);

    NutsSearchCommand dependencyFilter(NutsDependencyFilter filter);

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

    NutsSearchCommand failFast();

    NutsSearchCommand failFast(boolean enable);

    NutsSearchCommand setFailFast(boolean enable);

    NutsSearchCommand sort(Comparator<NutsId> comparator);

    NutsSearchCommand duplicates();

    NutsSearchCommand duplicates(boolean includeDuplicates);

    NutsSearchCommand setDuplicateVersions(boolean includeDuplicateVersions);

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

    boolean isSort();

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
     * @return
     */
    boolean isFailFast();

    Comparator<NutsId> getSortIdComparator();

    boolean isDuplicates();

    boolean isMain();

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
    NutsSearchCommand fetchStratery(NutsFetchStrategy mode);

    NutsSearchCommand setFetchStratery(NutsFetchStrategy mode);

    NutsSearchCommand setTransitive(boolean transitive);

    NutsSearchCommand transitive(boolean transitive);

    NutsSearchCommand transitive();

    /**
     * cache enabled
     *
     * @param cached
     * @return
     */
    /**
     * remote only
     *
     * @return
     */
    NutsSearchCommand remote();

    /**
     * installed and local
     *
     * @return
     */
    NutsSearchCommand offline();

    /**
     * installed, local and remote
     *
     * @return
     */
    NutsSearchCommand online();

    /**
     * local and remote
     *
     * @return
     */
    NutsSearchCommand installed();

    NutsSearchCommand anyWhere();

    NutsSearchCommand clearScopes();

    NutsSearchCommand scope(NutsDependencyScope scope);

    NutsSearchCommand addScope(NutsDependencyScope scope);

    NutsSearchCommand scopes(Collection<NutsDependencyScope> scope);

    NutsSearchCommand addScopes(Collection<NutsDependencyScope> scope);

    NutsSearchCommand scopes(NutsDependencyScope... scope);

    NutsSearchCommand addScopes(NutsDependencyScope... scope);

    NutsSearchCommand removeScope(NutsDependencyScope scope);

    /**
     *
     * @return @since v0.5.5
     */
    NutsSearchCommand defaultVersions();

    /**
     *
     * @param acceptDefaultVersion
     * @since v0.5.5
     * @return
     */
    NutsSearchCommand defaultVersions(Boolean acceptDefaultVersion);

    /**
     *
     * @param acceptDefaultVersion
     * @return
     * @since v0.5.5
     */
    NutsSearchCommand setDefaultVersions(Boolean acceptDefaultVersion);

    NutsSearchCommand optional();

    NutsSearchCommand optional(Boolean acceptOptional);

    NutsSearchCommand setOptional(Boolean acceptOptional);

    NutsSearchCommand setIndexed(Boolean indexEnabled);

    NutsSearchCommand indexed();

    NutsSearchCommand indexed(boolean enable);

    NutsSearchCommand inlineDependencies();

    NutsSearchCommand inlineDependencies(boolean include);

    NutsSearchCommand setInlineDependencies(boolean inlineDependencies);

    NutsSearchCommand dependencies();

    NutsSearchCommand dependencies(boolean include);

    NutsSearchCommand setDependencies(boolean inlineDependencies);

    NutsSearchCommand dependenciesTree();

    NutsSearchCommand dependenciesTree(boolean include);

    NutsSearchCommand setDependenciesTree(boolean inlineDependencies);

    NutsSearchCommand setEffective(boolean effective);

    NutsSearchCommand effective(boolean effective);

    NutsSearchCommand effective();

    NutsSearchCommand cached();

    NutsSearchCommand cached(boolean cached);

    NutsSearchCommand setCached(boolean cached);

    NutsSearchCommand content();

    NutsSearchCommand content(boolean includeContent);

    NutsSearchCommand setContent(boolean includeContent);

    NutsSearchCommand installInformation();

    NutsSearchCommand installInformation(boolean includeInstallInformation);

    NutsSearchCommand setInstallInformation(boolean includeInstallInformation);

    NutsSearchCommand setLocation(Path fileOrFolder);

    NutsSearchCommand location(Path fileOrFolder);

    NutsSearchCommand setDefaultLocation();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    Path getLocation();

    NutsFetchStrategy getFetchStrategy();

    Boolean getIndexed();

    boolean isIndexed();

    Set<NutsDependencyScope> getScope();

    Boolean getOptional();

    boolean isContent();

    boolean isInstallInformation();

    boolean isEffective();

    boolean isInlineDependencies();

    boolean isDependencies();

    boolean isDependenciesTree();

    boolean isTransitive();

    boolean isCached();

    /**
     *
     * @since v0.5.5
     * @return
     */
    Boolean getDefaultVersions();

    //
    // NutsWorkspaceCommand overridden methods
    //    
    @Override
    NutsSearchCommand session(NutsSession session);

    @Override
    NutsSearchCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(java.lang.String...)}
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

    NutsSearchResult<String> getResultFiles();

    NutsSearchResult<String> getResultNames();

    NutsSearchResult<String> getResultOses();

    NutsSearchResult<NutsExecutionEntry> getResultExecutionEntries();

    NutsSearchResult<String> getResultOsdists();

    NutsSearchResult<String> getResultPackagings();

    NutsSearchResult<String> getResultPlatforms();

    NutsSearchResult<String> getResultArchs();
}
