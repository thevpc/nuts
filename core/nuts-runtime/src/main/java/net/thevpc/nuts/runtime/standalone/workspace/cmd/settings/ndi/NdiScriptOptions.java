package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

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

    private NId nutsApiId;
    private NPath nutsApiJarPath;
    private NPath nutsAppJarPath;
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
                NDefinition apiDef = NSearchCmd.of()
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().findFirst().get();
                nutsApiJarPath = apiDef.getContent().orNull();
            } else {
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                nutsApiJarPath = NPath.of(bootConfig.getStoreLocation(nid, NStoreType.LIB));
                NLocations.of().getDefaultIdFilename(nid);
            }
        }
        return nutsApiJarPath;
    }
    public NPath resolveNutsAppJarPath() {
        if (nutsAppJarPath == null) {
            NId nid = resolveNutsAppId();
            if (getLauncher().getSwitchWorkspaceLocation() == null) {
                NDefinition appDef = NSearchCmd.of()
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().findFirst().get();
                nutsAppJarPath = appDef.getContent().orNull();
            } else {
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                nutsAppJarPath = NPath.of(bootConfig.getStoreLocation(nid, NStoreType.LIB));
                NLocations.of().getDefaultIdFilename(nid);
            }
        }
        return nutsAppJarPath;
    }

    public NPath resolveBinFolder() {
        return resolveNutsBinFolder().resolve("bin");
    }

    public NPath resolveIncFolder() {
        return resolveNutsBinFolder().resolve("inc");
    }

    public NPath resolveNutsBinFolder() {
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = NWorkspace.of().get().getApiId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(
                    bootConfig.getStoreLocation(apiId, NStoreType.BIN)
            );
        } else {
            return NLocations.of().getStoreLocation(apiId, NStoreType.BIN);
        }
    }

    public NPath resolveNutsApiBinFolder() {
        NWorkspaceBootConfig bootConfig = null;
        NId apiId = NWorkspace.of().get().getApiId().builder().setVersion(nutsVersion).build();
        apiId = NSearchCmd.of().addId(apiId).latest().failFast().content()
                .distinct()
                .getResultDefinitions()
                .findSingleton().get().getId();
        if (getLauncher().getSwitchWorkspaceLocation() != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
            return NPath.of(bootConfig.getStoreLocation(apiId, NStoreType.BIN));
        } else {
            return NLocations.of().getStoreLocation(apiId, NStoreType.BIN);
        }
    }

    public NDefinition resolveNutsApiDef() {
        return NSearchCmd.of().addId(resolveNutsApiId())
                .latest()
                .content()
                .failFast()
                .distinct()
                .getResultDefinitions()
                .findSingleton().get();
    }

    public NId resolveNutsAppId() {
        NId r = resolveNutsApiId();
        return r.builder().setArtifactId("nuts").build();
    }

    public NId resolveNutsApiId() {
        if (nutsApiId == null) {
            NWorkspace workspace = NWorkspace.of().get();
            if (getLauncher().getSwitchWorkspaceLocation() == null) {
                if (nutsVersion == null) {
                    nutsApiId = workspace.getApiId();
                } else {
                    nutsApiId = NSearchCmd.of().addId(
                                    workspace.getApiId().builder().setVersion(nutsVersion).build()
                            ).setLatest(true)
                            .setDistinct(true)
                            .getResultIds()
                            .findSingleton().get();
                }
            } else {
                NWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(getLauncher().getSwitchWorkspaceLocation());
                NVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                                    Paths.get(bootConfig.getStoreLocation(workspace.getApiId(), NStoreType.CONF))
                                            .getParent())
                            .filter(
                                    f
                                            -> NVersion.of(f.getFileName().toString())
                                            .flatMap(v->v.getNumber(0))
                                            .flatMap(NLiteral::asLong).isPresent()
                                            && Files.exists(f.resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME))
                            ).map(
                                    f -> NVersion.of(f.getFileName().toString()).get()
                            ).max(Comparator.naturalOrder()).orElse(null);
                } catch (IOException e) {
                    throw new NIOException(e);
                }
                NAssert.requireNonBlank(_latestVersion, "missing nuts-api version to link to");
                nutsApiId = workspace.getApiId().builder().setVersion(_latestVersion).build();
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
            return NLocations.of().getWorkspaceLocation().toPath().get();
        }
    }

    public NWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        if (workspaceBootConfig == null) {
            workspaceBootConfig = NConfigs.of().loadBootConfig(switchWorkspaceLocation, false, true);
            if (workspaceBootConfig == null) {
                throw new NIllegalArgumentException(NMsg.ofC("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        return workspaceBootConfig;
    }
}
