package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507.NWorkspaceConfigBoot507;
import net.thevpc.nuts.util.NStringUtils;

import java.util.stream.Collectors;

public class NVersionCompat803 extends AbstractNVersionCompat {
    public NVersionCompat803(NVersion apiVersion) {
        super(apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        NWorkspaceConfigBoot507 w = bytes == null ? null : NElements.of().json().parse(bytes, NWorkspaceConfigBoot507.class);
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
        NWorkspaceStore store = NWorkspaceExt.of().store();
        return store.loadConfigApi(nutsApiId);
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NWorkspaceStore store = NWorkspaceExt.of().store();
        return store.loadConfigRuntime();
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NWorkspaceStore store = NWorkspaceExt.of().store();
        return store.loadConfigSecurity(nutsApiId);
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NWorkspaceStore store = NWorkspaceExt.of().store();
        return store.loadConfigMain(nutsApiId);
    }
}
