package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;

public class NutsIdAndNutsDependencyFilterItem {

    public NutsId id;
    public NutsDescriptor descriptor;
    public NutsDependencyFilter filter;

    public NutsIdAndNutsDependencyFilterItem(NutsId id, NutsDependencyFilter filter) {
        this.id = id;
        this.filter = filter;
    }

    public NutsDescriptor getDescriptor(NutsWorkspace ws, NutsSession session) {
        if (descriptor == null) {
            descriptor = ws.fetchDescriptor(id, true, session);
        }
        return descriptor;
    }
}
