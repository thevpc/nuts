package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

public class DefaultNutsSearch {
    private final NutsRepositoryFilter repositoryFilter;
    private final NutsVersionFilter versionFilter;
    private final boolean sort;
    private final NutsIdFilter idFilter;
    private final boolean latestVersions;
    private final NutsDescriptorFilter descriptorFilter;
    private final String[] ids;
    private final NutsSession session;
    private final DefaultNutsWorkspace ws;
    private final boolean preferInstalled;
    private final boolean installedOnly;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsVersionFilter versionFilter, boolean sort, NutsIdFilter idFilter, boolean latestVersions,
                             NutsDescriptorFilter descriptorFilter,
                             boolean preferInstalled,
                             boolean installedOnly,
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
        this.preferInstalled = preferInstalled;
        this.installedOnly = installedOnly;
    }

    public boolean isPreferInstalled() {
        return preferInstalled;
    }

    public boolean isInstalledOnly() {
        return installedOnly;
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
