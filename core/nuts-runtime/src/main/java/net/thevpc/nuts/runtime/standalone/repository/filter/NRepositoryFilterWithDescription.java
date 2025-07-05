package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.NRepositoryFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

import java.util.function.Supplier;

public class NRepositoryFilterWithDescription extends NRepositoryFilterDelegate {
    private final NRepositoryFilter base;
    private Supplier<NElement> description;

    public NRepositoryFilterWithDescription(NRepositoryFilter base, Supplier<NElement> description) {
        super();
        this.base = base;
        this.description = description;
    }

    @Override
    public NRepositoryFilter baseRepositoryFilter() {
        return base;
    }

    @Override
    public NFilter redescribe(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe() {
        return NDescribables.safeDescribeOfBase(description, base);
    }
}
