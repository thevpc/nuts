package net.thevpc.nuts.runtime.main.config.compat;

import net.thevpc.nuts.runtime.main.config.*;

public interface NutsVersionCompat {
    NutsWorkspaceConfigBoot parseConfig(byte[] bytes);

    NutsWorkspaceConfigApi parseApiConfig();

    NutsWorkspaceConfigRuntime parseRuntimeConfig();

    NutsWorkspaceConfigSecurity parseSecurityConfig();

    NutsWorkspaceConfigMain parseMainConfig();
}
