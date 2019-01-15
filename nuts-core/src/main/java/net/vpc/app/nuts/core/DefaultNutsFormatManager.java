package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

public class DefaultNutsFormatManager implements NutsFormatManager {
    private NutsWorkspace ws;

    public DefaultNutsFormatManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsIdFormat createIdFormat() {
        return new DefaultNutsIdFormat(ws);
    }

    @Override
    public NutsWorkspaceVersionFormat createWorkspaceVersionFormat() {
        return new DefaultNutsWorkspaceVersionFormat(ws);
    }

    @Override
    public NutsWorkspaceInfoFormat createWorkspaceInfoFormat() {
        return new DefaultNutsWorkspaceInfoFormat(ws);
    }

    @Override
    public NutsDescriptorFormat createDescriptorFormat() {
        return new DefaultNutsDescriptorFormat(ws);
    }
}
