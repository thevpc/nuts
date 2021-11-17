package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;

public class BootAPINutsDescriptorFilter extends AbstractDescriptorFilter {

    private final NutsVersion bootApiVersion;

    public BootAPINutsDescriptorFilter(NutsSession session, NutsVersion bootApiVersion) {
        super(session, NutsFilterOp.CUSTOM);
        this.bootApiVersion = bootApiVersion;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor descriptor, NutsSession session) {
        for (NutsDependency dependency : descriptor.getDependencies()) {
            if (dependency.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                if (bootApiVersion.filter().acceptVersion(dependency.getVersion(), session)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BootAPI(" + bootApiVersion + ')';
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return this;
    }
}
