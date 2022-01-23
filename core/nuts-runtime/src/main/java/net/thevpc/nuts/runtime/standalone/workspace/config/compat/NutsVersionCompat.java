package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;

public interface NutsVersionCompat {
    NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session);

    NutsWorkspaceConfigApi parseApiConfig(NutsId nutsApiId, NutsSession session);

    NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session);

    NutsWorkspaceConfigSecurity parseSecurityConfig(NutsId nutsApiId, NutsSession session);

    NutsWorkspaceConfigMain parseMainConfig(NutsId nutsApiId, NutsSession session);
}
