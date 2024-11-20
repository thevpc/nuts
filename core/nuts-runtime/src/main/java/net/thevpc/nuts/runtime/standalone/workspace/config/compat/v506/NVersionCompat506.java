package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

import java.util.List;

public class NVersionCompat506 extends AbstractNVersionCompat {
    public NVersionCompat506(NVersion apiVersion) {
        super(apiVersion, 506);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return parseConfig506(bytes).toWorkspaceConfig();
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NWorkspaceConfigApi cc = new NWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
//            cc.setConfigVersion(???);
            cc.setApiVersion(c.getApiVersion());
//            cc.setExtensionDependencies(c.getExtensionDependencies());
            cc.setRuntimeId(c.getRuntimeId());
            cc.setJavaCommand(c.getJavaCommand());
            cc.setJavaOptions(c.getJavaOptions());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NWorkspaceConfigRuntime cc = new NWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
//            cc.setConfigVersion(???);
            cc.setDependencies(c.getRuntimeDependencies());
//            cc.setApiVersion(c.getApiVersion());
//            cc.setExtensionDependencies(c.getExtensionDependencies());
            cc.setId(c.getRuntimeId());
//            cc.setJavaCommand(c.getJavaCommand());
//            cc.setJavaOptions(c.getJavaOptions());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NWorkspaceConfigSecurity cc = new NWorkspaceConfigSecurity();
        NWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
//            cc.setConfigVersion(???);
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
        NWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
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

    private NWorkspaceConfigBoot506 parseConfig506(byte[] bytes) {
        return NElements.of().json().parse(bytes, NWorkspaceConfigBoot506.class);
    }

}
