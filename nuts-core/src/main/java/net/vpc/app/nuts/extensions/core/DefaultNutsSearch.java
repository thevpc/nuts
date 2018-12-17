package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;

public class DefaultNutsSearch implements NutsSearch {
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private boolean sort;
    private NutsIdFilter idFilter;
    private boolean latestVersions;
    private NutsDescriptorFilter descriptorFilter;
    private String[] ids;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsVersionFilter versionFilter, boolean sort, NutsIdFilter idFilter, boolean latestVersions, NutsDescriptorFilter descriptorFilter) {
        this.ids = ids;
        this.repositoryFilter = repositoryFilter;
        this.versionFilter = versionFilter;
        this.sort = sort;
        this.idFilter = idFilter;
        this.latestVersions = latestVersions;
        this.descriptorFilter = descriptorFilter;
    }

    @Override
    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    @Override
    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    @Override
    public boolean isSort() {
        return sort;
    }

    @Override
    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    @Override
    public boolean isLatestVersions() {
        return latestVersions;
    }

    @Override
    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    public String[] getIds() {
        return ids;
    }
}
