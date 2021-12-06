package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NutsVersionCompat803 extends AbstractNutsVersionCompat {
    public NutsVersionCompat803(NutsSession ws, String apiVersion) {
        super(ws, apiVersion, 507);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return bytes==null?null:NutsElements.of(session).json().parse(bytes, NutsWorkspaceConfigBoot.class);
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsSession session) {
        NutsPath path = (session.locations().getStoreLocation(session.getWorkspace().getApiId(), NutsStoreLocation.CONFIG))
                .resolve(CoreNutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigApi c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigApi.class);
        if (c != null) {
            c.setApiVersion(getApiVersion());
        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(session.getWorkspace().getRuntimeId(), NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigRuntime c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(session.getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigSecurity c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig(NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(session.getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE)
                .build()
                , NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigMain c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigMain.class);
        return c;
    }
}
