package net.thevpc.nuts.runtime.core.config;

import net.thevpc.nuts.*;

import java.nio.file.Path;

import net.thevpc.nuts.runtime.main.config.*;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

    static NutsWorkspaceConfigManagerExt of(NutsWorkspaceConfigManager wsc) {
        return (NutsWorkspaceConfigManagerExt) wsc;
    }

    String getApiVersion();

    NutsId getApiId();

    NutsId getRuntimeId();

    DefaultNutsWorkspaceCurrentConfig current();

    void setStartCreateTimeMillis(long currentTimeMillis);

    void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig runningContext);

    void setConfigBoot(NutsWorkspaceConfigBoot config, NutsUpdateOptions options);

    void setConfigApi(NutsWorkspaceConfigApi config, NutsUpdateOptions options);

    void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsUpdateOptions options);

    void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsUpdateOptions options);

    void setConfigMain(NutsWorkspaceConfigMain config, NutsUpdateOptions options);

    void setEndCreateTimeMillis(long currentTimeMillis);

    void prepareBootApi(NutsId apiId, NutsId runtimeId, boolean force, NutsSession session);

    void prepareBootRuntime(NutsId id, boolean force,NutsSession session);

    void prepareBootExtension(NutsId id, boolean force,NutsSession session);

    void prepareBoot(boolean force,NutsSession session);

    boolean isConfigurationChanged();

    boolean loadWorkspace(NutsSession session);

    void setBootApiVersion(String value, NutsUpdateOptions options);

    void setBootRuntimeId(String value, NutsUpdateOptions options);

    void setBootRuntimeDependencies(String value, NutsUpdateOptions options);

    void setBootRepositories(String value, NutsUpdateOptions options);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    void setUser(NutsUserConfig config, NutsUpdateOptions options);

    void removeUser(String userId, NutsRemoveOptions options);

    void setSecure(boolean secure, NutsUpdateOptions options);

    void fireConfigurationChanged(String configName, NutsSession session, DefaultNutsWorkspaceConfigManager.ConfigEventType t);

    NutsWorkspaceConfigApi getStoredConfigApi();

    NutsWorkspaceConfigBoot getStoredConfigBoot();

    NutsWorkspaceConfigSecurity getStoredConfigSecurity();

    NutsWorkspaceConfigMain getStoredConfigMain();


    NutsWorkspace getWorkspace();

    Path getRepositoriesRoot();

    boolean isValidWorkspaceFolder();

    NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session);

    void setExcludedRepositories(String[] excludedRepositories, NutsUpdateOptions options);

    void setUsers(NutsUserConfig[] users, NutsUpdateOptions options);

    NutsWorkspaceConfigRuntime getStoredConfigRuntime();

    NutsId createSdkId(String type, String version);

    void onExtensionsPrepared(NutsSession session);

    NutsSdkManager sdks();

    NutsImportManager imports();

    NutsCommandAliasManager aliases();

    NutsWorkspaceEnvManager env();
}
