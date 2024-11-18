package net.thevpc.nuts.reserved.util;

import net.thevpc.nuts.NVersionFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.spi.base.NVersionFilterDelegate;
import net.thevpc.nuts.util.NFilter;

public class NVersionFilterWithDescription extends NVersionFilterDelegate {
    private NVersionFilter baseVersionFilter;
    private NEDesc description;

    public NVersionFilterWithDescription(NWorkspace workspace, NVersionFilter baseVersionFilter, NEDesc description) {
        super(workspace);
        this.baseVersionFilter = baseVersionFilter;
        this.description = description;
    }

    @Override
    public NVersionFilter baseVersionFilter() {
        return baseVersionFilter;
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        this.description=description;
        return this;
    }

    @Override
    public NElement describe() {
        return NEDesc.safeDescribeOfBase(description, baseVersionFilter);
    }
}
