package net.vpc.app.nuts.indexer;

import net.vpc.app.nuts.NutsWorkspaceLocation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NutsIndexSubscriber implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private Map<String, NutsWorkspaceLocation> workspaceLocations;

    public NutsIndexSubscriber() {
    }

    public NutsIndexSubscriber(NutsIndexSubscriber other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.workspaceLocations = new HashMap<>(other.getWorkspaceLocations());
    }

    public String getUuid() {
        return uuid;
    }

    public NutsIndexSubscriber setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Map<String, NutsWorkspaceLocation> getWorkspaceLocations() {
        return workspaceLocations;
    }

    public NutsIndexSubscriber setWorkspaceLocations(Map<String, NutsWorkspaceLocation> workspaceLocations) {
        this.workspaceLocations = workspaceLocations;
        return this;
    }

    @Override
    public String toString() {
        return "NutsIndexSubscriber{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", workspaceLocations=" + workspaceLocations +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsIndexSubscriber that = (NutsIndexSubscriber) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(workspaceLocations, that.workspaceLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, workspaceLocations);
    }

    public NutsIndexSubscriber copy() {
        return new NutsIndexSubscriber(this);
    }

    public String getName() {
        return name;
    }

    public NutsIndexSubscriber setName(String name) {
        this.name = name;
        return this;
    }

    public String cacheFolderName() {
        return this.name + "-" + this.uuid;
    }
}
