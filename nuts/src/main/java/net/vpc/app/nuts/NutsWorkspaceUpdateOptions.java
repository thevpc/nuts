package net.vpc.app.nuts;

public class NutsWorkspaceUpdateOptions {
    private boolean logUpdates=true;
    private boolean enableMajorUpdates=false;
    private boolean updateExtensions=true;
    private boolean applyUpdates;
    private String forceBootAPIVersion;

    public boolean isLogUpdates() {
        return logUpdates;
    }

    public NutsWorkspaceUpdateOptions setLogUpdates(boolean logUpdates) {
        this.logUpdates = logUpdates;
        return this;
    }

    public boolean isEnableMajorUpdates() {
        return enableMajorUpdates;
    }

    public NutsWorkspaceUpdateOptions setEnableMajorUpdates(boolean enableMajorUpdates) {
        this.enableMajorUpdates = enableMajorUpdates;
        return this;
    }

    public boolean isUpdateExtensions() {
        return updateExtensions;
    }

    public NutsWorkspaceUpdateOptions setUpdateExtensions(boolean updateExtensions) {
        this.updateExtensions = updateExtensions;
        return this;
    }

    public boolean isApplyUpdates() {
        return applyUpdates;
    }

    public NutsWorkspaceUpdateOptions setApplyUpdates(boolean applyUpdates) {
        this.applyUpdates = applyUpdates;
        return this;
    }

    public String getForceBootAPIVersion() {
        return forceBootAPIVersion;
    }

    public NutsWorkspaceUpdateOptions setForceBootAPIVersion(String forceBootAPIVersion) {
        this.forceBootAPIVersion = forceBootAPIVersion;
        return this;
    }
}
