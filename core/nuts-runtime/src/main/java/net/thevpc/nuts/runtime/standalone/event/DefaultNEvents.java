package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NObservableMapListener;

import java.util.List;

@NComponentScope(NScopeType.SESSION)
public class DefaultNEvents implements NEvents {

    private DefaultNWorkspaceEventModel model;
    private NWorkspace workspace;

    public DefaultNEvents(NWorkspace workspace) {
        this.workspace = workspace;
        NWorkspaceExt e = NWorkspaceExt.of();
        this.model = e.getModel().eventsModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceEventModel getModel() {
        return model;
    }

    public void setModel(DefaultNWorkspaceEventModel model) {
        this.model = model;
    }

    @Override
    public NEvents removeRepositoryListener(NRepositoryListener listener) {
        model.removeRepositoryListener(listener);
        return this;
    }

    @Override
    public NEvents addRepositoryListener(NRepositoryListener listener) {
        model.addRepositoryListener(listener);
        return this;
    }

    @Override
    public List<NRepositoryListener> getRepositoryListeners() {
        return model.getRepositoryListeners();
    }

    @Override
    public NEvents addUserPropertyListener(NObservableMapListener<String, Object> listener) {
        model.addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NEvents removeUserPropertyListener(NObservableMapListener<String, Object> listener) {
        model.removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public List<NObservableMapListener<String, Object>> getUserPropertyListeners() {
        return model.getUserPropertyListeners();
    }

    @Override
    public NEvents removeWorkspaceListener(NWorkspaceListener listener) {
        model.removeWorkspaceListener(listener);
        return this;
    }

    @Override
    public NEvents addWorkspaceListener(NWorkspaceListener listener) {
        model.addWorkspaceListener(listener);
        return this;
    }

    @Override
    public List<NWorkspaceListener> getWorkspaceListeners() {
        return model.getWorkspaceListeners();
    }

    @Override
    public NEvents removeInstallListener(NInstallListener listener) {
        model.removeInstallListener(listener);
        return this;
    }

    @Override
    public NEvents addInstallListener(NInstallListener listener) {
        model.addInstallListener(listener);
        return this;
    }

    @Override
    public List<NInstallListener> getInstallListeners() {
        return model.getInstallListeners();
    }

    
}
