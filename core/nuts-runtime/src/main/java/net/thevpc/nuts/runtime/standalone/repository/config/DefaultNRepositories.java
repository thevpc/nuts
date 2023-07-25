package net.thevpc.nuts.runtime.standalone.repository.config;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NRepositorySessionAwareImpl;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

@NComponentScope(NScopeType.SESSION)
public class DefaultNRepositories implements NRepositories {

    private DefaultNRepositoryModel model;
    private NSession session;

    public DefaultNRepositories(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().repositoryModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NCallableSupport.DEFAULT_SUPPORT;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NRepositoryFilters filter() {
        return NRepositoryFilters.of(getSession());
    }

    private NRepository toSessionAwareRepo(NRepository x) {
        return NRepositorySessionAwareImpl.of(x, model.getWorkspace(), session);
    }

    private NOptional<NRepository> toSessionAwareRepoOptional(NOptional<NRepository> x) {
        return x.map(r->NRepositorySessionAwareImpl.of(r, model.getWorkspace(), session));
    }

    @Override
    public List<NRepository> getRepositories() {
        return Arrays.stream(model.getRepositories(session)).map(x -> toSessionAwareRepo(x))
                .collect(Collectors.toList());
    }

    @Override
    public NOptional<NRepository> findRepositoryById(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepoOptional(model.findRepositoryById(repositoryNameOrId, session));
    }

    @Override
    public NOptional<NRepository> findRepositoryByName(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepoOptional(model.findRepositoryByName(repositoryNameOrId, session));
    }

    @Override
    public NOptional<NRepository> findRepository(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepoOptional(model.findRepository(repositoryNameOrId, session));
    }

    @Override
    public NRepositories removeRepository(String repositoryId) {
        checkSession();
        model.removeRepository(repositoryId, session);
        return this;
    }

    @Override
    public NRepositories removeAllRepositories() {
        checkSession();
        model.removeAllRepositories(session);
        return this;
    }

    @Override
    public NRepository addRepository(NAddRepositoryOptions options) {
        checkSession();
        NRepository r = model.addRepository(options, session);
        return r == null ? null : toSessionAwareRepo(r);
    }

    @Override
    public NRepository addRepository(String repositoryNamedUrl) {
        checkSession();
        NRepository r = model.addRepository(repositoryNamedUrl, session);
        return r == null ? null : toSessionAwareRepo(r);
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNRepositoryModel getModel() {
        return model;
    }

}
