package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

public class DefaultNutsSearch {

    private final NutsRepositoryFilter repositoryFilter;
    private final NutsVersionFilter versionFilter;
    private final NutsIdFilter idFilter;
    private final NutsDescriptorFilter descriptorFilter;
    private final String[] ids;
    private final DefaultNutsWorkspace ws;
    private final NutsQueryOptions options;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsVersionFilter versionFilter, NutsIdFilter idFilter,
            NutsDescriptorFilter descriptorFilter,
            DefaultNutsWorkspace ws,
            NutsQueryOptions options) {
        this.ids = ids;
        this.ws = ws;
        this.options = options;
        this.repositoryFilter = repositoryFilter;
        this.versionFilter = versionFilter;
        this.idFilter = idFilter;
        this.descriptorFilter = descriptorFilter;
    }

    public NutsQueryOptions getOptions() {
        return options;
    }

    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    public String[] getIds() {
        return ids;
    }

}
