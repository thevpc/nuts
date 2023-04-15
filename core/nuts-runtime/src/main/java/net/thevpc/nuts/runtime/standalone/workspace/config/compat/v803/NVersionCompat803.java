package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NVersionCompat803 extends AbstractNVersionCompat {
    public NVersionCompat803(NSession session, NVersion apiVersion) {
        super(session, apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes, NSession session) {
        return bytes==null?null: NElements.of(session).json().parse(bytes, NWorkspaceConfigBoot.class);
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId, NSession session) {
        NPath path = NLocations.of(session).getStoreLocation(nutsApiId, NStoreLocation.CONFIG)
                .resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NWorkspaceConfigApi c = bytes==null?null: NElements.of(session)
                .setSession(session)
                .json().parse(bytes, NWorkspaceConfigApi.class);
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig(NSession session) {
        NPath path = NLocations.of(session).getStoreLocation(session.getWorkspace().getRuntimeId(), NStoreLocation.CONFIG)
                .resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NWorkspaceConfigRuntime c = bytes==null?null: NElements.of(session)
                .setSession(session)
                .json().parse(bytes, NWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId, NSession session) {
        NPath path = NLocations.of(session).getStoreLocation(nutsApiId
                , NStoreLocation.CONFIG)
                .resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NWorkspaceConfigSecurity c = bytes==null?null: NElements.of(session)
                .setSession(session)
                .json().parse(bytes, NWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId, NSession session) {
        NPath path = NLocations.of(session).getStoreLocation(nutsApiId
                , NStoreLocation.CONFIG)
                .resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NWorkspaceConfigMain c = bytes==null?null: NElements.of(session)
                .setSession(session)
                .json().parse(bytes, NWorkspaceConfigMain.class);
        return c;
    }
}
