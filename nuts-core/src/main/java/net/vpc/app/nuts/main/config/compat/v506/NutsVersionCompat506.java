package net.vpc.app.nuts.main.config.compat.v506;

import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsUserConfig;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.main.config.*;
import net.vpc.app.nuts.main.config.compat.AbstractNutsVersionCompat;
import net.vpc.app.nuts.main.config.compat.CompatUtils;

import java.util.List;

public class NutsVersionCompat506 extends AbstractNutsVersionCompat {
    public NutsVersionCompat506(NutsWorkspace ws, String apiVersion) {
        super(ws, apiVersion, 506);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return parseConfig506(bytes).toWorkspaceConfig();
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig() {
        NutsWorkspaceConfigApi cc = new NutsWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(getWorkspace().locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
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
    public NutsWorkspaceConfigRuntime parseRuntimeConfig() {
        NutsWorkspaceConfigRuntime cc = new NutsWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(getWorkspace().locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
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
    public NutsWorkspaceConfigSecurity parseSecurityConfig() {
        NutsWorkspaceConfigSecurity cc = new NutsWorkspaceConfigSecurity();
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(getWorkspace().locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
//            cc.setConfigVersion(???);
            cc.setSecure(c.isSecure());
            cc.setAuthenticationAgent(c.getAuthenticationAgent());
            List<NutsUserConfig> users = c.getUsers();
            cc.setUsers(CompatUtils.copyNutsUserConfigArray(users==null?null: users.toArray(new NutsUserConfig[0])));
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigMain parseMainConfig() {
        NutsWorkspaceConfigMain cc = new NutsWorkspaceConfigMain();
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(getWorkspace().locations().getWorkspaceLocation().resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            c.setRepositories(CompatUtils.copyNutsRepositoryRefList(c.getRepositories()));
            c.setCommandFactories(CompatUtils.copyNutsCommandAliasFactoryConfigList(c.getCommandFactories()));
            c.setEnv(CompatUtils.copyProperties(c.getEnv()));
            c.setSdk(CompatUtils.copyNutsSdkLocationList(c.getSdk()));
            c.setImports(CompatUtils.copyStringList(c.getImports()));
        }
        return cc;
    }

    private NutsWorkspaceConfigBoot506 parseConfig506(byte[] bytes) {
        return getWorkspace().formats().json().parse(bytes, NutsWorkspaceConfigBoot506.class);
    }

}
