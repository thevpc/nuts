package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;

public class NutsIdAndNutsDependencyFilterItem {

    public NutsIdGraph.NutsIdNode id;
    public NutsDescriptor descriptor;

    public NutsIdAndNutsDependencyFilterItem(NutsIdGraph.NutsIdNode id) {
        this.id = id;
    }

    public NutsDescriptor getDescriptor(NutsWorkspace ws, NutsSession session) {
        if (descriptor == null) {
            descriptor = ws.fetchDescriptor(id.id, true, session);
        }
        return descriptor;
    }
}
