package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDefinitionFilter;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependsOnResolver;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

/**
 * Accepts artifacts depending, directly or transitively, on a given artifact
 * whose resolved version matches the dependency version filter.
 *
 * @see NDependsOnResolver
 */
public class NDefinitionDependsOnFilter extends DefinitionFilterBase {

    private final NId dependency;

    public NDefinitionDependsOnFilter(NId dependency) {
        super(NFilterOp.CUSTOM);
        this.dependency = Objects.requireNonNull(dependency, "dependency");
    }

    public NId getDependency() {
        return dependency;
    }

    @Override
    public boolean acceptDefinition(NDefinition definition) {
        return NDependsOnResolver.accept(definition.id(), dependency);
    }

    @Override
    public String toString() {
        return "dependsOn(" + dependency + ')';
    }

    @Override
    public NDefinitionFilter simplify() {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NDefinitionDependsOnFilter that = (NDefinitionDependsOnFilter) o;
        return Objects.equals(dependency, that.dependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), dependency);
    }
}
