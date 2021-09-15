package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.descriptor.AbstractDescriptorFilter;

public class BootAPINutsDescriptorFilter extends AbstractDescriptorFilter {

    private final NutsVersion bootApiVersion;

    public BootAPINutsDescriptorFilter(NutsSession ws, NutsVersion bootApiVersion) {
        super(ws, NutsFilterOp.CUSTOM);
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
