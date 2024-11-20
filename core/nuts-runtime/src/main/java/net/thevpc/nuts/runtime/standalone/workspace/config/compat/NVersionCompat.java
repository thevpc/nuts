package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;

public interface NVersionCompat {
    NWorkspaceConfigBoot parseConfig(byte[] bytes);

    NWorkspaceConfigApi parseApiConfig(NId nutsApiId);

    NWorkspaceConfigRuntime parseRuntimeConfig();

    NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId);

    NWorkspaceConfigMain parseMainConfig(NId nutsApiId);
}
