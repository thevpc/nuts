package net.vpc.app.nuts.runtime.filters;

import net.vpc.app.nuts.runtime.util.NutsIdGraph;
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
                    .session(session.copy().silent())
                    .effective().getResultDescriptor();
        }
        return effDescriptor;
    }

    public NutsDescriptor getDescriptor(NutsSession session) {
        if (descriptor == null) {
            descriptor = session.getWorkspace().fetch().id(id.id).session(session).getResultDescriptor();
        }
        return descriptor;
    }
}
