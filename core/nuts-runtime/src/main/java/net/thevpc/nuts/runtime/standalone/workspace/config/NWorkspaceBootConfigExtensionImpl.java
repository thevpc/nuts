package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NWorkspaceBootConfig;

class NWorkspaceBootConfigExtensionImpl implements NWorkspaceBootConfig.Extension {
    private NId id;
    private boolean enabled;

    public NWorkspaceBootConfigExtensionImpl(NWorkspaceConfigBoot.ExtensionConfig c) {
        this.id = c.getId();
        this.enabled = c.isEnabled();
    }

    @Override
    public NId getId() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
