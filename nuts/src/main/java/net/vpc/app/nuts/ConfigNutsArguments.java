package net.vpc.app.nuts;

import java.util.ArrayList;
import java.util.List;

public class ConfigNutsArguments extends NutsArguments{
    private boolean version = false;
    private boolean doupdate = false;
    private boolean checkupdates = false;
    private String applyUpdatesFile = null;
    private boolean showHelp = false;
    private boolean showLicense = false;
    private List<String> args = new ArrayList<>();
    private NutsBootOptions bootOptions = new NutsBootOptions();
    private NutsWorkspaceCreateOptions workspaceCreateOptions = new NutsWorkspaceCreateOptions()
            .setCreateIfNotFound(true);

    public boolean isVersion() {
        return version;
    }

    public NutsArguments setVersion(boolean version) {
        this.version = version;
        return this;
    }

    public boolean isDoupdate() {
        return doupdate;
    }

    public NutsArguments setDoupdate(boolean doupdate) {
        this.doupdate = doupdate;
        return this;
    }

    public boolean isCheckupdates() {
        return checkupdates;
    }

    public NutsArguments setCheckupdates(boolean checkupdates) {
        this.checkupdates = checkupdates;
        return this;
    }

    public String getApplyUpdatesFile() {
        return applyUpdatesFile;
    }

    public NutsArguments setApplyUpdatesFile(String applyUpdatesFile) {
        this.applyUpdatesFile = applyUpdatesFile;
        return this;
    }

    public boolean isShowHelp() {
        return showHelp;
    }

    public NutsArguments setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
        return this;
    }

    public boolean isShowLicense() {
        return showLicense;
    }

    public NutsArguments setShowLicense(boolean showLicense) {
        this.showLicense = showLicense;
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

    public NutsWorkspaceCreateOptions getWorkspaceCreateOptions() {
        return workspaceCreateOptions;
    }

    public NutsArguments setWorkspaceCreateOptions(NutsWorkspaceCreateOptions workspaceCreateOptions) {
        this.workspaceCreateOptions = workspaceCreateOptions;
        return this;
    }
}
