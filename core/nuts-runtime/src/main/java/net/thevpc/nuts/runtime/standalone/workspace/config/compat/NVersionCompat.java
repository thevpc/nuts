package net.thevpc.nuts.runtime.standalone.workspace.config.compat;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NVersion;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502.NVersionCompat502;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506.NVersionCompat506;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507.NVersionCompat507;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803.NVersionCompat803;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v805.NVersionCompat805;

public interface NVersionCompat {
    static NVersionCompat of(NVersion apiVersion) {
        int buildNumber = CoreNUtils.getApiVersionOrdinalNumber(apiVersion);
        if (buildNumber >= 805) {
            return new NVersionCompat805(apiVersion);
        }else if (buildNumber >= 803) {
            return new NVersionCompat803(apiVersion);
        } else if (buildNumber >= 507) {
            return new NVersionCompat507(apiVersion);
        } else if (buildNumber >= 506) {
            return new NVersionCompat506(apiVersion);
        } else {
            return new NVersionCompat502(apiVersion);
        }
    }
    NWorkspaceConfigBoot parseConfig(byte[] bytes);

    NWorkspaceConfigApi parseApiConfig(NId nutsApiId);

    NWorkspaceConfigRuntime parseRuntimeConfig();

    NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId);

    NWorkspaceConfigMain parseMainConfig(NId nutsApiId);
}
