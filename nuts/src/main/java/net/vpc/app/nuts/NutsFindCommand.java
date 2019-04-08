package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public interface NutsFindCommand {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFindCommand setId(String id);

    NutsFindCommand setId(NutsId value);

    NutsFindCommand mainAndDependencies();

    NutsFindCommand mainOnly();

    NutsFindCommand setVersionFilter(NutsVersionFilter filter);

    NutsFindCommand setVersionFilter(String filter);

    NutsFindCommand addIds(String... ids);

    NutsFindCommand addIds(NutsId... ids);

    NutsFindCommand setIds(String... ids);

    NutsFindCommand id(String id);
    
    NutsFindCommand addId(String id);

    NutsFindCommand addId(NutsId id);

    NutsFindCommand addJs(Collection<String> value);

    NutsFindCommand addJs(String... value);

    NutsFindCommand addId(Collection<String> value);

    NutsFindCommand addId(String... value);

    NutsFindCommand addId(NutsId... value);

    NutsFindCommand addArch(Collection<String> value);

    NutsFindCommand addArch(String... value);

    NutsFindCommand addPackaging(Collection<String> value);

    NutsFindCommand addPackaging(String... value);

    NutsFindCommand addRepository(Collection<String> value);

    NutsFindCommand addRepository(String... value);

    /**
     * setSort(true)
     *
     * @return
     */
    NutsFindCommand sort();

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

    NutsFindCommand setIncludeAllVersions(boolean allVersions);

    NutsFindCommand setDependencyFilter(NutsDependencyFilter filter);

    NutsFindCommand setDependencyFilter(String filter);

    NutsFindCommand setRepositoryFilter(NutsRepositoryFilter filter);

    NutsFindCommand setRepositoryFilter(String filter);

    NutsFindCommand setDescriptorFilter(NutsDescriptorFilter filter);

    NutsFindCommand setDescriptorFilter(String filter);

    NutsFindCommand setIdFilter(NutsIdFilter filter);

    NutsFindCommand setIdFilter(String filter);

    NutsFindCommand setIds(Collection<String> ids);

    NutsFindCommand dependenciesOnly();

    NutsFindCommand lenient();

    NutsFindCommand lenient(boolean lenient);

    NutsFindCommand setLenient(boolean lenient);

    NutsFindCommand sort(Comparator<NutsId> comparator);

    NutsFindCommand setIncludeDuplicateVersions(boolean includeDuplicateVersions);

    NutsFindCommand copyFrom(NutsFindCommand other);

    NutsFindCommand copyFrom(NutsFetchCommand other);

    NutsFindCommand copy();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    String[] getIds();

    boolean isSort();

    NutsDependencyFilter getDependencyFilter();

    NutsRepositoryFilter getRepositoryFilter();

    NutsVersionFilter getVersionFilter();

    NutsDescriptorFilter getDescriptorFilter();

    NutsIdFilter getIdFilter();

    String[] getJs();

    String[] getArch();

    String[] getPackaging();

    String[] getRepos();

    /**
     * when true, NutsNotFoundException instances are ignored
     *
     * @return
     */
    boolean isLenient();

    Comparator<NutsId> getSortIdComparator();

    boolean isIncludeDuplicatedVersions();

    boolean isIncludeMain();

    boolean isIncludeAllVersions();

    NutsFetchCommand toFetch();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsFindResult<NutsId> getResultIds();

    NutsFindResult<NutsDefinition> getResultDefinitions();

    String getResultNutsPath();

    String getResultClassPath();

    ///////////////////////
    // SHARED
    ///////////////////////
    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsFindCommand setFetchStratery(NutsFetchStrategy mode);

    NutsFindCommand setTransitive(boolean transitive);

    NutsFindCommand transitive(boolean transitive);

    NutsFindCommand transitive();

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
     * local and remote
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

    NutsFindCommand setScope(NutsDependencyScope scope);

    NutsFindCommand setScope(NutsDependencyScope... scope);

    NutsFindCommand setScope(Collection<NutsDependencyScope> scope);

    NutsFindCommand addScope(NutsDependencyScope scope);

    NutsFindCommand addScope(Collection<NutsDependencyScope> scope);

    NutsFindCommand addScope(NutsDependencyScope... scope);

    NutsFindCommand removeScope(Collection<NutsDependencyScope> scope);

    NutsFindCommand removeScope(NutsDependencyScope scope);

    NutsFindCommand setAcceptOptional(Boolean acceptOptional);

    NutsFindCommand setIncludeOptional(boolean includeOptional);

    NutsFindCommand setIgnoreCache(boolean ignoreCache);

    NutsFindCommand ignoreCache();

    NutsFindCommand setIndexed(Boolean indexEnabled);

    NutsFindCommand indexed();

    NutsFindCommand indexDisabled();

    NutsFindCommand includeDependencies();

    NutsFindCommand includeDependencies(boolean include);

    NutsFindCommand setIncludeDependencies(boolean includeDependencies);

    NutsFindCommand setEffective(boolean effective);

    NutsFindCommand effective(boolean effective);

    NutsFindCommand effective();

    NutsFindCommand setCached(boolean cached);

    NutsFindCommand setIncludeFile(boolean includeFile);

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

    boolean isIgnoreCache();

    boolean isIncludeFile();

    boolean isIncludeInstallInformation();

    boolean isEffective();

    boolean isIncludeDependencies();

    boolean isTransitive();

    boolean isCached();
}
