package net.thevpc.nuts.runtime.standalone.store;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NLocationKey;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.InstallInfoConfig;
import net.thevpc.nuts.runtime.standalone.repository.index.NanoDBNIdSerializer;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem.NanoDBInMemory;

import java.util.*;

public class NWorkspaceStoreInMemory extends AbstractNWorkspaceStore {
    private NWorkspace workspace;
    private NWorkspaceConfigBoot storedConfigBoot;
    private NWorkspaceConfigSecurity storedConfigSecurity;
    private NWorkspaceConfigMain storedConfigMain;
    private NWorkspaceConfigApi storedConfigApi;
    private Map<NVersion, NWorkspaceConfigApi> storedConfigApiByVersion = new HashMap<>();
    private Map<NVersion, NWorkspaceConfigSecurity> storedConfigSecurityByVersion = new HashMap<>();
    private Map<NVersion, NWorkspaceConfigMain> storedConfigMainByVersion = new HashMap<>();
    private Map<String, NRepositoryConfig> repoConfigMap = new HashMap<>();
    private NWorkspaceConfigRuntime storedConfigRuntime;
    private NanoDB varDB;
    private NanoDB cacheDB;
    private HashMap<NLocationKey, Object> locationKeyToObjectConfMap = new HashMap<>();
    private Map<NId, Map<NId, InstallInfoConfig>> shortToLongToInstallInfoConfigMap = new HashMap<>();
    private Map<NId, String> shortToDefaultVersionMap = new HashMap<>();

    public NWorkspaceStoreInMemory(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NanoDB cacheDB() {
        if (cacheDB == null) {
            cacheDB = new NanoDBInMemory();
            cacheDB.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer(workspace));
        }
        return cacheDB;
    }

    @Override
    public NanoDB varDB() {
        if (varDB == null) {
            varDB = new NanoDBInMemory();
            varDB.getSerializers().setSerializer(NId.class, () -> new NanoDBNIdSerializer(workspace));
        }
        return varDB;
    }

    @Override
    public boolean isValidWorkspaceFolder() {
        return storedConfigBoot != null;
    }

    @Override
    public NWorkspaceConfigBoot loadWorkspaceConfigBoot() {
        return storedConfigBoot == null ? null : storedConfigBoot.copy();
    }


    @Override
    public void saveWorkspaceConfigBoot(NWorkspaceConfigBoot value) {
        this.storedConfigBoot = value == null ? null : value.copy();
    }

    @Override
    public void saveConfigSecurity(NWorkspaceConfigSecurity value) {
        this.storedConfigSecurity = value == null ? null : value.copy();
    }

    @Override
    public void saveConfigMain(NWorkspaceConfigMain value) {
        this.storedConfigMain = value == null ? null : value.copy();
    }

    @Override
    public void saveConfigApi(NWorkspaceConfigApi value) {
        this.storedConfigApi = value == null ? null : value.copy();
    }

    @Override
    public void saveConfigRuntime(NWorkspaceConfigRuntime value) {
        this.storedConfigRuntime = value == null ? null : value.copy();
    }

    public NWorkspaceConfigBoot loadWorkspaceConfigBoot(NPath workspacePath) {
        if (Objects.equals(workspacePath, workspace.getLocation())) {
            return storedConfigBoot;
        }
        return null;
    }

    @Override
    public NWorkspaceConfigApi loadConfigApi(NId apiId) {
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        if (apiId.equals(workspace.getApiId())) {
            return storedConfigApi;
        }
        return storedConfigApiByVersion.get(apiId.getVersion());
    }

    @Override
    public NWorkspaceConfigRuntime loadConfigRuntime() {
        return storedConfigRuntime == null ? null : storedConfigRuntime.copy();
    }

    @Override
    public NWorkspaceConfigSecurity loadConfigSecurity(NId apiId) {
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        if (apiId.equals(workspace.getApiId())) {
            return storedConfigSecurity;
        }
        return storedConfigSecurityByVersion.get(apiId.getVersion());
    }

    @Override
    public NWorkspaceConfigMain loadConfigMain(NId apiId) {
        if (apiId == null) {
            apiId = workspace.getApiId();
        }
        if (apiId.equals(workspace.getApiId())) {
            return storedConfigMain;
        }
        return storedConfigMainByVersion.get(apiId.getVersion());
    }


    @Override
    public boolean saveRepoConfig(NRepository repository, NRepositoryConfig config) {
        NRepositoryConfig old = repoConfigMap.put(repository.config().getStoreLocation().toString(), config.copy());
        return old == null;
    }

    @Override
    public NRepositoryConfig loadRepoConfig(String location, String name) {
        NRepositoryConfig y = repoConfigMap.get(location);
        return y == null ? null : y.copy();
    }

    @Override
    public void saveInstallInfoConfig(InstallInfoConfig installInfoConfig) {
        Map<NId, InstallInfoConfig> longToInstallInfoConfigMap = shortToLongToInstallInfoConfigMap.computeIfAbsent(installInfoConfig.getId().getShortId(), r -> new HashMap<>());
        longToInstallInfoConfigMap.put(installInfoConfig.getId().getLongId(), installInfoConfig);
    }

    @Override
    public InstallInfoConfig loadInstallInfoConfig(NId id) {
        Map<NId, InstallInfoConfig> longToInstallInfoConfigMap = shortToLongToInstallInfoConfigMap.get(id.getShortId());
        if (longToInstallInfoConfigMap != null) {
            return longToInstallInfoConfigMap.get(id.getLongId());
        }
        return null;
    }

    @Override
    public void deleteInstallInfoConfig(NId id) {
        Map<NId, InstallInfoConfig> longToInstallInfoConfigMap = shortToLongToInstallInfoConfigMap.get(id.getShortId());
        if (longToInstallInfoConfigMap != null) {
            longToInstallInfoConfigMap.remove(id.getLongId());
        }
    }

    @Override
    public Iterator<InstallInfoConfig> searchInstalledVersions() {
        return shortToLongToInstallInfoConfigMap.values().stream().flatMap(x -> x.values().stream()).iterator();
    }

    @Override
    public Iterator<NVersion> searchInstalledVersions(NId id) {
        Map<NId, InstallInfoConfig> nIdInstallInfoConfigMap = shortToLongToInstallInfoConfigMap.get(id.getShortId());
        if (nIdInstallInfoConfigMap != null) {
            return nIdInstallInfoConfigMap.values().stream().map(x -> x.getId().getVersion()).iterator();
        }
        return Collections.emptyIterator();
    }

    @Override
    public String loadInstalledDefaultVersion(NId id) {
        return shortToDefaultVersionMap.get(id.getShortId());
    }

    @Override
    public void saveInstalledDefaultVersion(NId id) {
        shortToDefaultVersionMap.put(id.getShortId(), id.getVersion().getValue());
    }


    @Override
    public void saveLocationKey(NLocationKey k, Object value) {
        locationKeyToObjectConfMap.put(k, value);

    }

    @Override
    public <T> T loadLocationKey(NLocationKey k, Class<T> type) {
        Object obj = locationKeyToObjectConfMap.get(k);
        if (obj == null) {
            return null;
        }
        return (T) obj;
    }

}
