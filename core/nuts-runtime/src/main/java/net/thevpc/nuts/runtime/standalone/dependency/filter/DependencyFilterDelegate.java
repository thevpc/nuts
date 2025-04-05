package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.List;

public abstract class DependencyFilterDelegate extends AbstractDependencyFilter{
    public DependencyFilterDelegate() {
        super(NFilterOp.CUSTOM);
    }
    public abstract NDependencyFilter dependencyFilter();

    @Override
    public boolean acceptDependency(NDependency dependency, NId from) {
        return dependencyFilter().acceptDependency(dependency, from);
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        return dependencyFilter().withDesc(description);
    }

    @Override
    public NDependencyFilter simplify() {
        return (NDependencyFilter) dependencyFilter().simplify();
    }

    @Override
    public NFilterOp getFilterOp() {
        return dependencyFilter().getFilterOp();
    }

    @Override
    public List<NFilter> getSubFilters() {
        return dependencyFilter().getSubFilters();
    }

    @Override
    public <T extends NFilter> T to(Class<T> type) {
        return dependencyFilter().to(type);
    }

    @Override
    public Class<? extends NFilter> getFilterType() {
        return dependencyFilter().getFilterType();
    }

    @Override
    public <T extends NFilter> NFilter simplify(Class<T> type) {
        return dependencyFilter().simplify(type);
    }

    @Override
    public NElement describe() {
        return dependencyFilter().describe();
    }
}
