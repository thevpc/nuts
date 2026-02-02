package net.thevpc.nuts.internal.util;

import net.thevpc.nuts.artifact.NIdFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NIdFilterDelegate;
import net.thevpc.nuts.util.NFilter;

import java.util.function.Supplier;

public class NIdFilterWithDescriptor extends NIdFilterDelegate {
    private NIdFilter base;
    private Supplier<NElement> description;

    public NIdFilterWithDescriptor(NIdFilter base, Supplier<NElement> description) {
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NIdFilter baseNIdFilter() {
        return base;
    }

    @Override
    public NFilter withDescription(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, base);
    }
}
