package net.vpc.app.nuts;

public class NutsInstallInfo {
    private boolean installed;
    private String installFolder;

    public NutsInstallInfo(boolean installed, String installFolder) {
        this.installed = installed;
        this.installFolder = installFolder;
    }

    public boolean isInstalled() {
        return installed;
    }

    public String getInstallFolder() {
        return installFolder;
    }
}
