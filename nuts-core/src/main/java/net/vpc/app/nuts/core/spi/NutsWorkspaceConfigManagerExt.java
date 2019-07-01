package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.*;

import java.net.URL;
import java.nio.file.Path;
import net.vpc.app.nuts.core.NutsBootConfig;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

    public static NutsWorkspaceConfigManagerExt of(NutsWorkspaceConfigManager wsc) {
        return (NutsWorkspaceConfigManagerExt) wsc;
    }

    void setStartCreateTimeMillis(long currentTimeMillis);

    void onInitializeWorkspace(Path workspaceLocation,NutsWorkspaceOptions options, URL[] bootClassWorldURLs, ClassLoader classLoader);
    
    void setRunningContext(NutsBootContext runningContext);

    void setConfig(NutsWorkspaceConfig config, NutsSession session);

    void setEndCreateTimeMillis(long currentTimeMillis);

    boolean isConfigurationChanged();

    Path getConfigFile();

    boolean load(NutsSession session);

    void setBootApiVersion(String value);

    void setBootRuntime(String value);

    void setBootRuntimeDependencies(String value);

    void setBootRepositories(String value);

    NutsUserConfig getUser(String userId);

    NutsUserConfig[] getUsers();

    void setUser(NutsUserConfig config);

    void removeUser(String userId);

    void setUsers(NutsUserConfig[] users);

    void setSecure(boolean secure);

    void fireConfigurationChanged();

    NutsWorkspaceConfig getStoredConfig();

    NutsWorkspace getWorkspace();

    void removeAllRepositories(NutsRemoveOptions options);

    Path getRepositoriesRoot();

//    NutsRepository wireRepository(NutsRepository repository);
    void setExcludedRepositories(String[] excludedRepositories);

    /**
     * update workspace boot configuration
     *
     * @param other
     */
    void setBootConfig(NutsBootConfig other);

    /**
     * return a copy of workspace boot configuration
     *
     * @return a copy of workspace boot configuration
     */
    NutsBootConfig getBootConfig();
}
