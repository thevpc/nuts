package net.thevpc.nuts.runtime.standalone.workspace.list;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.*;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;


import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NBlankable;

import java.util.*;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * date 2019-03-02
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNWorkspaceList implements NWorkspaceList {

    private String name;
    private Map<String, NWorkspaceLocation> workspaces = new LinkedHashMap<>();
    private NWorkspaceListConfig config;

    public DefaultNWorkspaceList() {
        NWorkspace ws = NWorkspace.of();
        name(null);
        NPath file = getConfigFile();
        if (file.exists()) {
            this.config = NElementReader.ofJson().read(file, NWorkspaceListConfig.class);
            for (NWorkspaceLocation var : this.config.workspaces()) {
                this.workspaces.put(var.uuid(), var);
            }
        } else {
            this.config = new NWorkspaceListConfig()
                    .uuid(UUID.randomUUID().toString())
                    .name("default-config");
            this.workspaces.put(ws.uuid(),
                    new NWorkspaceLocation()
                            .uuid(ws.uuid())
                            .name(NConstants.Names.DEFAULT_WORKSPACE_NAME)
                            .location(NWorkspace.of().workspaceLocation().toString())
            );
            this.save();
        }
    }

    public DefaultNWorkspaceList name(String name) {
        if (NBlankable.isBlank(name)) {
            name = "default";
        }
        this.name = name.trim();
        return this;
    }

    public String name() {
        return name;
    }

    private NPath getConfigFile() {
        return NPath.of(NStoreKey.ofConf(NId.getForClass(DefaultNWorkspaceList.class).get()))
                .resolve(name + "-nuts-workspace-list.json");
    }

    @Override
    public List<NWorkspaceLocation> workspaces() {
        return new ArrayList<>(workspaces.values());
    }

    @Override
    public NWorkspaceLocation getWorkspaceLocation(String uuid) {
        return this.workspaces.get(uuid);
    }

    @Override
    public NWorkspaceListConfig config() {
        return config;
    }

    @Override
    public DefaultNWorkspaceList config(NWorkspaceListConfig config) {
        this.config = config;
        return this;
    }

    @Override
    public NWorkspace addWorkspace(String path) {
        NWorkspace ss = this.createWorkspace(path);
        NWorkspace workspace = NWorkspace.of();
        NWorkspaceLocation workspaceLocation = new NWorkspaceLocation()
                .uuid(ss.uuid())
                .name(workspace.workspaceLocation().name())
                .location(workspace.workspaceLocation().toString());
        workspaces.put(ss.uuid(), workspaceLocation);
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
        this.config.workspaces(this.workspaces.isEmpty()
                ? null
                : new ArrayList<>(this.workspaces.values()));
        NPath file = getConfigFile();
        NElementWriter.ofJson().write(this.config,file);
    }

    public DefaultNWorkspaceList setWorkspaces(Map<String, NWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
        return this;
    }

    private NWorkspace createWorkspace(String path) {
        return Nuts.openWorkspace(
                NWorkspaceOptionsBuilder.of()
                        .workspace(path)
                        .openMode(NOpenMode.OPEN_OR_CREATE)
                        .installCompanions(false)
                        .build()
        );
    }

}
