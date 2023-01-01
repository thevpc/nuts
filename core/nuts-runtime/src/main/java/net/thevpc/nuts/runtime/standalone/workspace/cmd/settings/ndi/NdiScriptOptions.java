package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;

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
    private NLauncherOptions launcher = new NLauncherOptions();


    private String nutsVersion;
    private NSession session;

    private NId nutsApiId;
    private NPath nutsApiJarPath;
    private NWorkspaceBootConfig workspaceBootConfig;

    public NdiScriptOptions() {
    }

    public NLauncherOptions getLauncher() {
        return launcher;
    }

    public NdiScriptOptions setLauncher(NLauncherOptions launcher) {
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

    public NSession getSession() {
        return session;
    }

    public NdiScriptOptions setSession(NSession session) {
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

    public NPath resolveNutsApiJarPath() {
        if (nutsApiJarPath == null) {
            NId nid = resolveNutsApiId();
            if (getLauncher().getSwitchWorkspaceLocation() == null) {
                NDefinition apiDef = session.search()
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
                nutsApiJarPath = apiDef.getContent().orNull();
            } else {
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                nutsApiJarPath = NPath.of(bootConfig.getStoreLocation(nid, NStoreLocation.LIB),session);
                session.locations().getDefaultIdFilename(nid);
            }
        }
        return nutsApiJarPath;
    }

    public NPath resolveBinFolder() {
        return resolveNutsAppsFolder().resolve("bin");
    }

    public NPath resolveIncFolder() {
        return resolveNutsAppsFolder().resolve("inc");
    }

    public NPath resolveNutsAppsFolder() {
        NSession ws = session;
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = session.getWorkspace().getApiId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(
                    bootConfig.getStoreLocation(apiId, NStoreLocation.APPS),session
            );
        } else {
            return ws.locations().getStoreLocation(apiId, NStoreLocation.APPS);
        }
    }

    public NPath resolveNutsApiAppsFolder() {
        NSession ws = session;
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = ws.getWorkspace().getApiId().builder().setVersion(nutsVersion).build();
        apiId = session.search().addId(apiId).setLatest(true).setFailFast(true).setContent(true)
                .setDistinct(true)
                .getResultDefinitions().singleton().getId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(bootConfig.getStoreLocation(apiId, NStoreLocation.APPS),session);
        } else {
            return ws.locations().getStoreLocation(apiId, NStoreLocation.APPS);
        }
    }

    public NDefinition resolveNutsApiDef() {
        return session.search().addId(resolveNutsApiId())
                .setLatest(true)
                .setContent(true)
                .setFailFast(true)
                .setDistinct(true)
                .getResultDefinitions().singleton();
    }

    public NId resolveNutsApiId() {
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
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                NVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                                    Paths.get(bootConfig.getStoreLocation(session.getWorkspace().getApiId(), NStoreLocation.CONFIG))
                                            .getParent())
                            .filter(
                                    f
                                            -> NVersion.of(f.getFileName().toString())
                                            .flatMap(v->v.getNumber(0))
                                            .flatMap(NValue::asLong).isPresent()
                                            && Files.exists(f.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME))
                            ).map(
                                    f -> NVersion.of(f.getFileName().toString()).get(session)
                            ).max(Comparator.naturalOrder()).orElse(null);
                } catch (IOException e) {
                    throw new NIOException(session, e);
                }
                NAssert.requireNonBlank(_latestVersion, "missing nuts-api version to link to", session);
                nutsApiId = session.getWorkspace().getApiId().builder().setVersion(_latestVersion).build();
            }
        }
        return nutsApiId;
    }

    public NVersion getNutsApiVersion() {
        return resolveNutsApiId().getVersion();
    }

    public Path getWorkspaceLocation() {
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return Paths.get(bootConfig.getEffectiveWorkspace());
        } else {
            return session.locations().getWorkspaceLocation().toFile();
        }
    }

    public NWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        if (workspaceBootConfig == null) {
            workspaceBootConfig = session.config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (workspaceBootConfig == null) {
                throw new NIllegalArgumentException(session, NMsg.ofCstyle("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        return workspaceBootConfig;
    }
}
