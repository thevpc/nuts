package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.net.URL;
import java.nio.file.Path;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager {

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

    public boolean isGlobal();
}
