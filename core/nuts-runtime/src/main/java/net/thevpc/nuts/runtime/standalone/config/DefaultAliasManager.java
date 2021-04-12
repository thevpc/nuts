package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import java.util.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultAliasManager implements NutsCommandAliasManager {

    public DefaultAliasModel model;
    public NutsSession session;

    public DefaultAliasManager(DefaultAliasModel model) {
        this.model = model;
    }

    @Override
    public void addFactory(NutsCommandAliasFactoryConfig commandFactoryConfig) {
        checkSession();
        model.addFactory(commandFactoryConfig, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean removeFactory(String factoryId) {
        checkSession();
        return model.removeFactory(factoryId, session);
    }

    @Override
    public boolean add(NutsCommandAliasConfig command) {
        checkSession();
        return model.add(command, session);
    }

    @Override
    public boolean remove(String name) {
        checkSession();
        return model.remove(name, session);
    }

    @Override
    public NutsWorkspaceCommandAlias find(String name) {
        checkSession();
        return model.find(name, session);
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findAll() {
        checkSession();
        return model.findAll(session);
    }

    @Override
    public List<NutsWorkspaceCommandAlias> findByOwner(NutsId id) {
        checkSession();
        return model.findByOwner(id, session);
    }

    @Override
    public NutsCommandAliasFactoryConfig[] getFactories() {
        checkSession();
        return model.getFactories(session);
    }

    @Override
    public NutsWorkspaceCommandAlias find(String name, NutsId forId, NutsId forOwner) {
        checkSession();
        return model.find(name, forId, forOwner, session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCommandAliasManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

}
