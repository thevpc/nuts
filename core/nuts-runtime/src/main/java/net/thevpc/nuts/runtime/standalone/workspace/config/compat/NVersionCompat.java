package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NVersion;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502.NVersionCompat502;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506.NVersionCompat506;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507.NVersionCompat507;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803.NVersionCompat803;

public interface NVersionCompat {
    static NVersionCompat of(NVersion apiVersion, NWorkspace workspace) {
        int buildNumber = CoreNUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber >= 803) {
            return new NVersionCompat803(workspace, apiVersion);
        } else if (buildNumber >= 507) {
            return new NVersionCompat507(workspace, apiVersion);
        } else if (buildNumber >= 506) {
            return new NVersionCompat506(workspace, apiVersion);
        } else {
            return new NVersionCompat502(workspace, apiVersion);
        }
    }
    NWorkspaceConfigBoot parseConfig(byte[] bytes);

    NWorkspaceConfigApi parseApiConfig(NId nutsApiId);

    NWorkspaceConfigRuntime parseRuntimeConfig();

    NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId);

    NWorkspaceConfigMain parseMainConfig(NId nutsApiId);
}
