package net.vpc.app.nuts.runtime.filters.id;

import net.vpc.app.nuts.*;

public class BootAPINutsDescriptorFilter implements NutsDescriptorFilter {

    private final NutsVersion bootApiVersion;

    public BootAPINutsDescriptorFilter(NutsVersion bootApiVersion) {
        this.bootApiVersion = bootApiVersion;
    }

    @Override
    public boolean accept(NutsDescriptor descriptor, NutsSession session) {
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
}
