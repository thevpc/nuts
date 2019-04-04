package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.common.strings.StringUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public class DefaultNutsWorkspaceListManager implements NutsWorkspaceListManager {

    private Map<String, NutsWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private NutsWorkspace defaultWorkspace;
    private NutsWorkspaceListConfig config;
    private String name;

    public DefaultNutsWorkspaceListManager(NutsWorkspace ws, String name) {
        this.defaultWorkspace = ws;
        if (StringUtils.isEmpty(name)) {
            name = "default";
        }
        this.name = name.trim();
        Path file = getConfigFile();
        if (Files.exists(file)) {
            this.config = this.defaultWorkspace.io().readJson(file, NutsWorkspaceListConfig.class);
            for (NutsWorkspaceLocation var : this.config.getWorkspaces()) {
                this.workspaces.put(var.getUuid(), var);
            }
        } else {
            this.config = new NutsWorkspaceListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.workspaces.put(ws.getUuid(),
                    new NutsWorkspaceLocation()
                            .setUuid(ws.getUuid())
                            .setName("default-workspace")
                            .setLocation(this.defaultWorkspace.config().getWorkspaceLocation().toString())
            );
            this.save();
        }
    }

    private Path getConfigFile() {
        return this.defaultWorkspace
                .config()
                .getStoreLocation(
                        this.defaultWorkspace
                                .resolveIdForClass(DefaultNutsWorkspaceListManager.class)
                                .getSimpleNameId(),
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

    public NutsWorkspaceListConfig getConfig() {
        return config;
    }

    public DefaultNutsWorkspaceListManager setConfig(NutsWorkspaceListConfig config) {
        this.config = config;
        return this;
    }

    public NutsWorkspace addWorkspace(String path) {
        NutsWorkspace workspace = this.createWorkspace(path);
        NutsWorkspaceLocation workspaceLocation = new NutsWorkspaceLocation()
                .setUuid(workspace.getUuid())
                .setName(workspace.config().getWorkspaceLocation().getFileName().toString())
                .setLocation(workspace.config().getWorkspaceLocation().toString());
        workspaces.put(workspace.getUuid(), workspaceLocation);
        this.save();
        return workspace;
    }

    private NutsWorkspace createWorkspace(String path) {
        return Nuts.openWorkspace(new NutsWorkspaceOptions()
                .setWorkspace(path)
                .setOpenMode(NutsWorkspaceOpenMode.OPEN_OR_CREATE)
                .setSkipInstallCompanions(true)
        );
    }

    private void save() {
        this.config.setWorkspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        Path file = getConfigFile();
        this.defaultWorkspace.io().writeJson(this.config, file, true);
    }

    public boolean removeWorkspace(String uuid) {
        boolean b = this.workspaces.remove(uuid) != null;
        if (b) {
            save();
        }
        return b;
    }

    public void onOffWorkspace(String name, Boolean value) {
        this.workspaces.get(name).setEnabled(value);
        this.save();
    }
}
