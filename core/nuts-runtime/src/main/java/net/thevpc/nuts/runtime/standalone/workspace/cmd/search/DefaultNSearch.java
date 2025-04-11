package net.thevpc.nuts.runtime.standalone.workspace.cmd.search;

import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.NRepositoryFilter;

public class DefaultNSearch {

    private final NRepositoryFilter repositoryFilter;
    private final NDefinitionFilter definitionFilter;
    private final String[] ids;
    public DefaultNSearch(String[] ids, NRepositoryFilter repositoryFilter,
                          NDefinitionFilter definitionFilter) {
        this.ids = ids;
        this.repositoryFilter = repositoryFilter;
        this.definitionFilter = definitionFilter;
    }

    public NRepositoryFilter getRepositoryFilter() {
        return repositoryFilter;
    }

    public NDefinitionFilter getDefinitionFilter() {
        return definitionFilter;
    }

    public String[] getRegularIds() {
        return ids;
    }

}
