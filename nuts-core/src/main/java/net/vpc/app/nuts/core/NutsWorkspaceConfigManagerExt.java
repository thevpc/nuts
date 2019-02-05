package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.net.URL;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager{

    void setStartCreateTimeMillis(long currentTimeMillis);

    void onInitializeWorkspace(NutsWorkspaceOptions options, DefaultNutsBootContext defaultNutsBootContext, DefaultNutsBootContext defaultNutsBootContext1, URL[] bootClassWorldURLs, ClassLoader classLoader);

    void setConfig(NutsWorkspaceConfig config);

    NutsRepositoryLocation[] getRepositories();

    void setEndCreateTimeMillis(long currentTimeMillis);

    boolean isConfigurationChanged();

    File getConfigFile();

    boolean load();

    NutsWorkspaceCommandFactoryConfig[] getCommandFactories();

    ClassLoader getBootClassLoader();

    void setBootApiVersion(String value);

    void setBootRuntime(String value);

    void setBootRuntimeDependencies(String value);

    void setBootRepositories(String value);
}
