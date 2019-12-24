package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;

public class DefaultNutsSearch {

    private final NutsRepositoryFilter repositoryFilter;
    private final NutsIdFilter idFilter;
    private final NutsDescriptorFilter descriptorFilter;
    private final String[] ids;
    private final NutsSession session;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsIdFilter idFilter,
            NutsDescriptorFilter descriptorFilter,
                             NutsSession session) {
        this.ids = ids;
        this.session = session;
        this.repositoryFilter = repositoryFilter;
        this.idFilter = idFilter;
        this.descriptorFilter = descriptorFilter;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NutsIdFilter getIdFilter() {
        return idFilter;
    }

    public NutsDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    public String[] getRegularIds() {
        return ids;
    }

}
