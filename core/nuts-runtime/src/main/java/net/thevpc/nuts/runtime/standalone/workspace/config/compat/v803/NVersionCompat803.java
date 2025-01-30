package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElements;


import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.util.CoreNConstants;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.CompatUtils;

public class NVersionCompat803 extends AbstractNVersionCompat {
    public NVersionCompat803(NWorkspace workspace,NVersion apiVersion) {
        super(workspace,apiVersion, 507);
    }

    @Override
    public NWorkspaceConfigBoot parseConfig(byte[] bytes) {
        return bytes==null?null: NElements.of().json().parse(bytes, NWorkspaceConfigBoot.class);
    }

    @Override
    public NWorkspaceConfigApi parseApiConfig(NId nutsApiId) {
        NWorkspaceStore store = ((NWorkspaceExt) workspace).store();
        return store.loadConfigApi(nutsApiId);
    }

    @Override
    public NWorkspaceConfigRuntime parseRuntimeConfig() {
        NWorkspaceStore store = ((NWorkspaceExt) workspace).store();
        return store.loadConfigRuntime();
    }

    @Override
    public NWorkspaceConfigSecurity parseSecurityConfig(NId nutsApiId) {
        NWorkspaceStore store = ((NWorkspaceExt) workspace).store();
        return store.loadConfigSecurity(nutsApiId);
    }

    @Override
    public NWorkspaceConfigMain parseMainConfig(NId nutsApiId) {
        NWorkspaceStore store = ((NWorkspaceExt) workspace).store();
        return store.loadConfigMain(nutsApiId);
    }
}
