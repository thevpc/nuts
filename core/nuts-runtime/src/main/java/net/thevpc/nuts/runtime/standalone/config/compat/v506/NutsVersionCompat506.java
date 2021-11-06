package net.thevpc.nuts.runtime.standalone.config.compat.v506;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.*;
import net.thevpc.nuts.runtime.standalone.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;

import java.nio.file.Paths;
import java.util.List;

public class NutsVersionCompat506 extends AbstractNutsVersionCompat {
    public NutsVersionCompat506(NutsSession ws, String apiVersion) {
        super(ws, apiVersion, 506);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return parseConfig506(bytes).toWorkspaceConfig();
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsSession session) {
        NutsWorkspaceConfigApi cc = new NutsWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                session.locations().getWorkspaceLocation().toFile()
                .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session));
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
    public NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session) {
        NutsWorkspaceConfigRuntime cc = new NutsWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                session.locations().getWorkspaceLocation().toFile()
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session));
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
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session) {
        NutsWorkspaceConfigSecurity cc = new NutsWorkspaceConfigSecurity();
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                session.locations().getWorkspaceLocation().toFile()
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session));
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
    public NutsWorkspaceConfigMain parseMainConfig(NutsSession session) {
        NutsWorkspaceConfigMain cc = new NutsWorkspaceConfigMain();
        NutsWorkspaceConfigBoot506 c = parseConfig506(CompatUtils.readAllBytes(
                session.locations().getWorkspaceLocation().toFile()
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session));
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
        return NutsElements.of(getSession()).json().parse(bytes, NutsWorkspaceConfigBoot506.class);
    }

}
