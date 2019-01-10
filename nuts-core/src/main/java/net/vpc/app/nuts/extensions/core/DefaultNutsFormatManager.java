package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.DefaultNutsDescriptorFormat;
import net.vpc.app.nuts.extensions.util.DefaultNutsIdFormat;
import net.vpc.app.nuts.extensions.util.DefaultNutsWorkspaceInfoFormat;
import net.vpc.app.nuts.extensions.util.DefaultNutsWorkspaceVersionFormat;

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
