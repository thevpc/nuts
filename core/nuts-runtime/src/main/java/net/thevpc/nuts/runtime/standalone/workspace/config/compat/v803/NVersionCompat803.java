package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NVersionCompat803 extends AbstractNVersionCompat {
    public NVersionCompat803(NVersion apiVersion) {
        super(apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return bytes==null?null: NElements.of().json().parse(bytes, NWorkspaceConfigBoot.class);
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NPath path = NLocations.of().getStoreLocation(nutsApiId, NStoreType.CONF)
                .resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigApi c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigApi.class);
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NPath path = NLocations.of().getStoreLocation(NWorkspace.get().getRuntimeId(), NStoreType.CONF)
                .resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigRuntime c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NPath path = NLocations.of().getStoreLocation(nutsApiId
                , NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigSecurity c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NPath path = NLocations.of().getStoreLocation(nutsApiId
                , NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigMain c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigMain.class);
        return c;
    }
}
