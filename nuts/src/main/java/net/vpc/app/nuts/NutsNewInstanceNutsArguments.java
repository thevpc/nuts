package net.vpc.app.nuts;

import java.io.File;

public class NutsNewInstanceNutsArguments extends NutsArguments{
    private File bootFile;
    private String[] args;
    private String home;
    private String bootVersion;
    private String requiredVersion;
    private String javaCommand;
    private String javaOptions;

    public String getJavaCommand() {
        return javaCommand;
    }

    public NutsNewInstanceNutsArguments setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public File getBootFile() {
        return bootFile;
    }

    public NutsNewInstanceNutsArguments setBootFile(File bootFile) {
        this.bootFile = bootFile;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NutsNewInstanceNutsArguments setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public String getHome() {
        return home;
    }

    public NutsNewInstanceNutsArguments setHome(String home) {
        this.home = home;
        return this;
    }

    public String getBootVersion() {
        return bootVersion;
    }

    public NutsNewInstanceNutsArguments setBootVersion(String bootVersion) {
        this.bootVersion = bootVersion;
        return this;
    }

    public String getRequiredVersion() {
        return requiredVersion;
    }

    public NutsNewInstanceNutsArguments setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NutsNewInstanceNutsArguments setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }
}
