package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.common.ObservableMap;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.util.ArrayList;
import java.util.List;

public class DefaultNutsWorkspaceEventModel {
    private NutsWorkspace ws;
    protected final List<NutsWorkspaceListener> workspaceListeners = new ArrayList<>();
    protected final List<NutsInstallListener> installListeners = new ArrayList<>();
    protected final List<NutsRepositoryListener> repositoryListeners = new ArrayList<>();

    public DefaultNutsWorkspaceEventModel(NutsWorkspace ws) {
        this.ws = ws;
    }

    
    public void removeRepositoryListener(NutsRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    
    public void addRepositoryListener(NutsRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    
    public NutsRepositoryListener[] getRepositoryListeners() {
        return repositoryListeners.toArray(new NutsRepositoryListener[0]);
    }

    
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>) NutsWorkspaceUtils.defaultSession(ws).env().getProperties()).addListener(listener);
    }

    
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>)NutsWorkspaceUtils.defaultSession(ws).env().getProperties()).removeListener(listener);
    }

    
    public NutsMapListener<String, Object>[] getUserPropertyListeners() {
        return ((ObservableMap<String, Object>)NutsWorkspaceUtils.defaultSession(ws).env().getProperties()).getListeners();
    }

    
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    
    public NutsWorkspaceListener[] getWorkspaceListeners() {
        return workspaceListeners.toArray(new NutsWorkspaceListener[0]);
    }

    
    public void removeInstallListener(NutsInstallListener listener) {
        installListeners.remove(listener);
    }

    
    public void addInstallListener(NutsInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    
    public NutsInstallListener[] getInstallListeners() {
        return installListeners.toArray(new NutsInstallListener[0]);
    }

}
