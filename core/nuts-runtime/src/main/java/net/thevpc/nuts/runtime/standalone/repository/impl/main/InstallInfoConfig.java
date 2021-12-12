package net.thevpc.nuts.runtime.standalone.repository.impl.main;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InstallInfoConfig extends NutsConfigItem implements Cloneable{

    private static final long serialVersionUID = 3;
    private NutsId id;
    private boolean installed;
    private boolean required;
    private Instant creationDate;
    private Instant lastModificationDate;
    private String creationUser;
    private String lastModificationUser;
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

    public String getCreationUser() {
        return creationUser;
    }

    public void setCreationUser(String creationUser) {
        this.creationUser = creationUser;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstallInfoConfig that = (InstallInfoConfig) o;
        return installed == that.installed && required == that.required && Objects.equals(id, that.id) && Objects.equals(creationDate, that.creationDate) && Objects.equals(lastModificationDate, that.lastModificationDate) && Objects.equals(creationUser, that.creationUser) && Objects.equals(lastModificationUser, that.lastModificationUser) && Objects.equals(sourceRepoName, that.sourceRepoName) && Objects.equals(sourceRepoUUID, that.sourceRepoUUID) && Objects.equals(requiredBy, that.requiredBy) && Objects.equals(requires, that.requires);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, installed, required, creationDate, lastModificationDate, creationUser, lastModificationUser, sourceRepoName, sourceRepoUUID, requiredBy, requires);
    }

    @Override
    public String toString() {
        return "InstallInfoConfig{"
                + "id=" + id
                + ", installed=" + installed
                + ", required=" + required
                + ", installDate=" + creationDate
                + ", lastModifiedUser=" + lastModificationUser
                + ", lastModifiedDate=" + lastModificationDate
                + ", installUser='" + creationUser + '\''
                + '}';
    }

    public Instant getLastModificationDate() {
        return lastModificationDate;
    }

    public InstallInfoConfig setLastModificationDate(Instant lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
        return this;
    }

    public String getLastModificationUser() {
        return lastModificationUser;
    }

    public InstallInfoConfig setLastModificationUser(String lastModificationUser) {
        this.lastModificationUser = lastModificationUser;
        return this;
    }

    public InstallInfoConfig copy() {
        InstallInfoConfig cloned = null;
        try {
            cloned = (InstallInfoConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
        if(requiredBy!=null) {
            cloned.requiredBy = new ArrayList<>();
            for (InstallDepConfig installDepConfig : requiredBy) {
                cloned.requiredBy.add(installDepConfig.copy());
            }
        }
        if(requires!=null) {
            cloned.requires = new ArrayList<>();
            for (InstallDepConfig installDepConfig : requires) {
                cloned.requires.add(installDepConfig.copy());
            }
        }
        return cloned;
    }
}
