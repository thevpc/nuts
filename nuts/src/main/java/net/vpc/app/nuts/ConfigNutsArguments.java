package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;

public class ConfigNutsArguments extends NutsArguments{
    private String versionOptions;
    private String applyUpdatesFile = null;
    private NutsBootCommand bootCommand = NutsBootCommand.RUN;
    private List<String> args = new ArrayList<>();
    private NutsBootOptions bootOptions = new NutsBootOptions();
    private NutsWorkspaceOptions workspaceCreateOptions = new NutsWorkspaceOptions()
            .setCreateIfNotFound(true);

    public String getVersionOptions() {
        return versionOptions;
    }

    public void setVersionOptions(String versionOptions) {
        this.versionOptions = versionOptions;
    }

    public String getApplyUpdatesFile() {
        return applyUpdatesFile;
    }

    public NutsArguments setApplyUpdatesFile(String applyUpdatesFile) {
        this.applyUpdatesFile = applyUpdatesFile;
        return this;
    }

    public List<String> getArgs() {
        return args;
    }

    public NutsArguments setArgs(List<String> args) {
        this.args = args;
        return this;
    }

    public NutsBootOptions getBootOptions() {
        return bootOptions;
    }

    public NutsArguments setBootOptions(NutsBootOptions bootOptions) {
        this.bootOptions = bootOptions;
        return this;
    }

    public NutsWorkspaceOptions getWorkspaceCreateOptions() {
        return workspaceCreateOptions;
    }

    public NutsArguments setWorkspaceCreateOptions(NutsWorkspaceOptions workspaceCreateOptions) {
        this.workspaceCreateOptions = workspaceCreateOptions;
        return this;
    }

    public NutsBootCommand getBootCommand() {
        return bootCommand;
    }

    public ConfigNutsArguments setBootCommand(NutsBootCommand bootCommand) {
        this.bootCommand = bootCommand;
        return this;
    }
}
