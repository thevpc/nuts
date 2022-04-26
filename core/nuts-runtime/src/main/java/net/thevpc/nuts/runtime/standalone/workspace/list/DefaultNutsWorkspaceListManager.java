package net.thevpc.nuts.runtime.standalone.workspace.list;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.*;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 */
public class DefaultNutsWorkspaceListManager implements NutsWorkspaceListManager {

    private final NutsSession defaultSession;
    private String name;
    private Map<String, NutsWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private NutsWorkspaceListConfig config;

    public DefaultNutsWorkspaceListManager(NutsSession session) {
        this.defaultSession = session;
        setName(null);
        NutsPath file = getConfigFile(session);
        if (file.exists()) {
            this.config = NutsElements.of(this.defaultSession).json().parse(file, NutsWorkspaceListConfig.class);
            for (NutsWorkspaceLocation var : this.config.getWorkspaces()) {
                this.workspaces.put(var.getUuid(), var);
            }
        } else {
            this.config = new NutsWorkspaceListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.workspaces.put(session.getWorkspace().getUuid(),
                    new NutsWorkspaceLocation()
                            .setUuid(session.getWorkspace().getUuid())
                            .setName(NutsConstants.Names.DEFAULT_WORKSPACE_NAME)
                            .setLocation(this.defaultSession.locations().getWorkspaceLocation().toString())
            );
            this.save(session);
        }
    }

    public DefaultNutsWorkspaceListManager setName(String name) {
        if (NutsBlankable.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        return this;
    }

    public String getName() {
        return name;
    }

    private NutsPath getConfigFile(NutsSession session) {
        return session
                        .locations()
                        .getStoreLocation(NutsIdResolver.of(session).resolveId(DefaultNutsWorkspaceListManager.class),
                                NutsStoreLocation.CONFIG)
                .resolve(name + "-nuts-workspace-list.json");
    }

    @Override
    public List<NutsWorkspaceLocation> getWorkspaces() {
        return new ArrayList<>(workspaces.values());
    }

    @Override
    public NutsWorkspaceLocation getWorkspaceLocation(String uuid) {
        return this.workspaces.get(uuid);
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
    public NutsSession addWorkspace(String path, NutsSession session) {
        NutsSession ss = this.createWorkspace(path);
        NutsWorkspaceLocation workspaceLocation = new NutsWorkspaceLocation()
                .setUuid(ss.getWorkspace().getUuid())
                .setName(ss.locations().getWorkspaceLocation().getName())
                .setLocation(ss.locations().getWorkspaceLocation().toString());
        workspaces.put(ss.getWorkspace().getUuid(), workspaceLocation);
        this.save(session);
        return ss;
    }

    @Override
    public boolean removeWorkspace(String uuid, NutsSession session) {
        boolean b = this.workspaces.remove(uuid) != null;
        if (b) {
            save(session);
        }
        return b;
    }

    @Override
    public void save(NutsSession session) {
        this.config.setWorkspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        NutsPath file = getConfigFile(session);
        NutsElements.of(this.defaultSession).json().setValue(this.config)
                .setNtf(false)
                .print(file);
    }

    public DefaultNutsWorkspaceListManager setWorkspaces(Map<String, NutsWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    private NutsSession createWorkspace(String path) {
        return Nuts.openWorkspace(
                NutsWorkspaceOptionsBuilder.of()
                        .setWorkspace(path)
                        .setOpenMode(NutsOpenMode.OPEN_OR_CREATE)
                        .setSkipCompanions(true)
                        .build()
        );
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
