package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public class NutsBootNDescriptorFilter extends AbstractDescriptorFilter {

    private final NVersion bootVersion;

    public NutsBootNDescriptorFilter(NVersion bootVersion) {
        super(NFilterOp.CUSTOM);
        this.bootVersion = bootVersion;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor) {
        for (NDependency dependency : descriptor.getDependencies()) {
            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_BOOT)) {
                if (bootVersion.filter().acceptVersion(dependency.getVersion())) {
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
            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_BOOT)) {
                if (bootVersion.filter().acceptVersion(dependency.getVersion())) {
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
        return "NutsBoot(" + bootVersion + ')';
    }

    @Override
    public NDescriptorFilter simplify() {
        return this;
    }
}
