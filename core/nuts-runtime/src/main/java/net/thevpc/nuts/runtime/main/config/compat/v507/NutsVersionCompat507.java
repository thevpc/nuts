package net.thevpc.nuts.runtime.main.config.compat.v507;

import net.thevpc.nuts.NutsConstants;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.main.config.*;
import net.thevpc.nuts.runtime.CoreNutsConstants;
import net.thevpc.nuts.runtime.main.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.main.config.compat.CompatUtils;

import java.nio.file.Path;

public class NutsVersionCompat507 extends AbstractNutsVersionCompat {
    public NutsVersionCompat507(NutsWorkspace ws, String apiVersion) {
        super(ws, apiVersion, 507);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return parseConfig507(bytes);//.toWorkspaceConfig();
    }

    public NutsWorkspaceConfigBoot parseConfig507(byte[] bytes) {
        return bytes==null?null:getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigBoot.class);
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig() {
        Path path = getWorkspace().locations().getStoreLocation(getWorkspace().getApiId(), NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Files.WORKSPACE_API_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigApi c = bytes==null?null:getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigApi.class);
        if (c != null) {
            c.setApiVersion(getApiVersion());
        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig() {
        Path path = getWorkspace().locations().getStoreLocation(getWorkspace().getRuntimeId(), NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Files.WORKSPACE_RUNTIME_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigRuntime c = bytes==null?null:getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigRuntime.class);
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig() {
        Path path = getWorkspace().locations().getStoreLocation(getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE).build(), NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigSecurity c = bytes==null?null:getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig() {
        Path path = getWorkspace().locations().getStoreLocation(getWorkspace().getApiId()
                .builder().setVersion(NutsConstants.Versions.RELEASE)
                .build()
                , NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NutsWorkspaceConfigMain c = bytes==null?null:getWorkspace().formats().element().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigMain.class);
        return c;
    }
}
