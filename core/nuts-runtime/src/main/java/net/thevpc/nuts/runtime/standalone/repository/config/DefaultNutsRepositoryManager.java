package net.thevpc.nuts.runtime.standalone.repository.config;

import java.util.Arrays;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsRepositorySessionAwareImpl;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

public class DefaultNutsRepositoryManager implements NutsRepositoryManager {

    private DefaultNutsRepositoryModel model;
    private NutsSession session;

    public DefaultNutsRepositoryManager(DefaultNutsRepositoryModel model) {
        this.model = model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRepositoryManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NutsRepositoryFilters filter() {
        return NutsRepositoryFilters.of(getSession());
    }

    private NutsRepository toSessionAwareRepo(NutsRepository x){
        return NutsRepositorySessionAwareImpl.of(x, model.getWorkspace(), session);
    }
    
    @Override
    public NutsRepository[] getRepositories() {
        return Arrays.stream(model.getRepositories(session)).map(x -> toSessionAwareRepo(x))
                .toArray(NutsRepository[]::new);
    }

    @Override
    public NutsRepository findRepositoryById(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepositoryById(repositoryNameOrId, session));
    }

    @Override
    public NutsRepository findRepositoryByName(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepositoryByName(repositoryNameOrId, session));
    }

    @Override
    public NutsRepository findRepository(String repositoryNameOrId) {
        checkSession();
        return toSessionAwareRepo(model.findRepository(repositoryNameOrId, session));
    }

    @Override
    public NutsRepository getRepository(String repositoryIdOrName) throws NutsRepositoryNotFoundException {
        checkSession();
        return toSessionAwareRepo(model.getRepository(repositoryIdOrName, session));
    }

    @Override
    public NutsRepositoryManager removeRepository(String repositoryId) {
        checkSession();
        model.removeRepository(repositoryId, session);
        return this;
    }

    @Override
    public NutsRepositoryManager removeAllRepositories() {
        checkSession();
        model.removeAllRepositories(session);
        return this;
    }

    @Override
    public NutsRepository addRepository(NutsAddRepositoryOptions options) {
        checkSession();
        return toSessionAwareRepo(model.addRepository(options, session));
    }

    @Override
    public NutsRepository addRepository(String repositoryNamedUrl) {
        checkSession();
        return toSessionAwareRepo(model.addRepository(repositoryNamedUrl, session));
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
    }

    public DefaultNutsRepositoryModel getModel() {
        return model;
    }

}
