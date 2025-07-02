package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NVersionFilter;
import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NVersionFilterDelegate;
import net.thevpc.nuts.util.NFilter;

import java.util.function.Supplier;

public class NVersionFilterWithDescription extends NVersionFilterDelegate {
    private NVersionFilter baseVersionFilter;
    private Supplier<NElement> description;

    public NVersionFilterWithDescription(NVersionFilter baseVersionFilter, Supplier<NElement> description) {
        super();
        this.baseVersionFilter = baseVersionFilter;
        this.description = description;
    }

    @Override
    public NVersionFilter baseVersionFilter() {
        return baseVersionFilter;
    }

    @Override
    public NFilter redescribe(Supplier<NElement> description) {
        this.description=description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribableElementSupplier.safeDescribeOfBase(description, baseVersionFilter);
    }
}
