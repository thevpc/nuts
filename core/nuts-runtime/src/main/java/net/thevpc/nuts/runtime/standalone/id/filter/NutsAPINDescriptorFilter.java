package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public class NutsAPINDescriptorFilter extends AbstractDescriptorFilter {

    private final NVersion bootApiVersion;

    public NutsAPINDescriptorFilter(NWorkspace workspace, NVersion bootApiVersion) {
        super(workspace, NFilterOp.CUSTOM);
        this.bootApiVersion = bootApiVersion;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor) {
        for (NDependency dependency : descriptor.getDependencies()) {
            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_API)) {
                if (bootApiVersion.filter().acceptVersion(dependency.getVersion())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // check now all transitive
        List<NDependency> allDeps = NFetchCmd.of(descriptor.getId()).setDependencies(true)
                .setDependencyFilter(NDependencyFilters.of().byRunnable()).getResultDefinition().getDependencies().get()
                .transitive().toList();
        for (NDependency dependency : allDeps) {
            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_API)) {
                if (bootApiVersion.filter().acceptVersion(dependency.getVersion())) {
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
        return "NutsAPI(" + bootApiVersion + ')';
    }

    @Override
    public NDescriptorFilter simplify() {
        return this;
    }
}
