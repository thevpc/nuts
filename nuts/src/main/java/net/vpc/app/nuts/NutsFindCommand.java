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
public interface NutsFindCommand extends NutsWorkspaceCommand{

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFindCommand clearIds();

    NutsFindCommand mainAndDependencies();

    NutsFindCommand mainOnly();

    NutsFindCommand addId(String id);

    NutsFindCommand addId(NutsId id);

    NutsFindCommand addIds(String... ids);

    NutsFindCommand addIds(NutsId... ids);

    NutsFindCommand removeId(String id);

    NutsFindCommand removeId(NutsId id);

    NutsFindCommand id(String id);

    NutsFindCommand id(NutsId id);

    NutsFindCommand ids(String... ids);

    NutsFindCommand ids(NutsId... ids);

    NutsFindCommand script(String value);

    NutsFindCommand addScript(String value);

    NutsFindCommand removeScript(String value);

    NutsFindCommand scripts(Collection<String> value);

    NutsFindCommand addScripts(Collection<String> value);

    NutsFindCommand scripts(String... value);

    NutsFindCommand addScripts(String... value);

    NutsFindCommand clearScripts();

    String[] getScripts();

    NutsFindCommand clearArchs();

    NutsFindCommand addArch(String value);

    NutsFindCommand removeArch(String value);

    NutsFindCommand addArchs(Collection<String> value);

    NutsFindCommand addArchs(String... value);

    NutsFindCommand arch(String value);

    NutsFindCommand archs(Collection<String> value);

    NutsFindCommand archs(String... value);

    NutsFindCommand packaging(String value);

    NutsFindCommand packagings(Collection<String> value);

    NutsFindCommand packagings(String... value);

    NutsFindCommand clearPackagings();

    NutsFindCommand addPackagings(Collection<String> value);

    NutsFindCommand addPackagings(String... value);

    NutsFindCommand addPackaging(String value);

    NutsFindCommand removePackaging(String value);

    NutsFindCommand clearRepositories();

    NutsFindCommand repositories(Collection<String> value);

    NutsFindCommand addRepositories(Collection<String> value);

    NutsFindCommand addRepositories(String... value);

    NutsFindCommand addRepository(String value);

    NutsFindCommand removeRepository(String value);

    NutsFindCommand repository(String value);

    NutsFindCommand repositories(String... value);

    /**
     * <code>setSort(true)</code>
     *
     * @return
     */
    NutsFindCommand sort();

    NutsFindCommand sort(boolean sort);

    NutsFindCommand setSort(boolean sort);

    /**
     * setIncludeAllVersions(false)
     *
     * @return
     */
    NutsFindCommand allVersions();

    /**
     * setIncludeAllVersions(true)
     *
     * @return
     */
    NutsFindCommand latestVersions();
    
    NutsFindCommand setLatestVersions(boolean enable);

    NutsFindCommand allVersions(boolean allVersions);

    NutsFindCommand setAllVersions(boolean allVersions);

    NutsFindCommand setDependencyFilter(NutsDependencyFilter filter);

    NutsFindCommand setDependencyFilter(String filter);

    NutsFindCommand setRepositoryFilter(NutsRepositoryFilter filter);

    NutsFindCommand setRepository(String filter);

    NutsFindCommand setDescriptorFilter(NutsDescriptorFilter filter);

    NutsFindCommand setDescriptorFilter(String filter);

    NutsFindCommand setIdFilter(NutsIdFilter filter);

    NutsFindCommand setIdFilter(String filter);

    NutsFindCommand dependenciesOnly();

    NutsFindCommand lenient();

    NutsFindCommand lenient(boolean lenient);

    NutsFindCommand setLenient(boolean lenient);

    NutsFindCommand sort(Comparator<NutsId> comparator);

    NutsFindCommand duplicateVersions();

    NutsFindCommand duplicateVersions(boolean includeDuplicateVersions);

    NutsFindCommand setDuplicateVersions(boolean includeDuplicateVersions);

    NutsFindCommand copyFrom(NutsFindCommand other);

    NutsFindCommand copyFrom(NutsFetchCommand other);

    NutsFindCommand copy();

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
    boolean isLenient();

    Comparator<NutsId> getSortIdComparator();

    boolean isDuplicatedVersions();

