package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public class NutsAPIDefinitionFilter extends AbstractDefinitionFilter {

    private final NVersion apiVersion;

    public NutsAPIDefinitionFilter(NVersion apiVersion) {
        super(NFilterOp.CUSTOM);
        this.apiVersion = apiVersion;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        for (NDependency dependency : definition.getDescriptor().getDependencies()) {
            if (dependency.getShortName().equals(NConstants.Ids.NUTS_API)) {
                if (apiVersion.filter().acceptVersion(dependency.getVersion())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // check now all transitive
        List<NDependency> allDeps = NFetchCmd.of(definition.getId())
                .setDependencyFilter(NDependencyFilters.of().byRunnable())
                .getResultDefinition().getDependencies().get()
                .transitive().toList();
        for (NDependency dependency : allDeps) {
            if (dependency.getShortName().equals(NConstants.Ids.NUTS_API)) {
                if (apiVersion.filter().acceptVersion(dependency.getVersion())) {
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
        return "NutsAPI(" + apiVersion + ')';
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }
}
