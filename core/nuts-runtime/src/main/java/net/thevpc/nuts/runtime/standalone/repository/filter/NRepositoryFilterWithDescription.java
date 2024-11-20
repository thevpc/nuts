package net.thevpc.nuts.runtime.standalone.repository.filter;

import net.thevpc.nuts.NRepositoryFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class NRepositoryFilterWithDescription extends NRepositoryFilterDelegate {
    private final NRepositoryFilter base;
    private NEDesc description;

    public NRepositoryFilterWithDescription(NWorkspace workspace, NRepositoryFilter base, NEDesc description) {
        super(workspace);
        this.base = base;
        this.description = description;
    }

    @Override
    public NRepositoryFilter baseRepositoryFilter() {
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
