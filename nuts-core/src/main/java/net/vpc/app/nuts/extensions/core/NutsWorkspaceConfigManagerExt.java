package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsWorkspaceConfig;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager{
    NutsWorkspaceConfig getConfig();
}
