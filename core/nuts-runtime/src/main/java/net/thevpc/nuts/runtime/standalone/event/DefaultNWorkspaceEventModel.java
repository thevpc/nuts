package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.lib.common.collections.ObservableMap;
import net.thevpc.nuts.util.NMapListener;

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

    
    public void addUserPropertyListener(NMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>) NEnvs.of(NSessionUtils.defaultSession(ws)).getProperties()).addListener(listener);
    }

    
    public void removeUserPropertyListener(NMapListener<String, Object> listener) {
        ((ObservableMap<String, Object>) NEnvs.of(NSessionUtils.defaultSession(ws)).getProperties()).removeListener(listener);
    }

    
    public List<NMapListener<String, Object>> getUserPropertyListeners() {
        return ((ObservableMap<String, Object>) NEnvs.of(NSessionUtils.defaultSession(ws)).getProperties()).getListeners();
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
