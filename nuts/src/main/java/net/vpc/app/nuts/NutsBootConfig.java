package net.vpc.app.nuts;

public final class NutsBootConfig implements Cloneable {
    private String apiVersion = null;
    private String runtimeId = null;
    private String runtimeDependencies = null;
    private String repositories = null;
    private String javaCommand = null;
    private String javaOptions = null;

    public NutsBootConfig() {
    }

    public NutsBootConfig(NutsBootContext context) {
        if(context!=null){
            this.apiVersion =context.getApiId().getVersion().getValue();
            this.runtimeId =context.getRuntimeId().toString();
            this.runtimeDependencies =context.getRuntimeDependencies();
            this.repositories =context.getRepositories();
            this.javaCommand =context.getJavaCommand();
            this.javaOptions =context.getJavaOptions();
        }
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public NutsBootConfig setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootConfig setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeDependencies() {
        return runtimeDependencies;
    }

    public NutsBootConfig setRuntimeDependencies(String runtimeDependencies) {
        this.runtimeDependencies = runtimeDependencies;
        return this;
    }

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsBootConfig setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsBootConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getRepositories() {
        return repositories;
    }

    public NutsBootConfig setRepositories(String repositories) {
        this.repositories = repositories;
        return this;
    }

    public NutsBootConfig copy() {
        try {
            return (NutsBootConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException("Unexpected Behaviour");
        }
    }
}
