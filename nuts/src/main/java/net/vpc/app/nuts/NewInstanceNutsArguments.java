package net.vpc.app.nuts;

import java.io.File;

public class NewInstanceNutsArguments extends NutsArguments{
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

    public NewInstanceNutsArguments setJavaCommand(String javaCommand) {
        this.javaCommand = javaCommand;
        return this;
    }

    public File getBootFile() {
        return bootFile;
    }

    public NewInstanceNutsArguments setBootFile(File bootFile) {
        this.bootFile = bootFile;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NewInstanceNutsArguments setArgs(String[] args) {
        this.args = args;
        return this;
    }

    public String getHome() {
        return home;
    }

    public NewInstanceNutsArguments setHome(String home) {
        this.home = home;
        return this;
    }

    public String getBootVersion() {
        return bootVersion;
    }

    public NewInstanceNutsArguments setBootVersion(String bootVersion) {
        this.bootVersion = bootVersion;
        return this;
    }

    public String getRequiredVersion() {
        return requiredVersion;
    }

    public NewInstanceNutsArguments setRequiredVersion(String requiredVersion) {
        this.requiredVersion = requiredVersion;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public NewInstanceNutsArguments setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }
}
