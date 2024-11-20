package net.thevpc.nuts.runtime.standalone.workspace.list;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 */
public class DefaultNWorkspaceList implements NWorkspaceList {

    private final NSession defaultSession;
    private String name;
    private Map<String, NWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private NWorkspaceListConfig config;

    public DefaultNWorkspaceList(NSession session) {
        this.defaultSession = session;
        setName(null);
        NPath file = getConfigFile();
        if (file.exists()) {
            this.config = NElements.of().json().parse(file, NWorkspaceListConfig.class);
            for (NWorkspaceLocation var : this.config.getWorkspaces()) {
                this.workspaces.put(var.getUuid(), var);
            }
        } else {
            this.config = new NWorkspaceListConfig()
                    .setUuid(UUID.randomUUID().toString())
                    .setName("default-config");
            this.workspaces.put(session.getWorkspace().getUuid(),
                    new NWorkspaceLocation()
                            .setUuid(session.getWorkspace().getUuid())
                            .setName(NConstants.Names.DEFAULT_WORKSPACE_NAME)
                            .setLocation(NLocations.of().getWorkspaceLocation().toString())
            );
            this.save();
        }
    }

    public DefaultNWorkspaceList setName(String name) {
        if (NBlankable.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        return this;
    }

    public String getName() {
        return name;
    }

    private NPath getConfigFile() {
        return NLocations.of()
                .getStoreLocation(NId.ofClass(DefaultNWorkspaceList.class).get(),
                        NStoreType.CONF)
                .resolve(name + "-nuts-workspace-list.json");
    }

    @Override
    public List<NWorkspaceLocation> getWorkspaces() {
        return new ArrayList<>(workspaces.values());
    }

    @Override
    public NWorkspaceLocation getWorkspaceLocation(String uuid) {
        return this.workspaces.get(uuid);
    }

    @Override
    public NWorkspaceListConfig getConfig() {
        return config;
    }

    @Override
    public DefaultNWorkspaceList setConfig(NWorkspaceListConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public NSession addWorkspace(String path) {
        NSession ss = this.createWorkspace(path);
        NLocations locations = NLocations.of();
        NWorkspaceLocation workspaceLocation = new NWorkspaceLocation()
                .setUuid(ss.getWorkspace().getUuid())
                .setName(locations.getWorkspaceLocation().getName())
                .setLocation(locations.getWorkspaceLocation().toString());
        workspaces.put(ss.getWorkspace().getUuid(), workspaceLocation);
        this.save();
        return ss;
    }

    @Override
    public boolean removeWorkspace(String uuid) {
        boolean b = this.workspaces.remove(uuid) != null;
        if (b) {
            save();
        }
        return b;
    }

    @Override
    public void save() {
        this.config.setWorkspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        NPath file = getConfigFile();
        NElements.of().json().setValue(this.config)
                .setNtf(false)
                .print(file);
    }

    public DefaultNWorkspaceList setWorkspaces(Map<String, NWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    private NSession createWorkspace(String path) {
        return Nuts.openWorkspace(
                NWorkspaceOptionsBuilder.of()
                        .setWorkspace(path)
                        .setOpenMode(NOpenMode.OPEN_OR_CREATE)
                        .setInstallCompanions(false)
                        .build()
        );
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
