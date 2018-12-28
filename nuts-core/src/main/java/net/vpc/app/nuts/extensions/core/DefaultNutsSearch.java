package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
public class DefaultNutsSearch {
    private NutsRepositoryFilter repositoryFilter;
    private NutsVersionFilter versionFilter;
    private boolean sort;
    private NutsIdFilter idFilter;
    private boolean latestVersions;
    private NutsDescriptorFilter descriptorFilter;
    private String[] ids;
    private NutsSession session;
    private DefaultNutsWorkspace ws;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsVersionFilter versionFilter, boolean sort, NutsIdFilter idFilter, boolean latestVersions,
                             NutsDescriptorFilter descriptorFilter,
                             DefaultNutsWorkspace ws,
                             NutsSession session) {
        this.ids = ids;
        this.ws = ws;
        this.session = session;
        this.repositoryFilter = repositoryFilter;
        this.versionFilter = versionFilter;
        this.sort = sort;
        this.idFilter = idFilter;
        this.latestVersions = latestVersions;
        this.descriptorFilter = descriptorFilter;
    }

    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NutsVersionFilter getVersionFilter() {
        return versionFilter;
    }

    public boolean isSort() {
        return sort;
    }

    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    public boolean isLatestVersions() {
        return latestVersions;
    }

    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    public String[] getIds() {
        return ids;
    }


    public NutsSession getSession() {
        return session;
    }


}
