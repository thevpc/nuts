package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspaceCommand;

public class DefaultNutsWorkspaceCommand implements NutsWorkspaceCommand {
    private String name;
    private NutsId id;
    private String factoryId;
    private String[] command;

    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCommand setName(String name) {
        this.name = name;
        return this;
    }

    public NutsId getId() {
        return id;
    }

    public DefaultNutsWorkspaceCommand setId(NutsId id) {
        this.id = id;
        return this;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public DefaultNutsWorkspaceCommand setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public String[] getCommand() {
        return command;
    }

    public DefaultNutsWorkspaceCommand setCommand(String[] command) {
        this.command = command;
        return this;
    }
}
