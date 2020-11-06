package net.thevpc.nuts.runtime.main.config;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsWorkspaceBootConfig;

class NutsWorkspaceBootConfigExtensionImpl implements NutsWorkspaceBootConfig.Extension {
    private NutsId id;
    private boolean enabled;

    public NutsWorkspaceBootConfigExtensionImpl(NutsWorkspaceConfigBoot.ExtensionConfig c) {
        this.id = c.getId();
        this.enabled = c.isEnabled();
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
