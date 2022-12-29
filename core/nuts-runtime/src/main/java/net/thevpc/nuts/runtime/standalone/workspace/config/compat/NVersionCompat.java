package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;

public interface NVersionCompat {
    NWorkspaceConfigBoot parseConfig(byte[] bytes, NSession session);

    NWorkspaceConfigApi parseApiConfig(NId nutsApiId, NSession session);

    NWorkspaceConfigRuntime parseRuntimeConfig(NSession session);

    NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId, NSession session);

    NWorkspaceConfigMain parseMainConfig(NId nutsApiId, NSession session);
}
