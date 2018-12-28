package net.vpc.app.nuts;


import java.util.Map;

public class NutsWorkspaceCommandFactoryConfig {
    private String factoryId;
    private String factoryType;
    private int priority;
    private Map<String,String> parameters;

    public String getFactoryId() {
        return factoryId;
    }

    public NutsWorkspaceCommandFactoryConfig setFactoryId(String factoryId) {
        this.factoryId = factoryId;
        return this;
    }

    public String getFactoryType() {
        return factoryType;
    }

    public NutsWorkspaceCommandFactoryConfig setFactoryType(String factoryType) {
        this.factoryType = factoryType;
        return this;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public NutsWorkspaceCommandFactoryConfig setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
        return this;
    }

    public int getPriority() {
        return priority;
    }

    public NutsWorkspaceCommandFactoryConfig setPriority(int priority) {
        this.priority = priority;
        return this;
    }
}
