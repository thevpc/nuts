package net.vpc.app.nuts;

public class NutsWorkspaceCommandConfig {
    private NutsId id;
    private String name;
    private String factoryId;
    private String[] command;

    public NutsId getId() {
        return id;
    }

    public NutsWorkspaceCommandConfig setId(NutsId id) {
        this.id = id;
        return this;
    }

    public String getFactoryId() {
        return factoryId;
    }

    public NutsWorkspaceCommandConfig setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public String[] getCommand() {
        return command;
    }

    public NutsWorkspaceCommandConfig setCommand(String... command) {
        this.command = command;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceCommandConfig setName(String name) {
        this.name = name;
        return this;
    }
}
