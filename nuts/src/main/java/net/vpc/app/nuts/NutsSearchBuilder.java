package net.vpc.app.nuts;

import java.util.Collection;

public interface NutsSearchBuilder {
    NutsSearchBuilder addJs(Collection<String> value);

    NutsSearchBuilder addJs(String... value);

    NutsSearchBuilder addId(Collection<String> value);

    NutsSearchBuilder addId(String... value);

    NutsSearchBuilder addArch(Collection<String> value);

    NutsSearchBuilder addArch(String... value);

    NutsSearchBuilder addPackaging(Collection<String> value);

    NutsSearchBuilder addPackaging(String... value);

    NutsSearchBuilder addRepository(Collection<String> value);

    NutsSearchBuilder addRepository(String... value);

    NutsSearchBuilder copy();

    void copyFrom(NutsSearchBuilder other);

    NutsSearchBuilder setAll(NutsSearch other);
    NutsSearchBuilder setAll(NutsSearchBuilder other);

    boolean isSort();

    NutsSearchBuilder setSort(boolean sort);

    boolean isLatestVersions();

    NutsSearchBuilder setLatestVersions(boolean latestVersions);

    NutsSearchBuilder addIds(String... ids);

    NutsSearchBuilder addIds(NutsId... ids);

    NutsSearchBuilder setIds(String... ids);

    NutsSearchBuilder addId(String id);

    String[] getIds();

    NutsDependencyScope getScope();

    NutsSearchBuilder setScope(NutsDependencyScope scope);

    NutsSearchBuilder setDependencyFilter(NutsDependencyFilter filter);

    NutsDependencyFilter getDependencyFilter();

    NutsSearchBuilder setDependencyFilter(String filter);

    NutsSearchBuilder setRepositoryFilter(NutsRepositoryFilter filter);

    NutsRepositoryFilter getRepositoryFilter();

    NutsSearchBuilder setRepositoryFilter(String filter);

    NutsSearchBuilder setVersionFilter(NutsVersionFilter filter);

    NutsVersionFilter getVersionFilter();

    NutsSearchBuilder setVersionFilter(String filter);

    NutsSearchBuilder setDescriptorFilter(NutsDescriptorFilter filter);

    NutsDescriptorFilter getDescriptorFilter();

    NutsSearchBuilder setDescriptorFilter(String filter);

    NutsSearchBuilder setIdFilter(NutsIdFilter filter);

    NutsIdFilter getIdFilter();

    NutsSearchBuilder setIdFilter(String filter);

    String[] getJs();

    String[] getArch();

    String[] getPackagings();

    String[] getRepos();

    NutsSearch build();
}
