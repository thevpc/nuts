package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.NDependencyFilter;
import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

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
    public NFilter redescribe(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribableElementSupplier.safeDescribeOfBase(description, base);
    }
}
