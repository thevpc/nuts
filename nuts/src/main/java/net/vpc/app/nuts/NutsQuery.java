package net.vpc.app.nuts;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public interface NutsQuery {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    NutsQuery setId(String id);

    NutsQuery setId(NutsId value);

    NutsQuery mainAndDependencies();

    NutsQuery includeDependencies();

    NutsQuery includeDependencies(boolean include);

    NutsQuery setSession(NutsSession session);

    NutsQuery setScope(NutsDependencyScope scope);

    NutsQuery setScope(NutsDependencyScope... scope);

    NutsQuery setScope(Collection<NutsDependencyScope> scope);

    NutsQuery addScope(NutsDependencyScope scope);

    NutsQuery addScope(Collection<NutsDependencyScope> scope);

    NutsQuery addScope(NutsDependencyScope... scope);

    NutsQuery removeScope(Collection<NutsDependencyScope> scope);

    NutsQuery removeScope(NutsDependencyScope scope);

    NutsQuery setAcceptOptional(Boolean acceptOptional);

    NutsQuery setIncludeOptional(boolean includeOptional);

    NutsQuery setIgnoreCache(boolean ignoreCache);

    NutsQuery ignoreCache();

    NutsQuery setIncludeDependencies(boolean includeDependencies);

    NutsQuery setIncludeEffective(boolean includeEffectiveDescriptor);

    NutsQuery setIncludeFile(boolean includeFile);

    NutsQuery setIncludeInstallInformation(boolean includeInstallInformation);

    NutsQuery setInstalledOnly(boolean preferInstalled);

    NutsQuery setPreferInstalled(boolean preferInstalled);

    NutsQuery setVersionFilter(NutsVersionFilter filter);

    NutsQuery setVersionFilter(String filter);

    NutsQuery addIds(String... ids);

    NutsQuery addIds(NutsId... ids);

    NutsQuery setIds(String... ids);

    NutsQuery addId(String id);

    NutsQuery addId(NutsId id);

    NutsQuery addJs(Collection<String> value);

    NutsQuery addJs(String... value);

    NutsQuery addId(Collection<String> value);

    NutsQuery addId(String... value);

    NutsQuery addId(NutsId... value);

    NutsQuery addArch(Collection<String> value);

    NutsQuery addArch(String... value);

    NutsQuery addPackaging(Collection<String> value);

    NutsQuery addPackaging(String... value);

    NutsQuery addRepository(Collection<String> value);

    NutsQuery addRepository(String... value);

    NutsQuery setSort(boolean sort);

    NutsQuery setLatestVersions(boolean latestVersions);

    NutsQuery setDependencyFilter(NutsDependencyFilter filter);

    NutsQuery setDependencyFilter(String filter);

    NutsQuery setRepositoryFilter(NutsRepositoryFilter filter);

    NutsQuery setRepositoryFilter(String filter);

    NutsQuery setDescriptorFilter(NutsDescriptorFilter filter);

    NutsQuery setDescriptorFilter(String filter);

    NutsQuery setIdFilter(NutsIdFilter filter);

    NutsQuery setIdFilter(String filter);

    NutsQuery setIds(Collection<String> ids);

    NutsQuery setAll(NutsQuery other);

    NutsQuery copyFrom(NutsQuery other);

    NutsQuery copy();

    ////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////
    NutsSession getSession();

    String[] getIds();

    boolean isSort();

    boolean isLatestVersions();

    Set<NutsDependencyScope> getScope();

    NutsDependencyFilter getDependencyFilter();

    NutsRepositoryFilter getRepositoryFilter();

    NutsVersionFilter getVersionFilter();

    NutsDescriptorFilter getDescriptorFilter();

    NutsIdFilter getIdFilter();

    String[] getJs();

    String[] getArch();

    String[] getPackaging();

    String[] getRepos();

    NutsQuery dependenciesOnly();

    NutsQuery mainOnly();

    Boolean getAcceptOptional();

    boolean isIgnoreNotFound();

    NutsQuery setIgnoreNotFound(boolean ignoreNotFound);

    boolean isIncludeFile();

    boolean isIncludeInstallInformation();

    boolean isIncludeEffective();

    boolean isIgnoreCache();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    NutsId findOne();

    NutsId findFirst();

    List<NutsId> find();

    Iterator<NutsId> findIterator();

    NutsDefinition fetchOne();

    NutsDefinition fetchFirst();

    String findNutspathString();

    String findClasspathString();

    List<NutsDefinition> fetch();

    Iterator<NutsDefinition> fetchIterator();

}
