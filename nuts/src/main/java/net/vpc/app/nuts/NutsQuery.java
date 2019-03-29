package net.vpc.app.nuts;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public interface NutsQuery extends NutsQueryBaseOptions<NutsQuery> {

    ////////////////////////////////////////////////////////
    // Setters
    ////////////////////////////////////////////////////////
    
    NutsQuery setId(String id);

    NutsQuery setId(NutsId value);

    NutsQuery mainAndDependencies();

    NutsQuery mainOnly();

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

    /**
     * setSort(true)
     *
     * @return
     */
    NutsQuery sort();

    NutsQuery setSort(boolean sort);

    /**
     * setIncludeAllVersions(false)
     *
     * @return
     */
    NutsQuery allVersions();

    /**
     * setIncludeAllVersions(true)
     *
     * @return
     */
    NutsQuery latestVersions();

    NutsQuery setIncludeAllVersions(boolean allVersions);

    NutsQuery setDependencyFilter(NutsDependencyFilter filter);

    NutsQuery setDependencyFilter(String filter);

    NutsQuery setRepositoryFilter(NutsRepositoryFilter filter);

    NutsQuery setRepositoryFilter(String filter);

    NutsQuery setDescriptorFilter(NutsDescriptorFilter filter);

    NutsQuery setDescriptorFilter(String filter);

    NutsQuery setIdFilter(NutsIdFilter filter);

    NutsQuery setIdFilter(String filter);

    NutsQuery setIds(Collection<String> ids);

    NutsQuery dependenciesOnly();

    NutsQuery setIgnoreNotFound(boolean ignoreNotFound);

    NutsQuery sort(Comparator<NutsId> comparator);

    NutsQuery setIncludeDuplicateVersions(boolean includeDuplicateVersions);

    NutsQuery copyFrom(NutsQuery other);

    NutsQuery copy();

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

    boolean isIgnoreNotFound();

    ////////////////////////////////////////////////////////
    // Result
    ////////////////////////////////////////////////////////
    
    NutsId findOne();

    NutsId findFirst();

    List<NutsId> find();

    Iterator<NutsId> findIterator();

    Iterable<NutsId> findIterable();

    NutsDefinition fetchOne();

    NutsDefinition fetchFirst();

    String findNutspathString();

    String findClasspathString();

    List<NutsDefinition> fetch();

    Iterator<NutsDefinition> fetchIterator();

    Stream<NutsId> findStream();

    Stream<NutsDefinition> fetchStream();

    Comparator<NutsId> getSortIdComparator();

    boolean isIncludeDuplicatedVersions();

    boolean isIncludeMain();

    boolean isIncludeAllVersions();

    NutsQueryOptions toOptions();
    
}
