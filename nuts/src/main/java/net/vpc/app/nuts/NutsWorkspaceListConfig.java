package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing a Workspace list
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public class NutsWorkspaceListConfig implements Serializable {

    private static final long serialVersionUID = 2;
    private String uuid;
    private String name;
    private List<NutsWorkspaceLocation> workspaces;

    public NutsWorkspaceListConfig() {
    }

    public NutsWorkspaceListConfig(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public NutsWorkspaceListConfig(NutsWorkspaceListConfig other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.workspaces = other.getWorkspaces() == null ? null : new ArrayList<>(other.getWorkspaces());
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceListConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceListConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<NutsWorkspaceLocation> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<NutsWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceListConfig{" + "uuid=" + uuid + ", name=" + name + ", workspaces=" + workspaces + '}';
    }
}
