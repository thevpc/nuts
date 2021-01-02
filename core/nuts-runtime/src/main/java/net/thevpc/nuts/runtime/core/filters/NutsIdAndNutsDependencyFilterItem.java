package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.util.NutsIdGraph;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

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
