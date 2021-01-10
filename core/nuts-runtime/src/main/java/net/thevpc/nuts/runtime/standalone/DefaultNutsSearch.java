package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsDescriptorFilter;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsRepositoryFilter;
import net.thevpc.nuts.NutsSession;

public class DefaultNutsSearch {

    private final NutsRepositoryFilter repositoryFilter;
    private final NutsIdFilter idFilter;
    private final NutsDescriptorFilter descriptorFilter;
    private final String[] ids;
    private final NutsSession session;
    private boolean searchInInstalled;
    private boolean searchInOtherRepositories;

    public DefaultNutsSearch(String[] ids, NutsRepositoryFilter repositoryFilter, NutsIdFilter idFilter,
            NutsDescriptorFilter descriptorFilter,
                             boolean searchInInstalled,
                                     boolean searchInOtherRepositories,
                             NutsSession session) {
        this.ids = ids;
        this.session = session;
        this.repositoryFilter = repositoryFilter;
        this.idFilter = idFilter;
        this.descriptorFilter = descriptorFilter;
        this.searchInInstalled = searchInInstalled;
        this.searchInOtherRepositories = searchInOtherRepositories;
    }

    public boolean isSearchInInstalled() {
        return searchInInstalled;
    }

    public boolean isSearchInOtherRepositories() {
        return searchInOtherRepositories;
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
