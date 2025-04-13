package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public class NutsBootNDefinitionFilter extends AbstractDefinitionFilter {

    private final NVersion bootVersion;

    public NutsBootNDefinitionFilter(NVersion bootVersion) {
        super(NFilterOp.CUSTOM);
        this.bootVersion = bootVersion;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        for (NDependency dependency : definition.getDescriptor().getDependencies()) {
            if (dependency.getShortName().equals(NConstants.Ids.NUTS_BOOT)) {
                if (bootVersion.filter().acceptVersion(dependency.getVersion())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // check now all transitive
        List<NDependency> allDeps = NFetchCmd.of(definition.getId())
                .setDependencyFilter(NDependencyFilters.of().byRunnable()).getResultDefinition().getDependencies().get()
                .transitive().toList();
        for (NDependency dependency : allDeps) {
            if (dependency.getShortName().equals(NConstants.Ids.NUTS_BOOT)) {
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
    public NDefinitionFilter simplify() {
        return this;
    }
}
