package net.thevpc.nuts.util;

import net.thevpc.nuts.spi.NRepositoryLocation;

import java.util.List;

public class NMavenSettings {
    private String settingsFile;
    private String localRepository;
    private String remoteRepository;
    private List<NRepositoryLocation> activeRepositories;

    public String getSettingsFile() {
        return settingsFile;
    }

    public NMavenSettings setSettingsFile(String settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public NMavenSettings setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
        return this;
    }

    public String getRemoteRepository() {
        return remoteRepository;
    }

    public NMavenSettings setRemoteRepository(String remoteRepository) {
        this.remoteRepository = remoteRepository;
        return this;
    }

    public List<NRepositoryLocation> getActiveRepositories() {
        return activeRepositories;
    }

    public NMavenSettings setActiveRepositories(List<NRepositoryLocation> activeRepositories) {
        this.activeRepositories = activeRepositories;
        return this;
    }
}
