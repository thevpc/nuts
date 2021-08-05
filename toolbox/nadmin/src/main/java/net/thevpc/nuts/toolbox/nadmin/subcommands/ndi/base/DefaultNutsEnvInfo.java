package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class DefaultNutsEnvInfo implements NutsEnvInfo {
    private String nutsVersion;
    private String switchWorkspaceLocation;
    private NutsSession session;

    private NutsId nutsId;
    private Path nutsJarPath;
    private NutsWorkspaceBootConfig bootConfig;

    public DefaultNutsEnvInfo(String nutsVersion, String switchWorkspaceLocation, NutsSession session) {
        this.nutsVersion = nutsVersion;
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        this.session = session;
    }

    @Override
    public Path getNutsJarPath() {
        if (nutsJarPath == null) {
            NutsId nid = getNutsApiId();
            NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            if (switchWorkspaceLocation == null) {
                NutsDefinition apiDef = session.getWorkspace().search()
                        .addId(nid).setOptional(false).setLatest(true).setContent(true).getResultDefinitions().required();
                nutsJarPath = apiDef.getPath();
            } else {
                nutsJarPath = Paths.get(bootConfig.getStoreLocation(nid, NutsStoreLocation.LIB),
                        session.getWorkspace().locations().getDefaultIdFilename(nid));
            }
        }
        return nutsJarPath;
    }

    public Path getBinFolder() {
        return getNadminAppsFolder().resolve("bin");
    }

    public Path getIncFolder() {
        return getNadminAppsFolder().resolve("inc");
    }

    public Path getNadminAppsFolder() {
        NutsWorkspace ws = session.getWorkspace();
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId nadminId = session.getAppId();
        if (switchWorkspaceLocation != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getStoreLocation(nadminId, NutsStoreLocation.APPS));
        } else {
            return Paths.get(ws.locations().getStoreLocation(nadminId, NutsStoreLocation.APPS));
        }
    }

    @Override
    public Path getNutsApiAppsFolder() {
        NutsWorkspace ws = session.getWorkspace();
        NutsWorkspaceBootConfig bootConfig = null;
        NutsId apiId = ws.getApiId().builder().setVersion(nutsVersion).build();
        apiId = ws.search().addId(apiId).setLatest(true).setFailFast(true).setContent(true).getResultDefinitions().singleton().getId();
        if (switchWorkspaceLocation != null) {
            bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getStoreLocation(apiId, NutsStoreLocation.APPS));
        } else {
            return Paths.get(ws.locations().getStoreLocation(apiId, NutsStoreLocation.APPS));
        }
    }

    @Override
    public NutsDefinition getNutsApiDef() {
        return session.getWorkspace().search().addId(getNutsApiId()).setLatest(true).getResultDefinitions().singleton();
    }

    public NutsId getNutsApiId() {
        if (nutsId == null) {
            if (switchWorkspaceLocation == null) {
                if (nutsVersion == null) {
                    nutsId = session.getWorkspace().getApiId();
                } else {
                    nutsId = session.getWorkspace().search().addId(
                            session.getWorkspace().getApiId().builder().setVersion(nutsVersion).build()
                    ).setLatest(true).getResultIds().singleton();
                }
            } else {
                NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
                NutsVersion _latestVersion = null;
                try {
                    _latestVersion = Files.list(
                                    Paths.get(bootConfig.getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG))
                                            .getParent())
                            .filter(
                                    f
                                            -> session.getWorkspace().version().parse(f.getFileName().toString()).getNumber(0, -1) != -1
                                            && Files.exists(f.resolve("nuts-api-config.json"))
                            ).map(
                                    f -> session.getWorkspace().version().parse(f.getFileName().toString())
                            ).sorted(Comparator.reverseOrder()).findFirst().orElse(null);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
                if (_latestVersion == null) {
                    throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("missing nuts-api version to link to"));
                }
                nutsId = session.getWorkspace().getApiId().builder().setVersion(_latestVersion).build();
            }
        }
        return nutsId;
    }

    @Override
    public NutsVersion getNutsApiVersion() {
        return getNutsApiId().getVersion();
    }

    @Override
    public Path getWorkspaceLocation() {
        if (switchWorkspaceLocation != null) {
            NutsWorkspaceBootConfig bootConfig = loadSwitchWorkspaceLocationConfig(switchWorkspaceLocation);
            return Paths.get(bootConfig.getEffectiveWorkspace());
        } else {
            return Paths.get(session.getWorkspace().locations().getWorkspaceLocation());
        }
    }

    public NutsWorkspaceBootConfig loadSwitchWorkspaceLocationConfig(String switchWorkspaceLocation) {
        if (bootConfig == null) {
            bootConfig = session.getWorkspace().config().loadBootConfig(switchWorkspaceLocation, false, true);
            if (bootConfig == null) {
                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("invalid workspace: %s", switchWorkspaceLocation));
            }
        }
        return bootConfig;
    }
}
