package net.thevpc.nuts.runtime.standalone.id.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.filter.AbstractDescriptorFilter;
import net.thevpc.nuts.util.NFilterOp;

public class BootAPINDescriptorFilter extends AbstractDescriptorFilter {

    private final NVersion bootApiVersion;

    public BootAPINDescriptorFilter(NSession session, NVersion bootApiVersion) {
        super(session, NFilterOp.CUSTOM);
        this.bootApiVersion = bootApiVersion;
    }

    @Override
    public boolean acceptDescriptor(NDescriptor descriptor, NSession session) {
        for (NDependency dependency : descriptor.getDependencies()) {
            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_API)) {
                if (bootApiVersion.filter(session).acceptVersion(dependency.getVersion(), session)) {
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "BootAPI(" + bootApiVersion + ')';
    }

    @Override
    public NDescriptorFilter simplify() {
        return this;
    }
}
