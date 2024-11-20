package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.NDescriptorFilter;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.util.NFilter;

public class NDescriptorFilterDelegateWithDescription extends NDescriptorFilterDelegate {
    private NDescriptorFilter base;
    private NEDesc description;

    public NDescriptorFilterDelegateWithDescription(NWorkspace workspace, NDescriptorFilter base, NEDesc description) {
        super(workspace);
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
    public NElement describe() {
        return NEDesc.safeDescribeOfBase(description, base);
    }
}
