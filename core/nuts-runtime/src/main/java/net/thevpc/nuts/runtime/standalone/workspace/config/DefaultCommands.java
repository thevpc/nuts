package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.util.Arrays;
import java.util.List;

public class DefaultCommands implements NCommands {

    public DefaultCustomCommandsModel model;
    public NSession session;

    public DefaultCommands(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().aliasesModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public List<NCommandFactoryConfig> getCommandFactories() {
        checkSession();
        return Arrays.asList(model.getFactories(session));
    }

    @Override
    public void addCommandFactory(NCommandFactoryConfig commandFactoryConfig) {
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
    public boolean addCommand(NCommandConfig command) {
        checkSession();
        return model.add(command, session);
    }

    @Override
    public boolean updateCommand(NCommandConfig command) {
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
    public NCustomCommand findCommand(String name, NId forId, NId forOwner) {
        checkSession();
        return model.find(name, forId, forOwner, session);
    }

    @Override
    public NCustomCommand findCommand(String name) {
        checkSession();
        return model.find(name, session);
    }

    @Override
    public List<NCustomCommand> findAllCommands() {
        checkSession();
        return model.findAll(session);
    }

    @Override
    public List<NCustomCommand> findCommandsByOwner(NId id) {
        checkSession();
        return model.findByOwner(id, session);
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NCommands setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }
}
