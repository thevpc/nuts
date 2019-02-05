package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsWorkspaceCommand;

public class DefaultNutsWorkspaceCommand implements NutsWorkspaceCommand {
    private String name;
    private NutsId owner;
    private String factoryId;
    private String[] command;
    private String[] executorOptions;

    public String getName() {
        return name;
    }

    public DefaultNutsWorkspaceCommand setName(String name) {
        this.name = name;
        return this;
    }

    public NutsId getOwner() {
        return owner;
    }

    public DefaultNutsWorkspaceCommand setOwner(NutsId owner) {
        this.owner = owner;
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

    @Override
    public String[] getExecutorOptions() {
        return executorOptions;
    }

    public DefaultNutsWorkspaceCommand setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }
}
