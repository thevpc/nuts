package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;

import java.util.Objects;

public abstract class NDescriptorFilterDelegate extends AbstractDescriptorFilter{
    public abstract NDescriptorFilter baseDescriptorFilter();

    public NDescriptorFilterDelegate(NSession session) {
        super(session, NFilterOp.CUSTOM);
    }

    @Override
    public boolean acceptSearchId(NSearchId sid, NSession session) {
        return baseDescriptorFilter().acceptSearchId(sid, session);
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor, NSession session) {
        return baseDescriptorFilter().acceptDescriptor(descriptor, session);
    }

    @Override
    public NFilter withDesc(NEDesc description) {
        return baseDescriptorFilter().withDesc(description);
    }

    @Override
    public NDescriptorFilter simplify() {
        return (NDescriptorFilter) baseDescriptorFilter().simplify();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDescriptorFilterDelegate that = (NDescriptorFilterDelegate) o;
        return Objects.equals(baseDescriptorFilter(), that.baseDescriptorFilter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(baseDescriptorFilter());
    }
}