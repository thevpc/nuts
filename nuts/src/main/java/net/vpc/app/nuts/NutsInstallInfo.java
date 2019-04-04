package net.vpc.app.nuts;

import java.nio.file.Path;

public class NutsInstallInfo {
    private boolean installed;
    private boolean justInstalled;
    private Path installFolder;

    public NutsInstallInfo(boolean installed, Path installFolder) {
        this.installed = installed;
        this.installFolder = installFolder;
    }

    public boolean isInstalled() {
        return installed;
    }

    public Path getInstallFolder() {
        return installFolder;
    }

    public boolean isJustInstalled() {
        return justInstalled;
    }

    public void setJustInstalled(boolean justInstalled) {
        this.justInstalled = justInstalled;
    }
    
}
