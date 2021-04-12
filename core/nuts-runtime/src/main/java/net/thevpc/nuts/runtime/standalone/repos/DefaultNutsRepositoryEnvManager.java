package net.thevpc.nuts.runtime.standalone.repos;

import net.thevpc.nuts.NutsRepositoryEnvManager;

import java.util.Map;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsRepositoryEnvManager implements NutsRepositoryEnvManager {
    private DefaultNutsRepositoryEnvModel model;
    private NutsSession session;

    public DefaultNutsRepositoryEnvManager(DefaultNutsRepositoryEnvModel model) {
        this.model = model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsRepositoryEnvManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }
    

    @Override
    public Map<String, String> toMap(boolean inherit) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(inherit,getSession());
    }

    @Override
    public String get(String key, String defaultValue, boolean inherit) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.get(key,defaultValue,inherit,getSession());
    }

    @Override
    public Map<String, String> toMap() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.toMap(getSession());
    }

    @Override
    public String get(String property, String defaultValue) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        return model.get(property, defaultValue,getSession());
    }

    @Override
    public NutsRepositoryEnvManager set(String property, String value) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
        model.set(property, value,session);
        return this;
    }

}
