package net.thevpc.nuts.runtime.core.config;

import net.thevpc.nuts.*;

import net.thevpc.nuts.runtime.standalone.config.*;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

    static NutsWorkspaceConfigManagerExt of(NutsWorkspaceConfigManager wsc) {
        return (NutsWorkspaceConfigManagerExt) wsc;
    }

    DefaultNutsWorkspaceConfigModel getModel();
//    String getApiVersion();
//
//    NutsId getApiId();
//
//    NutsId getRuntimeId();
//
//    DefaultNutsWorkspaceCurrentConfig current();
//
//    void setStartCreateTimeMillis(long currentTimeMillis);
//
//    void setCurrentConfig(DefaultNutsWorkspaceCurrentConfig runningContext);
//
//    void setConfigBoot(NutsWorkspaceConfigBoot config, NutsSession options);
//
//    void setConfigApi(NutsWorkspaceConfigApi config, NutsSession options);
//
//    void setConfigRuntime(NutsWorkspaceConfigRuntime config, NutsSession options);
//
//    void setConfigSecurity(NutsWorkspaceConfigSecurity config, NutsSession options);
//
//    void setConfigMain(NutsWorkspaceConfigMain config, NutsSession options);
//
//    void setEndCreateTimeMillis(long currentTimeMillis);
//
//    void prepareBootApi(NutsId apiId, NutsId runtimeId, boolean force, NutsSession session);
//
//    void prepareBootRuntime(NutsId id, boolean force,NutsSession session);
//
//    void prepareBootExtension(NutsId id, boolean force,NutsSession session);
//
//    void prepareBoot(boolean force,NutsSession session);
//
//    boolean isConfigurationChanged();
//
//    boolean loadWorkspace(NutsSession session);
//
//    void setBootApiVersion(String value, NutsSession options);
//
//    void setBootRuntimeId(String value, NutsSession options);
//
//    void setBootRuntimeDependencies(String value, NutsSession options);
//
//    void setBootRepositories(String value, NutsSession options);
//
//    NutsUserConfig getUser(String userId, NutsSession session);
//
//    NutsUserConfig[] getUsers(NutsSession session);
//
//    void setUser(NutsUserConfig config, NutsSession options);
//
//    void removeUser(String userId, NutsRemoveOptions options);
//
//    void setSecure(boolean secure, NutsSession options);
//
//    void fireConfigurationChanged(String configName, NutsSession session, ConfigEventType t);
//
//    NutsWorkspaceConfigApi getStoredConfigApi();
//
//    NutsWorkspaceConfigBoot getStoredConfigBoot();
//
//    NutsWorkspaceConfigSecurity getStoredConfigSecurity();
//
//    NutsWorkspaceConfigMain getStoredConfigMain();
//
//
//    NutsWorkspace getWorkspace();
//
//    String getRepositoriesRoot();
//    String getTempRepositoriesRoot();
//
//    boolean isValidWorkspaceFolder();
//
//    NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent, NutsSession session);
//
////    void setExcludedRepositories(String[] excludedRepositories, NutsSession options);
//
//    void setUsers(NutsUserConfig[] users, NutsSession options);
//
//    NutsWorkspaceConfigRuntime getStoredConfigRuntime();
//
//    NutsId createSdkId(String type, String version);
//
//    void onExtensionsPrepared(NutsSession session);
//
//    NutsSdkManager sdks();
//
//    NutsImportManager imports();

}
