package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.File;
import java.net.URL;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager{

    NutsWorkspaceConfig getConfig();

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
}
