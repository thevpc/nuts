package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.artifact.NDependencyFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

import java.util.Objects;
import java.util.function.Supplier;

public class DependencyFilterWithDescription extends DependencyFilterDelegate {
    private NDependencyFilter base;
    private Supplier<NElement> description;

    public DependencyFilterWithDescription(NDependencyFilter base, Supplier<NElement> description) {
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NDependencyFilter dependencyFilter() {
        return base;
    }

    @Override
    public NFilter withDescription(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DependencyFilterWithDescription that = (DependencyFilterWithDescription) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, base);
    }
}
