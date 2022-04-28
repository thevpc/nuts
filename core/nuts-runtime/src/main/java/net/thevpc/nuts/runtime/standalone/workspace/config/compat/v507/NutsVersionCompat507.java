package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NutsVersionCompat507 extends AbstractNutsVersionCompat {

    public static final NutsVersion CONFIG_VERSION_507 = NutsVersion.of("5.0.7").get();

    public NutsVersionCompat507(NutsSession ws, NutsVersion apiVersion) {
        super(ws, apiVersion, 507);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return bytes==null?null:NutsElements.of(session).json().parse(bytes, NutsWorkspaceConfigBoot.class);
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsId nutsApiId, NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(nutsApiId, NutsStoreLocation.CONFIG)
                .resolve(NutsConstants.Files.API_BOOT_CONFIG_FILE_NAME);
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
                .resolve(NutsConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigRuntime c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsId nutsApiId, NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(nutsApiId
                , NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigSecurity c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig(NutsId nutsApiId, NutsSession session) {
        NutsPath path = session.locations().getStoreLocation(
                        nutsApiId, NutsStoreLocation.CONFIG)
                .resolve(CoreNutsConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path,session);
        NutsWorkspaceConfigMain507 c = bytes==null?null:NutsElements.of(session)
                .setSession(session)
                .json().parse(bytes, NutsWorkspaceConfigMain507.class);
        if(c==null){
            return null;
        }
        NutsWorkspaceConfigMain m=new NutsWorkspaceConfigMain();
        m.setEnv(c.getEnv());
        m.setCommandFactories(c.getCommandFactories());
        m.setRepositories(c.getRepositories());
        m.setImports(c.getImports());
        m.setConfigVersion(CONFIG_VERSION_507);
        m.setPlatforms(c.getSdk());
        return m;
    }
}
