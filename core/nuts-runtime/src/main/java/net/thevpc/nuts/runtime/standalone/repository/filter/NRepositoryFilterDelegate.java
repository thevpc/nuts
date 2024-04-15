package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.NRepositoryFilter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;
import java.util.Objects;

public abstract class NRepositoryFilterDelegate extends AbstractRepositoryFilter {
    public NRepositoryFilterDelegate(NSession session) {
        super(session, NFilterOp.CUSTOM);
    }
    public abstract NRepositoryFilter baseRepositoryFilter();

    @Override
    public NFilterOp getFilterOp() {
        return baseRepositoryFilter().getFilterOp();
    }

    @Override
    public NSession getSession() {
        return baseRepositoryFilter().getSession();
    }

    @Override
    public List<NFilter> getSubFilters() {
        return baseRepositoryFilter().getSubFilters();
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return baseRepositoryFilter().getFilterType();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return baseRepositoryFilter().simplify(type);
    }

    @Override
    public NElement describe(NSession session) {
        return baseRepositoryFilter().describe(session);
    }

    @Override
    public boolean acceptRepository(NRepository repository) {
        return baseRepositoryFilter().acceptRepository(repository);
    }

    @Override
    public NRepositoryFilter simplify() {
        return (NRepositoryFilter) baseRepositoryFilter().simplify();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NRepositoryFilterDelegate that = (NRepositoryFilterDelegate) o;
        return Objects.equals(baseRepositoryFilter(), that.baseRepositoryFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseRepositoryFilter());
    }
}
