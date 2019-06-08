package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.*;

import java.net.URL;
import java.nio.file.Path;
import net.vpc.app.nuts.core.DefaultNutsBootContext;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

    public static NutsWorkspaceConfigManagerExt of(NutsWorkspaceConfigManager wsc) {
        return (NutsWorkspaceConfigManagerExt) wsc;
    }

    void setStartCreateTimeMillis(long currentTimeMillis);

    void onInitializeWorkspace(NutsWorkspaceOptions options, DefaultNutsBootContext defaultNutsBootContext, DefaultNutsBootContext defaultNutsBootContext1, URL[] bootClassWorldURLs, ClassLoader classLoader);

    void setConfig(NutsWorkspaceConfig config);

    void setEndCreateTimeMillis(long currentTimeMillis);

    boolean isConfigurationChanged();

    Path getConfigFile();

    boolean load();

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
    
    void removeAllRepositories();
    
    Path getRepositoriesRoot();
    
//    NutsRepository wireRepository(NutsRepository repository);

    void setExcludedRepositories(String[] excludedRepositories);
}
