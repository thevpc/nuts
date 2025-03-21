package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.NDescriptorFilter;
import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.NRepositoryFilter;

public class DefaultNSearch {

    private final NRepositoryFilter repositoryFilter;
    private final NIdFilter idFilter;
    private final NDescriptorFilter descriptorFilter;
    private final String[] ids;
    public DefaultNSearch(String[] ids, NRepositoryFilter repositoryFilter, NIdFilter idFilter,
                          NDescriptorFilter descriptorFilter) {
        this.ids = ids;
        this.repositoryFilter = repositoryFilter;
        this.idFilter = idFilter;
        this.descriptorFilter = descriptorFilter;
    }

    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NIdFilter getIdFilter() {
        return idFilter;
    }

    public NDescriptorFilter getDescriptorFilter() {
        return descriptorFilter;
    }

    public String[] getRegularIds() {
        return ids;
    }

}
