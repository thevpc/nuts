package net.thevpc.nuts.runtime;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.common.ObservableMap;

import java.util.ArrayList;
import java.util.List;

public class DefaultNutsWorkspaceEventManager implements NutsWorkspaceEventManager {
    private NutsWorkspace ws;
    protected final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    protected final List<NutsInstallListener> installListeners = new ArrayList<>();
    protected final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    public DefaultNutsWorkspaceEventManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    @Override
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    @Override
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    @Override
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>)ws.userProperties()).addListener(listener);
    }

    @Override
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>)ws.userProperties()).removeListener(listener);
    }

    @Override
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return ((ObservableMap<String, Object>)ws.userProperties()).getListeners();
    }

    @Override
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    @Override
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    @Override
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[0]);
    }

    @Override
    public void removeInstallListener(NutsInstallListener listener) {
        installListeners.remove(listener);
    }

    @Override
    public void addInstallListener(NutsInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    @Override
    public NutsInstallListener[] getInstallListeners() {
        return installListeners.toArray(new NutsInstallListener[0]);
    }

}
