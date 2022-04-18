package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NutsDescriptorFilterOr extends AbstractDescriptorFilter  {

    private NutsDescriptorFilter[] all;

    public NutsDescriptorFilterOr(NutsSession session, NutsDescriptorFilter... all) {
        super(session, NutsFilterOp.OR);
        List<NutsDescriptorFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NutsDescriptorFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NutsDescriptorFilter[0]);
    }

    @Override
    public boolean acceptDescriptor(NutsDescriptor id, NutsSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NutsDescriptorFilter filter : all) {
            if (filter.acceptDescriptor(id, session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsDescriptorFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(getSession(),NutsDescriptorFilter.class,this,all);
    }

    @Override
    public String toString() {
        return String.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    public List<NutsFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