    boolean isIncludeMain();

    boolean isAllVersions();

    NutsFetchCommand toFetch();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsFindResult<NutsId> getResultIds();

    NutsFindResult<NutsDefinition> getResultDefinitions();

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
    NutsFindCommand fetchStratery(NutsFetchStrategy mode);

    NutsFindCommand setFetchStratery(NutsFetchStrategy mode);

    NutsFindCommand setTransitive(boolean transitive);

    NutsFindCommand transitive(boolean transitive);

    NutsFindCommand transitive();

    NutsFindCommand setTrace(boolean trace);

    NutsFindCommand trace(boolean trace);

    NutsFindCommand trace();

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
    NutsFindCommand remote();

    NutsFindCommand local();

    /**
     * installed and local
     *
     * @return
     */
    NutsFindCommand offline();

    /**
     * installed, local and remote
     *
     * @return
     */
    NutsFindCommand online();

    /**
     * local and remote (regardless of installed or not)
     *
     * @return
     */
    NutsFindCommand wired();

    /**
     * local and remote
     *
     * @return
     */
    NutsFindCommand installed();

    NutsFindCommand anyWhere();

    NutsFindCommand session(NutsSession session);

    NutsFindCommand setSession(NutsSession session);

    NutsFindCommand clearScopes();

    NutsFindCommand scope(NutsDependencyScope scope);

    NutsFindCommand addScope(NutsDependencyScope scope);

    NutsFindCommand scopes(Collection<NutsDependencyScope> scope);

    NutsFindCommand addScopes(Collection<NutsDependencyScope> scope);

    NutsFindCommand scopes(NutsDependencyScope... scope);

    NutsFindCommand addScopes(NutsDependencyScope... scope);

    NutsFindCommand removeScope(NutsDependencyScope scope);

    NutsFindCommand acceptOptional();

    NutsFindCommand acceptOptional(Boolean acceptOptional);

    NutsFindCommand setAcceptOptional(Boolean acceptOptional);

    NutsFindCommand includeOptional();

    NutsFindCommand includeOptional(boolean includeOptional);

    NutsFindCommand setIncludeOptional(boolean includeOptional);

    NutsFindCommand setIndexed(Boolean indexEnabled);

    NutsFindCommand indexed();

    NutsFindCommand indexed(boolean enable);

    NutsFindCommand includeDependencies();

    NutsFindCommand includeDependencies(boolean include);

    NutsFindCommand setIncludeDependencies(boolean includeDependencies);

    NutsFindCommand setEffective(boolean effective);

    NutsFindCommand effective(boolean effective);

    NutsFindCommand effective();

    NutsFindCommand cached();

    NutsFindCommand cached(boolean cached);

    NutsFindCommand setCached(boolean cached);

    NutsFindCommand includeContent();

    NutsFindCommand includeContent(boolean includeContent);

    NutsFindCommand setIncludeContent(boolean includeContent);

    NutsFindCommand includeInstallInformation();

    NutsFindCommand includeInstallInformation(boolean includeInstallInformation);

    NutsFindCommand setIncludeInstallInformation(boolean includeInstallInformation);

    NutsFindCommand setLocation(Path fileOrFolder);

    NutsFindCommand location(Path fileOrFolder);

    NutsFindCommand setDefaultLocation();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    Path getLocation();

    NutsFetchStrategy getFetchStrategy();

    Boolean getIndexed();

    boolean isIndexed();

    Set<NutsDependencyScope> getScope();

    Boolean getAcceptOptional();

    NutsSession getSession();

    boolean isIncludeContent();

    boolean isIncludeInstallInformation();

    boolean isEffective();

    boolean isIncludeDependencies();

    boolean isTransitive();

    boolean isTrace();

    boolean isCached();

    NutsFindCommand outputFormat(NutsOutputFormat outputFormat);

    NutsFindCommand setOutputFormat(NutsOutputFormat outputFormat);

    NutsOutputFormat getOutputFormat();

    NutsFindCommand parseOptions(String... args);

    NutsFindCommand setTraceFormat(NutsTraceFormat traceFormat);

    NutsFindCommand traceFormat(NutsTraceFormat traceFormat);

    NutsTraceFormat getTraceFormat();

    NutsFindCommand run();
}
