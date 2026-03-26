package net.thevpc.nuts.runtime.standalone.store;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryConfig;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.InstallInfoConfig;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.xtra.nanodb.NanoDB;

import java.util.Iterator;
import java.util.function.Supplier;

public interface NWorkspaceStore {
    boolean isValidWorkspaceFolder();

    NWorkspaceConfigBoot loadWorkspaceConfigBoot();

    NWorkspaceConfigBoot loadWorkspaceConfigBoot(NPath path);

    void saveWorkspaceConfigBoot(NWorkspaceConfigBoot value);

    void saveConfigSecurity(NWorkspaceConfigSecurity value);

    void saveConfigMain(NWorkspaceConfigMain value);

    void saveConfigApi(NWorkspaceConfigApi value);

    void saveConfigRuntime(NWorkspaceConfigRuntime value);

    NWorkspaceConfigApi loadConfigApi(NId nutsApiId);

    NWorkspaceConfigRuntime loadConfigRuntime();

    NWorkspaceConfigSecurity loadConfigSecurity(NId nutsApiId);

    NWorkspaceConfigMain loadConfigMain(NId apiId);

    NanoDB cacheDB();

    NanoDB varDB();

    boolean saveRepoConfig(NRepository repository, NRepositoryConfig config);

    NRepositoryConfig loadRepoConfig(String location, String name);


    void saveLocationKey(NStoreKey k, Object value);

    <T> T loadLocationKey(NStoreKey k, Class<T> type);

    boolean deleteLocationKey(NStoreKey k);

    <T> T supplyWithCache(NStoreKey k, Class<T> type, Supplier<T> supplier);

    void saveInstallInfoConfig(InstallInfoConfig installInfoConfig);

    void deleteInstallInfoConfig(NId id);

    Iterator<InstallInfoConfig> searchInstalledVersions();

    Iterator<NVersion> searchInstalledVersions(NId id);

    InstallInfoConfig loadInstallInfoConfig(NId id);

    String loadInstalledDefaultVersion(NId id);

    void saveInstalledDefaultVersion(NId id);

}
