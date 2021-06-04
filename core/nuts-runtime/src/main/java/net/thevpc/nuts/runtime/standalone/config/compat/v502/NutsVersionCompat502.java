package net.thevpc.nuts.runtime.standalone.config.compat.v502;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.*;
import net.thevpc.nuts.runtime.standalone.config.compat.AbstractNutsVersionCompat;
import net.thevpc.nuts.runtime.standalone.config.compat.CompatUtils;

import java.nio.file.Paths;
import java.util.List;

public class NutsVersionCompat502 extends AbstractNutsVersionCompat {
    public NutsVersionCompat502(NutsWorkspace ws,String apiVersion) {
        super(ws,apiVersion, 502);
    }

    @Override
    public NutsWorkspaceConfigBoot parseConfig(byte[] bytes, NutsSession session) {
        return parseConfig502(bytes).toWorkspaceConfig();
    }

    @Override
    public NutsWorkspaceConfigApi parseApiConfig(NutsSession session) {
        NutsWorkspaceConfigApi cc = new NutsWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                Paths.get(getWorkspace().locations().getWorkspaceLocation())
                .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setApiVersion(c.getBootApiVersion());
            cc.setRuntimeId(c.getBootRuntime());
            cc.setJavaCommand(c.getBootJavaCommand());
            cc.setJavaOptions(c.getBootJavaOptions());
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigRuntime parseRuntimeConfig(NutsSession session) {
        NutsWorkspaceConfigRuntime cc = new NutsWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                Paths.get(getWorkspace().locations().getWorkspaceLocation())
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            cc.setDependencies(c.getBootRuntimeDependencies());
            cc.setId(c.getBootRuntime());
        }
        return cc;
    }

    @Override
    public NutsWorkspaceConfigSecurity parseSecurityConfig(NutsSession session) {
        NutsWorkspaceConfigSecurity cc = new NutsWorkspaceConfigSecurity();
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                Paths.get(getWorkspace().locations().getWorkspaceLocation())
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
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
        NutsWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                Paths.get(getWorkspace().locations().getWorkspaceLocation())
                        .resolve(NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME)));
        if (c != null) {
            c.setRepositories(CompatUtils.copyNutsRepositoryRefList(c.getRepositories()));
            c.setCommandFactories(CompatUtils.copyNutsCommandAliasFactoryConfigList(c.getCommandFactories()));
            c.setEnv(CompatUtils.copyProperties(c.getEnv()));
            c.setSdk(CompatUtils.copyNutsSdkLocationList(c.getSdk()));
            c.setImports(CompatUtils.copyStringList(c.getImports()));
        }
        return cc;
    }

    private NutsWorkspaceConfigBoot502 parseConfig502(byte[] bytes) {
        return getWorkspace().elem().setContentType(NutsContentType.JSON).parse(bytes, NutsWorkspaceConfigBoot502.class);
    }


}
