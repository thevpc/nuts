package net.vpc.app.nuts.core.filters;

import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.NutsIdGraph;
import net.vpc.app.nuts.*;

public class NutsIdAndNutsDependencyFilterItem {

    public NutsIdGraph.NutsIdNode id;
    public NutsDescriptor descriptor;
    public NutsDescriptor effDescriptor;

    public NutsIdAndNutsDependencyFilterItem(NutsIdGraph.NutsIdNode id) {
        this.id = id;
    }

    public NutsDescriptor getEffDescriptor(NutsSession session) {
        if (effDescriptor == null) {
            effDescriptor = session.getWorkspace().fetch().id(id.id)
                    .effective()
                    .setSession(session.copy().trace(false))
                    .setEffective(true).getResultDescriptor();
        }
        return effDescriptor;
    }

    public NutsDescriptor getDescriptor(NutsSession session) {
        if (descriptor == null) {
            descriptor = session.getWorkspace().fetch().id(id.id).setSession(session).getResultDescriptor();
        }
        return descriptor;
    }
}
