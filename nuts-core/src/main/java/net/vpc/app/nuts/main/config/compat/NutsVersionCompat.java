package net.vpc.app.nuts.main.config.compat;

import net.vpc.app.nuts.main.config.*;

public interface NutsVersionCompat {
    NutsWorkspaceConfigBoot parseConfig(byte[] bytes);

    NutsWorkspaceConfigApi parseApiConfig();

    NutsWorkspaceConfigRuntime parseRuntimeConfig();

    NutsWorkspaceConfigSecurity parseSecurityConfig();

    NutsWorkspaceConfigMain parseMainConfig();
}
