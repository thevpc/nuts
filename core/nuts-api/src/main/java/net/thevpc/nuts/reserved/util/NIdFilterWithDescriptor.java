package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NIdFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NIdFilterDelegate;
import net.thevpc.nuts.util.NFilter;

public class NIdFilterWithDescriptor extends NIdFilterDelegate {
    private NIdFilter base;
    private NEDesc description;

    public NIdFilterWithDescriptor(NWorkspace workspace, NIdFilter base, NEDesc description) {
        super(workspace);
        this.base = base;
        this.description = description;
    }

    @Override
    public NIdFilter baseNIdFilter() {
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
