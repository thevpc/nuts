package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.common.io.FileUtils;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Class for managing a Workspace list
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 */
public class DefaultNutsWorkspaceListManager implements NutsWorkspaceListManager {

    private Map<String, NutsWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private NutsWorkspace defaultWorkspace;
    private NutsWorkspaceListConfig config;
    private String name;

    public DefaultNutsWorkspaceListManager(NutsWorkspace ws,String name) {
        this.defaultWorkspace = ws;
        if(StringUtils.isEmpty(name)){
            name="default";
        }
        this.name=name.trim();
        File file = getConfigFile();
        if (file.exists()) {
            this.config = this.defaultWorkspace.getIOManager().readJson(file, NutsWorkspaceListConfig.class);
            for (NutsWorkspaceLocation var : this.config.getWorkspaces()) {
                this.workspaces.put(var.getName(), var);
            }
        } else {
            this.config = new NutsWorkspaceListConfig()
                .setUuid(UUID.randomUUID().toString())
                .setName("default-config");
            this.workspaces.put("default-workspace",
                new NutsWorkspaceLocation()
                    .setName("default-workspace")
                    .setLocation(this.defaultWorkspace.getConfigManager().getWorkspaceLocation())
            );
            this.save();
        }
    }

    private File getConfigFile() {
        return new File(this.defaultWorkspace
                .getConfigManager()
                .getStoreLocation(
                    this.defaultWorkspace
                        .resolveIdForClass(DefaultNutsWorkspaceListManager.class)
                        .getSimpleNameId()
                        .setVersion("LATEST"),
                    NutsStoreFolder.CONFIG),
                name+"-nuts-workspace-list.json");
    }

    public Map<String, NutsWorkspaceLocation> getWorkspaces() {
        return workspaces;
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

    public NutsWorkspace addWorkspace(String name) {
        NutsWorkspace workspace = this.createWorkspace(name);
        NutsWorkspaceLocation location = new NutsWorkspaceLocation()
            .setName(name)
            .setLocation(workspace.getConfigManager().getWorkspaceLocation());
        workspaces.put(name, location);
        return workspace;
    }

    private NutsWorkspace createWorkspace(String name) {
        return Nuts.openWorkspace(new NutsWorkspaceOptions()
            .setWorkspace(name)
            .setOpenMode(NutsWorkspaceOpenMode.DEFAULT)
            .setSkipPostCreateInstallCompanionTools(true)
        );
    }

    public void save() {
        this.config.setWorkspaces(this.workspaces.isEmpty()
            ? null
            : new ArrayList<>(this.workspaces.values()));
        File file = getConfigFile();
        this.defaultWorkspace.getIOManager().writeJson(this.config, file, true);
    }

    public boolean deleteWorkspace(String name) {
        if (name.equals("default-workspace")) {
            return false;
        }
        try {
            NutsWorkspace workspace = Nuts.openWorkspace(name);
            File file = new File(workspace.getConfigManager().getWorkspaceLocation());
            this.workspaces.remove(name);
            return FileUtils.deleteFolderTree(file, null);
        } catch (NutsException ex) {
            return false;
        }
    }

    public void onOffWorkspace(String name, Boolean value) {
        this.workspaces.get(name).setEnabled(value);
    }
}
