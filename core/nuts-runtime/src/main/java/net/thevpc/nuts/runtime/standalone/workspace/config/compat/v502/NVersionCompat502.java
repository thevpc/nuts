package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.NUserConfig;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

import java.util.List;

public class NVersionCompat502 extends AbstractNVersionCompat {
    public NVersionCompat502(NWorkspace workspace,NVersion apiVersion) {
        super(workspace,apiVersion, 502);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return parseConfig502(bytes).toWorkspaceConfig();
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NWorkspaceConfigApi cc = new NWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NWorkspace.get().getWorkspaceLocation().toPath().get()
                .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setApiVersion(c.getBootApiVersion());
            cc.setRuntimeId(c.getBootRuntime());
            cc.setJavaCommand(c.getBootJavaCommand());
            cc.setJavaOptions(c.getBootJavaOptions());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NWorkspaceConfigRuntime cc = new NWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NWorkspace.get().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setDependencies(c.getBootRuntimeDependencies());
            cc.setId(c.getBootRuntime());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NWorkspaceConfigSecurity cc = new NWorkspaceConfigSecurity();
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NWorkspace.get().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setSecure(c.isSecure());
            cc.setAuthenticationAgent(c.getAuthenticationAgent());
            List<NUserConfig> users = c.getUsers();
            cc.setUsers(CompatUtils.copyNutsUserConfigArray(users==null?null: users.toArray(new NUserConfig[0])));
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NWorkspaceConfigMain cc = new NWorkspaceConfigMain();
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NWorkspace.get().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            c.setRepositories(CompatUtils.copyNutsRepositoryRefList(c.getRepositories()));
            c.setCommandFactories(CompatUtils.copyNutsCommandAliasFactoryConfigList(c.getCommandFactories()));
            c.setEnv(CompatUtils.copyProperties(c.getEnv()));
            c.setSdk(CompatUtils.copyNutsSdkLocationList(c.getSdk()));
            c.setImports(CompatUtils.copyStringList(c.getImports()));
        }
        return cc;
    }

    private NWorkspaceConfigBoot502 parseConfig502(byte[] bytes) {
        return NElements.of().json().parse(bytes, NWorkspaceConfigBoot502.class);
    }


}
