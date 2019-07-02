package net.vpc.app.nuts.core.spi;

import net.vpc.app.nuts.*;

import java.net.URL;
import java.nio.file.Path;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

    public static NutsWorkspaceConfigManagerExt of(NutsWorkspaceConfigManager wsc) {
        return (NutsWorkspaceConfigManagerExt) wsc;
    }

    void setStartCreateTimeMillis(long currentTimeMillis);

    void onInitializeWorkspace(Path workspaceLocation,NutsWorkspaceOptions options, URL[] bootClassWorldURLs, ClassLoader classLoader);
    
    void setCurrentConfig(NutsWorkspaceCurrentConfig runningContext);

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

    void setExcludedRepositories(String[] excludedRepositories);

    boolean isValidWorkspaceFolder();

    NutsAuthenticationAgent createAuthenticationAgent(String authenticationAgent);

//    char[] decryptString(char[] input);
//
//    byte[] decryptString(byte[] input);
//
//    char[] encryptString(char[] input);
//
//    byte[] encryptString(byte[] input);
}
