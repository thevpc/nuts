package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;

public class NVersionCompat803 extends AbstractNVersionCompat {
    public NVersionCompat803(NVersion apiVersion) {
        super(apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return bytes==null?null: NElements.of().json().parse(bytes, NWorkspaceConfigBoot.class);
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
