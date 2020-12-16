package net.thevpc.nuts.runtime.core.filters.id;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.AbstractNutsFilter;

public class BootAPINutsDescriptorFilter extends AbstractNutsFilter implements NutsDescriptorFilter {

    private final NutsVersion bootApiVersion;

    public BootAPINutsDescriptorFilter(NutsWorkspace ws, NutsVersion bootApiVersion) {
        super(ws, NutsFilterOp.CUSTOM);
        this.bootApiVersion = bootApiVersion;
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor descriptor, NutsSession session) {
        for (NutsDependency dependency : descriptor.getDependencies()) {
            if (dependency.getSimpleName().equals(NutsConstants.Ids.NUTS_API)) {
                if (bootApiVersion.matches(dependency.getVersion().toString())) {
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
        return "BootAPI(" +bootApiVersion +')';
    }

    @Override
    public NutsFilter simplify() {
        return this;
    }
}
