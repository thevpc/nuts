package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public abstract class NIdFilterDelegate extends AbstractIdFilter {
    public abstract NIdFilter baseNIdFilter();

    public NIdFilterDelegate() {
        super(NFilterOp.CUSTOM);
    }

    @Override
    public boolean acceptId(NId id) {
        return baseNIdFilter().acceptId(id);
    }

    @Override
    public NFilterOp getFilterOp() {
        return baseNIdFilter().getFilterOp();
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return baseNIdFilter().getFilterType();
    }

    @Override
    public NIdFilter simplify() {
        return (NIdFilter) baseNIdFilter().simplify();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return baseNIdFilter().simplify();
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return baseNIdFilter().to(type);
    }

    @Override
    public List<NFilter> getSubFilters() {
        return baseNIdFilter().getSubFilters();
    }

    @Override
    public NElement describe() {
        return baseNIdFilter().describe();
    }

    @Override
    public NFilter redescribe(Supplier<NElement> description) {
        return baseNIdFilter().redescribe(description);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NIdFilterDelegate that = (NIdFilterDelegate) o;
        return Objects.equals(baseNIdFilter(), that.baseNIdFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseNIdFilter());
    }
}
