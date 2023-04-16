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
                NDefinition apiDef = NSearchCommand.of(session)
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
                nutsApiJarPath = apiDef.getContent().orNull();
            } else {
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                nutsApiJarPath = NPath.of(bootConfig.getStoreLocation(nid, NStoreType.LIB),session);
                NLocations.of(session).getDefaultIdFilename(nid);
            }
        }
        return nutsApiJarPath;
    }

    public NPath resolveBinFolder() {
        return resolveNutsBinFolder().resolve("bin");
    }

    public NPath resolveIncFolder() {
        return resolveNutsBinFolder().resolve("inc");
    }

    public NPath resolveNutsBinFolder() {
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = session.getWorkspace().getApiId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(
                    bootConfig.getStoreLocation(apiId, NStoreType.BIN),session
            );
        } else {
            return NLocations.of(session).getStoreLocation(apiId, NStoreType.BIN);
        }
    }

    public NPath resolveNutsApiBinFolder() {
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = session.getWorkspace().getApiId().builder().setVersion(nutsVersion).build();
        apiId = NSearchCommand.of(session).addId(apiId).setLatest(true).setFailFast(true).setContent(true)
                .setDistinct(true)
                .getResultDefinitions().singleton().getId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(bootConfig.getStoreLocation(apiId, NStoreType.BIN),session);
        } else {
            return NLocations.of(session).getStoreLocation(apiId, NStoreType.BIN);
        }
    }

    public NDefinition resolveNutsApiDef() {
        return NSearchCommand.of(session).addId(resolveNutsApiId())
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
                    nutsApiId = NSearchCommand.of(session).addId(
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
                                    Paths.get(bootConfig.getStoreLocation(session.getWorkspace().getApiId(), NStoreType.CONF))
                                            .getParent())
                            .filter(
                                    f
                                            -> NVersion.of(f.getFileName().toString())
                                            .flatMap(v->v.getNumber(0))
                                            .flatMap(NLiteral::asLong).isPresent()
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
            return NLocations.of(session).getWorkspaceLocation().toFile();
        }
    }

    public NWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        if (workspaceBootConfig == null) {
            workspaceBootConfig = NConfigs.of(session).loadBootConfig(switchWorkspaceLocation, false, true);
            if (workspaceBootConfig == null) {
                throw new NIllegalArgumentException(session, NMsg.ofC("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        return workspaceBootConfig;
    }
}
