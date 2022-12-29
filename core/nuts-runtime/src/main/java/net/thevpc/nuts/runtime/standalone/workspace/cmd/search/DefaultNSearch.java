package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.NDescriptorFilter;
import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.NRepositoryFilter;
import net.thevpc.nuts.NSession;

public class DefaultNSearch {

    private final NRepositoryFilter repositoryFilter;
    private final NIdFilter idFilter;
    private final NDescriptorFilter descriptorFilter;
    private final String[] ids;
    private final NSession session;
    public DefaultNSearch(String[] ids, NRepositoryFilter repositoryFilter, NIdFilter idFilter,
                          NDescriptorFilter descriptorFilter, NSession session) {
        this.ids = ids;
        this.session = session;
        this.repositoryFilter = repositoryFilter;
        this.idFilter = idFilter;
        this.descriptorFilter = descriptorFilter;
    }

    public NSession getSession() {
        return session;
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
