package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.elem.NDescribableElementSupplier;
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
    public NFilter redescribe(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribableElementSupplier.safeDescribeOfBase(description, base);
    }
}
