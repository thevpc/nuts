package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;

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

    public DefaultNutsWorkspaceListManager(NutsWorkspace ws, NutsSession session,String name) {
        this.defaultWorkspace = ws;
        if (CoreStringUtils.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        Path file = getConfigFile(session);
        if (Files.exists(file)) {
            this.config = this.defaultWorkspace.formats().element().setContentType(NutsContentType.JSON).parse(file, NutsWorkspaceListConfig.class);
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
            this.save(session);
        }
    }

    private Path getConfigFile(NutsSession session) {
        return this.defaultWorkspace
                .locations()
                .getStoreLocation(
                        this.defaultWorkspace
                                .id().resolveId(DefaultNutsWorkspaceListManager.class, session),
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
    public NutsWorkspace addWorkspace(String path, NutsSession session) {
        NutsWorkspace workspace = this.createWorkspace(path);
        NutsWorkspaceLocation workspaceLocation = new NutsWorkspaceLocation()
                .setUuid(workspace.uuid())
                .setName(workspace.locations().getWorkspaceLocation().getFileName().toString())
                .setLocation(workspace.locations().getWorkspaceLocation().toString());
        workspaces.put(workspace.uuid(), workspaceLocation);
        this.save(session);
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
    public void save(NutsSession session) {
        this.config.setWorkspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        Path file = getConfigFile(session);
        this.defaultWorkspace.formats().element().setContentType(NutsContentType.JSON).setValue(this.config).print(file);
    }

    @Override
    public boolean removeWorkspace(String uuid, NutsSession session) {
        boolean b = this.workspaces.remove(uuid) != null;
        if (b) {
            save(session);
        }
        return b;
    }
}
