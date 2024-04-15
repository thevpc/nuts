package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.NDependencyFilter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class DependencyFilterWithDescription extends DependencyFilterDelegate {
    private NDependencyFilter base;
    private NEDesc description;

    public DependencyFilterWithDescription(NDependencyFilter base, NEDesc description) {
        super(base.getSession());
        this.base = base;
        this.description = description;
    }

    @Override
    public NDependencyFilter dependencyFilter() {
        return base;
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribeOfBase(session, description, base);
    }
}
