package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.Arrays;
import java.util.List;

public class DefaultCommands implements NCommands {

    public DefaultCustomCommandsModel model;
    public NWorkspace ws;

    public DefaultCommands(NWorkspace ws) {
        this.ws = ws;
        this.model = NWorkspaceExt.of().getModel().aliasesModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public List<NCommandFactoryConfig> getCommandFactories() {
        return Arrays.asList(model.getFactories());
    }

    @Override
    public void addCommandFactory(NCommandFactoryConfig commandFactoryConfig) {
        model.addFactory(commandFactoryConfig);
    }

    @Override
    public void removeCommandFactory(String commandFactoryId) {
        model.removeFactory(commandFactoryId);
    }

    @Override
    public boolean removeCommandFactoryIfExists(String commandFactoryId) {
        return model.removeFactoryIfExists(commandFactoryId);
    }

    @Override
    public boolean commandExists(String command) {
        return findCommand(command) != null;
    }

    @Override
    public boolean commandFactoryExists(String factoryId) {
        return model.commandFactoryExists(factoryId);
    }

    @Override
    public boolean addCommand(NCommandConfig command) {
        return model.add(command);
    }

    @Override
    public boolean updateCommand(NCommandConfig command) {
        return model.update(command);
    }

    @Override
    public void removeCommand(String command) {
        model.remove(command);
    }

    @Override
    public boolean removeCommandIfExists(String name) {
        if (model.find(name) != null) {
            model.remove(name);
            return true;
        }
        return false;
    }

    @Override
    public NCustomCmd findCommand(String name, NId forId, NId forOwner) {
        return model.find(name, forId, forOwner);
    }

    @Override
    public NCustomCmd findCommand(String name) {
        return model.find(name);
    }

    @Override
    public List<NCustomCmd> findAllCommands() {
        return model.findAll();
    }

    @Override
    public List<NCustomCmd> findCommandsByOwner(NId id) {
        return model.findByOwner(id);
    }

}
