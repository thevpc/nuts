package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.internal.rpi.NDependencyFilterRPI;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;
import java.util.Objects;

public class NutsAPIDefinitionFilter extends AbstractDefinitionFilter {

    private final NVersion apiVersion;

    public NutsAPIDefinitionFilter(NVersion apiVersion) {
        super(NFilterOp.CUSTOM);
        this.apiVersion = apiVersion;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        for (NDependency dependency : definition.descriptor().dependencies()) {
            if (dependency.shortName().equals(NConstants.Ids.NUTS_API)) {
                if (apiVersion.toFilter().acceptVersion(dependency.version())) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        // check now all transitive
        List<NDependency> allDeps = NFetch.of(definition.id())
                .dependencyFilter(NDependencyFilter.ofRunnable())
                .getResultDefinition().dependencies().get()
                .transitive().toList();
        for (NDependency dependency : allDeps) {
            if (dependency.shortName().equals(NConstants.Ids.NUTS_API)) {
                if (apiVersion.toFilter().acceptVersion(dependency.version())) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NutsAPIDefinitionFilter that = (NutsAPIDefinitionFilter) o;
        return Objects.equals(apiVersion, that.apiVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), apiVersion);
    }
}
