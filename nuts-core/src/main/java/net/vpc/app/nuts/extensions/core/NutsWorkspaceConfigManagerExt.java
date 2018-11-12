package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsWorkspaceConfig;
import net.vpc.app.nuts.NutsWorkspaceConfigManager;

import java.io.PrintStream;

public interface NutsWorkspaceConfigManagerExt extends NutsWorkspaceConfigManager{
    NutsWorkspaceConfig getConfig();

}
