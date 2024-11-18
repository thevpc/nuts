package net.thevpc.nuts.runtime.standalone.repository.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NOptional;

@NComponentScope(NScopeType.SESSION)
public class DefaultNRepositories implements NRepositories {

    private DefaultNRepositoryModel model;
    private NWorkspace workspace;

    public DefaultNRepositories(NWorkspace workspace) {
        this.workspace = workspace;
        NWorkspaceExt e = NWorkspaceExt.of(workspace);
        this.model = e.getModel().repositoryModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NRepositoryFilters filter() {
        return NRepositoryFilters.of();
    }

//    private NRepository toSessionAwareRepo(NRepository x) {
//        return NRepositorySessionAwareImpl.of(x, model.getWorkspace(), workspace);
//    }

//    private NOptional<NRepository> toSessionAwareRepoOptional(NOptional<NRepository> x) {
//        return x.map(r->NRepositorySessionAwareImpl.of(r, model.getWorkspace(), workspace));
//    }

    @Override
    public List<NRepository> getRepositories() {
        return Arrays.stream(model.getRepositories())
                .collect(Collectors.toList());
    }

    @Override
    public NOptional<NRepository> findRepositoryById(String repositoryNameOrId) {
        return model.findRepositoryById(repositoryNameOrId);
    }

    @Override
    public NOptional<NRepository> findRepositoryByName(String repositoryNameOrId) {
        return model.findRepositoryByName(repositoryNameOrId);
    }

    @Override
    public NOptional<NRepository> findRepository(String repositoryNameOrId) {
        return model.findRepository(repositoryNameOrId);
    }

    @Override
    public NRepositories removeRepository(String repositoryId) {
        model.removeRepository(repositoryId);
        return this;
    }

    @Override
    public NRepositories removeAllRepositories() {
        model.removeAllRepositories();
        return this;
    }

    @Override
    public NRepository addRepository(NAddRepositoryOptions options) {
        return model.addRepository(options);
    }

    @Override
    public NRepository addRepository(String repositoryNamedUrl) {
        return model.addRepository(repositoryNamedUrl);
    }


    public DefaultNRepositoryModel getModel() {
        return model;
    }

}
