package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class NVersionCompat507 extends AbstractNVersionCompat {

    public static final NVersion CONFIG_VERSION_507 = NVersion.get("5.0.7").get();

    public NVersionCompat507(NVersion apiVersion) {
        super(apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        NWorkspaceConfigBoot507 w = bytes == null ? null : NElementParser.ofJson().parse(bytes, NWorkspaceConfigBoot507.class);
        NWorkspaceConfigBoot v=new NWorkspaceConfigBoot();
         v.setUuid(w.getUuid());
        v.setSystem(w.isSystem());
        v.setName(w.getName());
        v.setWorkspace(w.getWorkspace());
        v.setBootRepositories(NStringUtils.split(w.getBootRepositories(),";",true,true));

        v.setStoreLocations(w.getStoreLocations());
        v.setHomeLocations(w.getHomeLocations());

        v.setRepositoryStoreStrategy(w.getRepositoryStoreStrategy());
        v.setStoreStrategy(w.getStoreStrategy());
        v.setStoreLayout(w.getStoreLayout());
        v.setConfigVersion(w.getConfigVersion());
        v.setExtensions(w.getExtensions()==null?null:w.getExtensions().stream().map(x->{
            if(x==null){
                return null;
            }
            NWorkspaceConfigBoot.ExtensionConfig c = new NWorkspaceConfigBoot.ExtensionConfig();
            c.setDependencies(x.getDependencies());
            c.setEnabled(x.isEnabled());
            c.setId(x.getId());
            c.setConfigVersion(x.getConfigVersion());
            return c;
        }).collect(Collectors.toList()));
        return v;
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(nutsApiId, NStoreType.CONF)
                .resolve(NConstants.Files.API_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigApi c = bytes==null?null: NElementParser.ofJson().parse(bytes, NWorkspaceConfigApi.class);
        // Removed test because we would have incoherence between ApiVersion and RuntimeVersion
        // Actually I dont know was the initial need was for doing this test!
//        if (c != null) {
//            c.setApiVersion(getApiVersion());
//        }
        return c;
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NPath path = NWorkspace.of().getStoreLocation(NWorkspace.of().getRuntimeId(), NStoreType.CONF)
                .resolve(NConstants.Files.RUNTIME_BOOT_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigRuntime c = bytes==null?null: NElementParser.ofJson().parse(bytes, NWorkspaceConfigRuntime.class);
        return c;
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(nutsApiId
                , NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_SECURITY_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigSecurity c = bytes==null?null: NElementParser.ofJson().parse(bytes, NWorkspaceConfigSecurity.class);
        return c;
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NPath path = NWorkspace.of().getStoreLocation(
                        nutsApiId, NStoreType.CONF)
                .resolve(CoreNConstants.Files.WORKSPACE_MAIN_CONFIG_FILE_NAME);
        byte[] bytes = CompatUtils.readAllBytes(path);
        NWorkspaceConfigMain507 c = bytes==null?null: NElementParser.ofJson().parse(bytes, NWorkspaceConfigMain507.class);
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
