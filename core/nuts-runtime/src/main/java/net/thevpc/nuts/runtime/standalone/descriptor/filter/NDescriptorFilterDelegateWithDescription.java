package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.NDescriptorFilter;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class NDescriptorFilterDelegateWithDescription extends NDescriptorFilterDelegate {
    private NDescriptorFilter base;
    private NEDesc description;

    public NDescriptorFilterDelegateWithDescription(NDescriptorFilter base, NEDesc description) {
        super(base.getSession());
        this.base = base;
        this.description = description;
    }

    @Override
    public NDescriptorFilter baseDescriptorFilter() {
        return base;
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        this.description = description;
        return this;
    }

    @Override
    public NElement describe(NSession session) {
        return NEDesc.safeDescribeOfBase(getSession(), description, base);
    }
}
