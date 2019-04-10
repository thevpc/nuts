package net.vpc.app.nuts;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;

public interface NutsFindCommand {

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

    NutsFindCommand addIds(Collection<String> value);

    NutsFindCommand ids(Collection<String> value);

    NutsFindCommand removeId(String id);

    NutsFindCommand removeId(NutsId id);

    NutsFindCommand id(String id);

    NutsFindCommand id(NutsId id);

    NutsFindCommand ids(String... ids);

    NutsFindCommand ids(NutsId... ids);

    NutsFindCommand addScripts(Collection<String> value);

    NutsFindCommand addScripts(String... value);

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

    NutsFindCommand setRepository(String filter);

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

    boolean isCached();
}
