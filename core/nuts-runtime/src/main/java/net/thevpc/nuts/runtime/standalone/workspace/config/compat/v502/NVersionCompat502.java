package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

import java.util.List;

public class NVersionCompat502 extends AbstractNVersionCompat {
    public NVersionCompat502(NSession session, NVersion apiVersion) {
        super(session,apiVersion, 502);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes, NSession session) {
        return parseConfig502(bytes, session).toWorkspaceConfig();
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId, NSession session) {
        NWorkspaceConfigApi cc = new NWorkspaceConfigApi();
        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session), session);
        if (c != null) {
            cc.setApiVersion(c.getBootApiVersion());
            cc.setRuntimeId(c.getBootRuntime());
            cc.setJavaCommand(c.getBootJavaCommand());
            cc.setJavaOptions(c.getBootJavaOptions());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig(NSession session) {
        NWorkspaceConfigRuntime cc = new NWorkspaceConfigRuntime();
//        cc.setApiVersion(getApiVersion());
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session), session);
        if (c != null) {
            cc.setDependencies(c.getBootRuntimeDependencies());
            cc.setId(c.getBootRuntime());
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId, NSession session) {
        NWorkspaceConfigSecurity cc = new NWorkspaceConfigSecurity();
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session), session);
        if (c != null) {
            cc.setSecure(c.isSecure());
            cc.setAuthenticationAgent(c.getAuthenticationAgent());
            List<NUserConfig> users = c.getUsers();
            cc.setUsers(CompatUtils.copyNutsUserConfigArray(users==null?null: users.toArray(new NUserConfig[0])));
        }
        return cc;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId, NSession session) {
        NWorkspaceConfigMain cc = new NWorkspaceConfigMain();
        NWorkspaceConfigBoot502 c = parseConfig502(CompatUtils.readAllBytes(
                NLocations.of().getWorkspaceLocation().toPath().get()
                        .resolve(NConstants.Files.WORKSPACE_CONFIG_FILE_NAME),session), session);
        if (c != null) {
            c.setRepositories(CompatUtils.copyNutsRepositoryRefList(c.getRepositories()));
            c.setCommandFactories(CompatUtils.copyNutsCommandAliasFactoryConfigList(c.getCommandFactories()));
            c.setEnv(CompatUtils.copyProperties(c.getEnv()));
            c.setSdk(CompatUtils.copyNutsSdkLocationList(c.getSdk()));
            c.setImports(CompatUtils.copyStringList(c.getImports()));
        }
        return cc;
    }

    private NWorkspaceConfigBoot502 parseConfig502(byte[] bytes, NSession session) {
        return NElements.of().json().parse(bytes, NWorkspaceConfigBoot502.class);
    }


}
