package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 */
public class DefaultNutsWorkspaceListManager implements NutsWorkspaceListManager {

    private Map<String, NutsWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private final NutsWorkspace defaultWorkspace;
    private NutsWorkspaceListConfig config;
    private final String name;

    public DefaultNutsWorkspaceListManager(NutsWorkspace ws, String name) {
        this.defaultWorkspace = ws;
        if (CoreStringUtils.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        Path file = getConfigFile();
        if (Files.exists(file)) {
            this.config = this.defaultWorkspace.formats().json().parse(file, NutsWorkspaceListConfig.class);
            for (NutsWorkspaceLocation var : this.config.getWorkspaces()) {
                this.workspaces.put(var.getUuid(), var);
            }
        } else {
            this.config = new NutsWorkspaceListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.workspaces.put(ws.uuid(),
                    new NutsWorkspaceLocation()
                            .setUuid(ws.uuid())
                            .setName("default-workspace")
                            .setLocation(this.defaultWorkspace.locations().getWorkspaceLocation().toString())
            );
            this.save();
        }
    }

    private Path getConfigFile() {
        return this.defaultWorkspace
                .locations()
                .getStoreLocation(
                        this.defaultWorkspace
                                .id().resolveId(DefaultNutsWorkspaceListManager.class),
                        NutsStoreLocation.CONFIG).resolve(name + "-nuts-workspace-list.json");
    }

    @Override
    public List<NutsWorkspaceLocation> getWorkspaces() {
        return new ArrayList<>(workspaces.values());
    }

    @Override
    public NutsWorkspaceLocation getWorkspaceLocation(String uuid) {
        return this.workspaces.get(uuid);
    }

    public DefaultNutsWorkspaceListManager setWorkspaces(Map<String, NutsWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    @Override
    public NutsWorkspaceListConfig getConfig() {
        return config;
    }

    @Override
    public DefaultNutsWorkspaceListManager setConfig(NutsWorkspaceListConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public NutsWorkspace addWorkspace(String path) {
        NutsWorkspace workspace = this.createWorkspace(path);
        NutsWorkspaceLocation workspaceLocation = new NutsWorkspaceLocation()
                .setUuid(workspace.uuid())
                .setName(workspace.locations().getWorkspaceLocation().getFileName().toString())
                .setLocation(workspace.locations().getWorkspaceLocation().toString());
        workspaces.put(workspace.uuid(), workspaceLocation);
        this.save();
        return workspace;
    }

    private NutsWorkspace createWorkspace(String path) {
        return Nuts.openWorkspace(
                this.defaultWorkspace.config().optionsBuilder()
                .setWorkspace(path)
                .setOpenMode(NutsWorkspaceOpenMode.OPEN_OR_CREATE)
                .setSkipCompanions(true)
        );
    }

    @Override
    public void save() {
        this.config.setWorkspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        Path file = getConfigFile();
        this.defaultWorkspace.formats().json().value(this.config).print(file);
    }

    @Override
    public boolean removeWorkspace(String uuid) {
        boolean b = this.workspaces.remove(uuid) != null;
        if (b) {
            save();
        }
        return b;
    }
}
