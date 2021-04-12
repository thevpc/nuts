package net.thevpc.nuts.runtime.standalone.config.compat.v507;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.*;
import net.thevpc.nuts.runtime.core.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class NutsVersionCompat507 extends AbstractNutsVersionCompat {
    public NutsVersionCompat507(NutsWorkspace ws, String apiVersion) {
        super(ws, apiVersion, 507);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return parseConfig507(bytes,session);//.toWorkspaceConfig();
    }

    public NutsWorkspaceConfigBoot parseConfig507(byte[] bytes, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        return bytes==null?null:ws.formats().element().setSession(session).setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigBoot.class);
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        Path path = Paths.get(ws.locations().getStoreLocation(ws.getApiId(), NutsStoreLocation.CONFIG))
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigApi c = bytes==null?null:ws.formats().element()
                .setSession(session)
                .setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigApi.class);
        if (c != null) {
            c.setApiVersion(getApiVersion());
        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        Path path = Paths.get(ws.locations().getStoreLocation(ws.getRuntimeId(), NutsStoreLocation.CONFIG))
                .resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigRuntime c = bytes==null?null:ws.formats().element()
                .setSession(session)
                .setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigRuntime.class);
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        Path path = Paths.get(ws.locations().getStoreLocation(ws.getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG))
                .resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigSecurity c = bytes==null?null:ws.formats().element()
                .setSession(session)
                .setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig(NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        Path path = Paths.get(ws.locations().getStoreLocation(ws.getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE)
                .build()
                , NutsStoreLocation.CONFIG))
                .resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigMain c = bytes==null?null:ws.formats().element()
                .setSession(session)
                .setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigMain.class);
        return c;
    }
}
