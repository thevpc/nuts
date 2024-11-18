package net.thevpc.nuts.runtime.standalone.dependency.filter;

import net.thevpc.nuts.NDependencyFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class DependencyFilterWithDescription extends DependencyFilterDelegate {
    private NDependencyFilter base;
    private NEDesc description;

    public DependencyFilterWithDescription(NWorkspace workspace, NDependencyFilter base, NEDesc description) {
        super(workspace);
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
    public NElement describe() {
        return NEDesc.safeDescribeOfBase(description, base);
    }
}
