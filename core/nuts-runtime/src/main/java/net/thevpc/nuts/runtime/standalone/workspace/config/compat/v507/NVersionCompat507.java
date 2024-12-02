package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NVersionCompat507 extends AbstractNVersionCompat {

    public static final NVersion CONFIG_VERSION_507 = NVersion.get("5.0.7").get();

    public NVersionCompat507(NWorkspace workspace,NVersion apiVersion) {
        super(workspace,apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return bytes==null?null: NElements.of().json().parse(bytes, NWorkspaceConfigBoot.class);
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(nutsApiId, NStoreType.CONF)
                .resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigApi c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigApi.class);
        // Removed test because we would have incoherence between ApiVersion and RuntimeVersion
        // Actually I dont know was the initial need was for doing this test!
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NPath path = NWorkspace.of().getStoreLocation(workspace.getRuntimeId(), NStoreType.CONF)
                .resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigRuntime c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(nutsApiId
                , NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigSecurity c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(
                        nutsApiId, NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigMain507 c = bytes==null?null: NElements.of()
                .json().parse(bytes, NWorkspaceConfigMain507.class);
        if(c==null){
            return null;
        }
        NWorkspaceConfigMain m=new NWorkspaceConfigMain();
        m.setEnv(c.getEnv());
        m.setCommandFactories(c.getCommandFactories());
        m.setRepositories(c.getRepositories());
        m.setImports(c.getImports());
        m.setConfigVersion(CONFIG_VERSION_507);
        m.setPlatforms(c.getSdk());
        return m;
    }
}
