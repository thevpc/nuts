package net.thevpc.nuts.runtime.standalone.event;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMapListener;

import java.util.List;

public class DefaultNEvents implements NEvents {

    private DefaultNWorkspaceEventModel model;
    private NSession session;

    public DefaultNEvents(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().eventsModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceEventModel getModel() {
        return model;
    }

    public void setModel(DefaultNWorkspaceEventModel model) {
        this.model = model;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NEvents setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
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
    public NEvents addUserPropertyListener(NMapListener<String, Object> listener) {
        model.addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NEvents removeUserPropertyListener(NMapListener<String, Object> listener) {
        model.removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public List<NMapListener<String, Object>> getUserPropertyListeners() {
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
