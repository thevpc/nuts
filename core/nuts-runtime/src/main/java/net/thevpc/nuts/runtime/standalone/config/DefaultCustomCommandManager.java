package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.util.List;

public class DefaultCustomCommandManager implements NutsCustomCommandManager {

    public DefaultCustomCommandsModel model;
    public NutsSession session;

    public DefaultCustomCommandManager(DefaultCustomCommandsModel model) {
        this.model = model;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsCommandFactoryConfig[] getCommandFactories() {
        checkSession();
        return model.getFactories(session);
    }

    @Override
    public void addCommandFactory(NutsCommandFactoryConfig commandFactoryConfig) {
        checkSession();
        model.addFactory(commandFactoryConfig, session);
    }

    @Override
    public void removeCommandFactory(String commandFactoryId) {
        checkSession();
        model.removeFactory(commandFactoryId, session);
    }

    @Override
    public boolean removeCommandFactoryIfExists(String commandFactoryId) {
        checkSession();
        return model.removeFactoryIfExists(commandFactoryId, session);
    }

    @Override
    public boolean commandExists(String command) {
        checkSession();
        return findCommand(command) != null;
    }

    @Override
    public boolean commandFactoryExists(String factoryId) {
        checkSession();
        return model.commandFactoryExists(factoryId, session);
    }

    @Override
    public boolean addCommand(NutsCommandConfig command) {
        checkSession();
        return model.add(command, session);
    }

    @Override
    public boolean updateCommand(NutsCommandConfig command) {
        checkSession();
        return model.update(command, session);
    }

    @Override
    public void removeCommand(String command) {
        checkSession();
        model.remove(command, session);
    }

    @Override
    public boolean removeCommandIfExists(String name) {
        checkSession();
        if (model.find(name, session) != null) {
            model.remove(name, session);
            return true;
        }
        return false;
    }

    @Override
    public NutsWorkspaceCustomCommand findCommand(String name, NutsId forId, NutsId forOwner) {
        checkSession();
        return model.find(name, forId, forOwner, session);
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
    public List<NutsWorkspaceCustomCommand> findCommandsByOwner(NutsId id) {
        checkSession();
        return model.findByOwner(id, session);
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCustomCommandManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }
}
