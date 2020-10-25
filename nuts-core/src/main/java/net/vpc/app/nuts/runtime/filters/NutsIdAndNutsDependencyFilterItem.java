package net.vpc.app.nuts.runtime.filters;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsIdGraph;

public class NutsIdAndNutsDependencyFilterItem {

    public boolean optional;
    public NutsIdGraph.NutsIdNode id;
    public NutsDescriptor descriptor;
    public NutsDescriptor effDescriptor;
    public NutsIdAndNutsDependencyFilterItem parent;

    public NutsIdAndNutsDependencyFilterItem(NutsIdGraph.NutsIdNode id, NutsIdAndNutsDependencyFilterItem parent) {
        this.id = id;
        this.parent = parent;
        this.optional = id.optional || (parent != null && parent.optional);
    }

    public NutsIdAndNutsDependencyFilterItem getParent() {
        return parent;
    }

    public NutsDescriptor getEffDescriptor(NutsSession session) {
        if (effDescriptor == null) {
            effDescriptor = session.getWorkspace().fetch().setId(id.id)
                    .setEffective(true)
                    .setSession(CoreNutsUtils.silent(session))
                    .getResultDescriptor();
        }
        return effDescriptor;
    }

    public NutsDescriptor getDescriptor(NutsSession session) {
        if (descriptor == null) {
            descriptor = session.getWorkspace().fetch().setId(id.id).setSession(session).getResultDescriptor();
        }
        return descriptor;
    }
}
