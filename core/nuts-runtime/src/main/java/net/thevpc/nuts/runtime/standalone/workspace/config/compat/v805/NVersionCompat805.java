package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v805;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NVersion;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.runtime.standalone.store.NWorkspaceStore;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.compat.AbstractNVersionCompat;

public class NVersionCompat805 extends AbstractNVersionCompat {
    public NVersionCompat805(NVersion apiVersion) {
        super(apiVersion, 805);
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
