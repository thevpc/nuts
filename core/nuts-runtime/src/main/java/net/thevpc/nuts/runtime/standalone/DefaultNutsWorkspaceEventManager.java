package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;

public class DefaultNutsWorkspaceEventManager implements NutsWorkspaceEventManager {

    private DefaultNutsWorkspaceEventModel model;
    private NutsSession session;

    public DefaultNutsWorkspaceEventManager(DefaultNutsWorkspaceEventModel model) {
        this.model = model;
    }

    public DefaultNutsWorkspaceEventModel getModel() {
        return model;
    }

    public void setModel(DefaultNutsWorkspaceEventModel model) {
        this.model = model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsWorkspaceEventManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsWorkspaceEventManager removeRepositoryListener(NutsRepositoryListener listener) {
        model.removeRepositoryListener(listener);
        return this;
    }

    @Override
    public NutsWorkspaceEventManager addRepositoryListener(NutsRepositoryListener listener) {
        model.addRepositoryListener(listener);
        return this;
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return model.getRepositoryListeners();
    }

    @Override
    public NutsWorkspaceEventManager addUserPropertyListener(NutsMapListener<String, Object> listener) {
        model.addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NutsWorkspaceEventManager removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        model.removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return model.getUserPropertyListeners();
    }

    @Override
    public NutsWorkspaceEventManager removeWorkspaceListener(NutsWorkspaceListener listener) {
        model.removeWorkspaceListener(listener);
        return this;
    }

    @Override
    public NutsWorkspaceEventManager addWorkspaceListener(NutsWorkspaceListener listener) {
        model.addWorkspaceListener(listener);
        return this;
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return model.getWorkspaceListeners();
    }

    @Override
    public NutsWorkspaceEventManager removeInstallListener(NutsInstallListener listener) {
        model.removeInstallListener(listener);
        return this;
    }

    @Override
    public NutsWorkspaceEventManager addInstallListener(NutsInstallListener listener) {
        model.addInstallListener(listener);
        return this;
    }

    @Override
    public NutsInstallListener[] getInstallListeners() {
        return model.getInstallListeners();
    }

    
}
