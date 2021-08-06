package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import java.util.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultManager implements NutsCommandManager {

    public DefaultAliasModel model;
    public NutsSession session;

    public DefaultManager(DefaultAliasModel model) {
        this.model = model;
    }

    @Override
    public void addCommandFactory(NutsCommandFactoryConfig commandFactoryConfig) {
        checkSession();
        model.addFactory(commandFactoryConfig, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean removeCommandFactory(String factoryId) {
        checkSession();
        return model.removeFactory(factoryId, session);
    }

    @Override
    public boolean addCommand(NutsCommandConfig command) {
        checkSession();
        return model.add(command, session);
    }

    @Override
    public boolean removeCommand(String name) {
        checkSession();
        return model.remove(name, session);
    }

    @Override
    public NutsWorkspaceCustomCommand findCommand(String name) {
        checkSession();
        return model.find(name, session);
    }

    @Override
    public List<NutsWorkspaceCustomCommand> findAllCommands() {
        checkSession();
        return model.findAll(session);
    }

    @Override
    public List<NutsWorkspaceCustomCommand> findCommandByOwner(NutsId id) {
        checkSession();
        return model.findByOwner(id, session);
    }

    @Override
    public NutsCommandFactoryConfig[] getCommandFactories() {
        checkSession();
        return model.getFactories(session);
    }

    @Override
    public NutsWorkspaceCustomCommand findCommand(String name, NutsId forId, NutsId forOwner) {
        checkSession();
        return model.find(name, forId, forOwner, session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCommandManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

}
