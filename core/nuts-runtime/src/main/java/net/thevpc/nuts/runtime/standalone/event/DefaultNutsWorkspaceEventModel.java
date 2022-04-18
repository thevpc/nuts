package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.ObservableMap;

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

    
    public List<NutsRepositoryListener> getRepositoryListeners() {
        return repositoryListeners;
    }

    
    public void addUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>) NutsSessionUtils.defaultSession(ws).env().getProperties()).addListener(listener);
    }

    
    public void removeUserPropertyListener(NutsMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>) NutsSessionUtils.defaultSession(ws).env().getProperties()).removeListener(listener);
    }

    
    public List<NutsMapListener<String, Object>> getUserPropertyListeners() {
        return ((ObservableMap<String, Object>) NutsSessionUtils.defaultSession(ws).env().getProperties()).getListeners();
    }

    
    public void removeWorkspaceListener(NutsWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    
    public void addWorkspaceListener(NutsWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public List<NutsWorkspaceListener> getWorkspaceListeners() {
        return workspaceListeners;
    }

    
    public void removeInstallListener(NutsInstallListener listener) {
        installListeners.remove(listener);
    }

    
    public void addInstallListener(NutsInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    
    public List<NutsInstallListener> getInstallListeners() {
        return installListeners;
    }

}
