package net.vpc.app.nuts;

import java.io.Serializable;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public class NutsWorkspaceLocation implements Serializable {

    private static final long serialVersionUID = 1;

    private String uuid;
    private String name;
    private String location;
    private boolean enabled = true;

    public NutsWorkspaceLocation() {
    }

    public NutsWorkspaceLocation(NutsWorkspaceLocation other) {
        this.name = other.uuid;
        this.name = other.getName();
        this.location = other.getLocation();
        this.enabled = other.isEnabled();
    }

    public NutsWorkspaceLocation(String uuid, String name, String location) {
        this.uuid = uuid;
        this.name = name;
        this.location = location;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsWorkspaceLocation setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceLocation setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsWorkspaceLocation setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceLocation setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NutsWorkspaceLocation that = (NutsWorkspaceLocation) o;

        return location != null ? location.equals(that.location) : that.location == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceLocation{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", enabled=" + enabled +
                '}';
    }

    public NutsWorkspaceLocation copy() {
        return new NutsWorkspaceLocation(this);
    }
}
