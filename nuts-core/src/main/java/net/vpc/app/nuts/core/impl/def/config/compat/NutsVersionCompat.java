package net.vpc.app.nuts.core.impl.def.config.compat;

import net.vpc.app.nuts.core.impl.def.config.*;

public interface NutsVersionCompat {
    NutsWorkspaceConfigBoot parseConfig(byte[] bytes);

    NutsWorkspaceConfigApi parseApiConfig();

    NutsWorkspaceConfigRuntime parseRuntimeConfig();

    NutsWorkspaceConfigSecurity parseSecurityConfig();

    NutsWorkspaceConfigMain parseMainConfig();
}
