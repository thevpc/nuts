package net.thevpc.nuts.boot.internal.maven;

import net.thevpc.nuts.boot.NBootRepositoryLocation;

import java.util.List;

public class NMavenSettingsBoot {
    private String settingsFile;
    private String localRepository;
    private String remoteRepository;
    private List<NBootRepositoryLocation> activeRepositories;

    public String settingsFile() {
        return settingsFile;
    }

    public NMavenSettingsBoot settingsFile(String settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    public String localRepository() {
        return localRepository;
    }

    public NMavenSettingsBoot localRepository(String localRepository) {
        this.localRepository = localRepository;
        return this;
    }

    public String remoteRepository() {
        return remoteRepository;
    }

    public NMavenSettingsBoot remoteRepository(String remoteRepository) {
        this.remoteRepository = remoteRepository;
        return this;
    }

    public List<NBootRepositoryLocation> activeRepositories() {
        return activeRepositories;
    }

    public NMavenSettingsBoot activeRepositories(List<NBootRepositoryLocation> activeRepositories) {
        this.activeRepositories = activeRepositories;
        return this;
    }
}
