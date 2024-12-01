package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;

import net.thevpc.nuts.util.NObservableMap;
import net.thevpc.nuts.util.NObservableMapListener;

import java.util.ArrayList;
import java.util.List;

public class DefaultNWorkspaceEventModel {
    private NWorkspace ws;
    protected final List<NWorkspaceListener> workspaceListeners = new ArrayList<>();
    protected final List<NInstallListener> installListeners = new ArrayList<>();
    protected final List<NRepositoryListener> repositoryListeners = new ArrayList<>();

    public DefaultNWorkspaceEventModel(NWorkspace ws) {
        this.ws = ws;
    }

    
    public void removeRepositoryListener(NRepositoryListener listener) {
        repositoryListeners.add(listener);
    }

    
    public void addRepositoryListener(NRepositoryListener listener) {
        if (listener != null) {
            repositoryListeners.add(listener);
        }
    }

    
    public List<NRepositoryListener> getRepositoryListeners() {
        return repositoryListeners;
    }

    
    public void addUserPropertyListener(NObservableMapListener<String, Object> listener) {
        ((NObservableMap<String, Object>) NWorkspace.get().getProperties()).addMapListener(listener);
    }

    
    public void removeUserPropertyListener(NObservableMapListener<String, Object> listener) {
        ((NObservableMap<String, Object>) NWorkspace.get().getProperties()).removeMapListener(listener);
    }

    
    public List<NObservableMapListener<String, Object>> getUserPropertyListeners() {
        return ((NObservableMap<String, Object>) NWorkspace.get().getProperties()).getMapListeners();
    }

    
    public void removeWorkspaceListener(NWorkspaceListener listener) {
        workspaceListeners.add(listener);
    }

    
    public void addWorkspaceListener(NWorkspaceListener listener) {
        if (listener != null) {
            workspaceListeners.add(listener);
        }
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

    public List<NWorkspaceListener> getWorkspaceListeners() {
        return workspaceListeners;
    }

    
    public void removeInstallListener(NInstallListener listener) {
        installListeners.remove(listener);
    }

    
    public void addInstallListener(NInstallListener listener) {
        if (listener != null) {
            installListeners.add(listener);
        }
    }

    
    public List<NInstallListener> getInstallListeners() {
        return installListeners;
    }

}
