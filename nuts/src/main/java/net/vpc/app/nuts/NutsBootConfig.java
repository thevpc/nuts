package net.vpc.app.nuts;

public class NutsBootConfig {
    private String bootAPIVersion = null;
    private String bootRuntime = null;
    private String bootRuntimeDependencies = null;
    private String bootRepositories = null;
    private String bootJavaCommand = null;
    private String bootJavaOptions = null;

    public String getBootAPIVersion() {
        return bootAPIVersion;
    }

    public NutsBootConfig setBootAPIVersion(String bootAPIVersion) {
        this.bootAPIVersion = bootAPIVersion;
        return this;
    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsBootConfig setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public String getBootRuntimeDependencies() {
        return bootRuntimeDependencies;
    }

    public NutsBootConfig setBootRuntimeDependencies(String bootRuntimeDependencies) {
        this.bootRuntimeDependencies = bootRuntimeDependencies;
        return this;
    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public NutsBootConfig setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public NutsBootConfig setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public String getBootRepositories() {
        return bootRepositories;
    }

    public NutsBootConfig setBootRepositories(String bootRepositories) {
        this.bootRepositories = bootRepositories;
        return this;
    }
}
