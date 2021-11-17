package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public class InstallInfoConfig extends NutsConfigItem {

    private static final long serialVersionUID = 3;
    private NutsId id;
    private boolean installed;
    private boolean required;
    private Instant createdDate;
    private Instant lastModifiedDate;
    private String installUser;
    private String sourceRepoName;
    private String sourceRepoUUID;
    private List<InstallDepConfig> requiredBy;
    private List<InstallDepConfig> requires;

    public List<InstallDepConfig> getRequiredBy() {
        return requiredBy;
    }

    public InstallInfoConfig setRequiredBy(List<InstallDepConfig> requiredBy) {
        this.requiredBy = requiredBy;
        return this;
    }

    public List<InstallDepConfig> getRequires() {
        return requires;
    }

    public InstallInfoConfig setRequires(List<InstallDepConfig> requires) {
        this.requires = requires;
        return this;
    }

    public String getSourceRepoName() {
        return sourceRepoName;
    }

    public void setSourceRepoName(String sourceRepoName) {
        this.sourceRepoName = sourceRepoName;
    }

    public String getSourceRepoUUID() {
        return sourceRepoUUID;
    }

    public void setSourceRepoUUID(String sourceRepoUUID) {
        this.sourceRepoUUID = sourceRepoUUID;
    }

    public boolean isInstalled() {
        return installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public boolean isRequired() {
        return required;
    }

    public InstallInfoConfig setRequired(boolean required) {
        this.required = required;
        return this;
    }

    public NutsId getId() {
        return id;
    }

    public void setId(NutsId id) {
        this.id = id;
    }

    public String getInstallUser() {
        return installUser;
    }

    public void setInstallUser(String installUser) {
        this.installUser = installUser;
    }

    public Instant getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdDate, installUser);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InstallInfoConfig that = (InstallInfoConfig) o;
        return Objects.equals(id, that.id)
                && Objects.equals(createdDate, that.createdDate)
                && Objects.equals(lastModifiedDate, that.lastModifiedDate)
                && Objects.equals(installUser, that.installUser);
    }

    @Override
    public String toString() {
        return "InstallInfoConfig{"
                + "id=" + id
                + ", installed=" + installed
                + ", required=" + required
                + ", installDate=" + createdDate
                + ", lastModifiedDate=" + lastModifiedDate
                + ", installUser='" + installUser + '\''
                + '}';
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public InstallInfoConfig setLastModifiedDate(Instant lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
        return this;
    }
}
