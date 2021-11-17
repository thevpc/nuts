package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class NdiScriptOptions implements Cloneable {

    private String id;
    private boolean forceBoot;
    private boolean fetch;
    private boolean includeEnv;
    private boolean addNutsScript;
    //    private NutsEnvInfo env;
    private NutsLauncherOptions launcher = new NutsLauncherOptions();


    private String nutsVersion;
    private NutsSession session;

    private NutsId nutsApiId;
    private NutsPath nutsApiJarPath;
    private NutsWorkspaceBootConfig workspaceBootConfig;

    public NdiScriptOptions() {
    }

    public NutsLauncherOptions getLauncher() {
        return launcher;
    }

    public NdiScriptOptions setLauncher(NutsLauncherOptions launcher) {
        this.launcher = launcher;
        return this;
    }

    //    public NdiScriptOptions setEnv(NutsEnvInfo env) {
//        this.env = env;
//        return this;
//    }
//
//    public NutsEnvInfo getEnv() {
//        return env;
//    }
//
    public boolean isAddNutsScript() {
        return addNutsScript;
    }

    public NdiScriptOptions setAddNutsScript(boolean addNutsScript) {
        this.addNutsScript = addNutsScript;
        return this;
    }

    public String getId() {
        return id;
    }

    public NdiScriptOptions setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isForceBoot() {
        return forceBoot;
    }

    public NdiScriptOptions setForceBoot(boolean forceBoot) {
        this.forceBoot = forceBoot;
        return this;
    }

    public boolean isFetch() {
        return fetch;
    }

    public NdiScriptOptions setFetch(boolean fetch) {
        this.fetch = fetch;
        return this;
    }

//    public List<String> getExecutorOptions() {
//        return executorOptions;
//    }
//
//    public NdiScriptOptions setExecutorOptions(List<String> executorOptions) {
//        this.executorOptions = executorOptions;
//        return this;
//    }

    public NutsSession getSession() {
        return session;
    }

    public NdiScriptOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public boolean isIncludeEnv() {
        return includeEnv;
    }

    public NdiScriptOptions setIncludeEnv(boolean includeEnv) {
        this.includeEnv = includeEnv;
        return this;
    }

    public NdiScriptOptions copy() {
        NdiScriptOptions c;
        try {
            c = (NdiScriptOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
        c.setLauncher(c.getLauncher() == null ? null : c.getLauncher().copy());
        return c;
    }

    public NutsPath resolveNutsApiJarPath() {
        if (nutsApiJarPath == null) {
            NutsId nid = resolveNutsApiId();
            if (getLauncher().getSwitchWorkspaceLocation() == null) {
                NutsDefinition apiDef = session.search()
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
                nutsApiJarPath = apiDef.getPath();
            } else {
                NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                nutsApiJarPath = NutsPath.of(bootConfig.getStoreLocation(nid, NutsStoreLocation.LIB),session);
                session.locations().getDefaultIdFilename(nid);
            }
        }
        return nutsApiJarPath;
    }

    public NutsPath resolveBinFolder() {
        return resolveNutsAppsFolder().resolve("bin");
    }

    public NutsPath resolveIncFolder() {
        return resolveNutsAppsFolder().resolve("inc");
    }

    public NutsPath resolveNutsAppsFolder() {
        NutsSession ws = session;
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId apiId = session.getWorkspace().getApiId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NutsPath.of(
                    bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS),session
            );
        } else {
            return ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS);
        }
    }

    public NutsPath resolveNutsApiAppsFolder() {
        NutsSession ws = session;
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId apiId = ws.getWorkspace().getApiId().builder().setVersion(nutsVersion).build();
        apiId = session.search().addId(apiId).setLatest(true).setFailFast(true).setContent(true)
                .setDistinct(true)
                .getResultDefinitions().singleton().getId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NutsPath.of(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS),session);
        } else {
            return ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS);
        }
    }

    public NutsDefinition resolveNutsApiDef() {
        return session.search().addId(resolveNutsApiId())
                .setLatest(true)
                .setContent(true)
                .setFailFast(true)
                .setDistinct(true)
                .getResultDefinitions().singleton();
    }

    public NutsId resolveNutsApiId() {
        if (nutsApiId == null) {
            if (getLauncher().getSwitchWorkspaceLocation() == null) {
                if (nutsVersion == null) {
                    nutsApiId = session.getWorkspace().getApiId();
                } else {
                    nutsApiId = session.search().addId(
                                    session.getWorkspace().getApiId().builder().setVersion(nutsVersion).build()
                            ).setLatest(true)
                            .setDistinct(true)
                            .getResultIds().singleton();
                }
            } else {
                NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                NutsVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                                    Paths.get(bootConfig.getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG))
                                            .getParent())
                            .filter(
                                    f
                                            -> NutsVersion.of(f.getFileName().toString(), session).getLong(0, -1) == -1
                                            && Files.exists(f.resolve("nuts-api-config.json"))
                            ).map(
                                    f -> NutsVersion.of(f.getFileName().toString(), session)
                            ).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
                } catch (IOException e) {
                    throw new NutsIOException(session, e);
                }
                if (_latestVersion == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing nuts-api version to link to"));
                }
                nutsApiId = session.getWorkspace().getApiId().builder().setVersion(_latestVersion).build();
            }
        }
        return nutsApiId;
    }

    public NutsVersion getNutsApiVersion() {
        return resolveNutsApiId().getVersion();
    }

    public Path getWorkspaceLocation() {
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return Paths.get(bootConfig.getEffectiveWorkspace());
        } else {
            return session.locations().getWorkspaceLocation().toFile();
        }
    }

    public NutsWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        if (workspaceBootConfig == null) {
            workspaceBootConfig = session.config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (workspaceBootConfig == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        return workspaceBootConfig;
    }
}
