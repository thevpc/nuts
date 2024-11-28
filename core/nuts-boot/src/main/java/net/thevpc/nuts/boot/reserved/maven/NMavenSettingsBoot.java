package net.thevpc.nuts.boot.reserved.maven;

import net.thevpc.nuts.boot.NRepositoryLocationBoot;

import java.util.List;

public class NMavenSettingsBoot {
    private String settingsFile;
    private String localRepository;
    private String remoteRepository;
    private List<NRepositoryLocationBoot> activeRepositories;

    public String getSettingsFile() {
        return settingsFile;
    }

    public NMavenSettingsBoot setSettingsFile(String settingsFile) {
        this.settingsFile = settingsFile;
        return this;
    }

    public String getLocalRepository() {
        return localRepository;
    }

    public NMavenSettingsBoot setLocalRepository(String localRepository) {
        this.localRepository = localRepository;
        return this;
    }

    public String getRemoteRepository() {
        return remoteRepository;
    }

    public NMavenSettingsBoot setRemoteRepository(String remoteRepository) {
        this.remoteRepository = remoteRepository;
        return this;
    }

    public List<NRepositoryLocationBoot> getActiveRepositories() {
        return activeRepositories;
    }

    public NMavenSettingsBoot setActiveRepositories(List<NRepositoryLocationBoot> activeRepositories) {
        this.activeRepositories = activeRepositories;
        return this;
    }
}
