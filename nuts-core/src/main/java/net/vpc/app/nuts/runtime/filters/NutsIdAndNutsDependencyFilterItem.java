package net.vpc.app.nuts.runtime.filters;

import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
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
