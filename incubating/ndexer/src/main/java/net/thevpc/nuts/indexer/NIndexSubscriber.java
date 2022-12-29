package net.thevpc.nuts.indexer;

import net.thevpc.nuts.NWorkspaceLocation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NIndexSubscriber implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private Map<String, NWorkspaceLocation> workspaceLocations;

    public NIndexSubscriber() {
    }

    public NIndexSubscriber(NIndexSubscriber other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.workspaceLocations = new HashMap<>(other.getWorkspaceLocations());
    }

    public String getUuid() {
        return uuid;
    }

    public NIndexSubscriber setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public Map<String, NWorkspaceLocation> getWorkspaceLocations() {
        return workspaceLocations;
    }

    public NIndexSubscriber setWorkspaceLocations(Map<String, NWorkspaceLocation> workspaceLocations) {
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
        NIndexSubscriber that = (NIndexSubscriber) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(workspaceLocations, that.workspaceLocations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, workspaceLocations);
    }

    public NIndexSubscriber copy() {
        return new NIndexSubscriber(this);
    }

    public String getName() {
        return name;
    }

    public NIndexSubscriber setName(String name) {
        this.name = name;
        return this;
    }

    public String cacheFolderName() {
        return this.name + "-" + this.uuid;
    }
}
