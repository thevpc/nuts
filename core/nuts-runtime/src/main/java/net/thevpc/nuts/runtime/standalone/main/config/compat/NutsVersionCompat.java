package net.thevpc.nuts.runtime.standalone.main.config.compat;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.main.config.*;

public interface NutsVersionCompat {
    NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session);

    NutsWorkspaceConfigApi parseApiConfig(NutsSession session);

    NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session);

    NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session);

    NutsWorkspaceConfigMain parseMainConfig(NutsSession session);
}
