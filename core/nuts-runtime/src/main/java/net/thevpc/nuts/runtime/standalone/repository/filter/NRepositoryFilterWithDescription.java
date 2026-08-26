package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

import java.util.Objects;
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
    public NFilter withDescription(Supplier<NElement> description) {
        this.description = description;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NRepositoryFilterWithDescription that = (NRepositoryFilterWithDescription) o;
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
