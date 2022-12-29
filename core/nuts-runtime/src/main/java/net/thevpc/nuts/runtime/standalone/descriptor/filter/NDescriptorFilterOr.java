package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NDescriptorFilterOr extends AbstractDescriptorFilter  {

    private NDescriptorFilter[] all;

    public NDescriptorFilterOr(NSession session, NDescriptorFilter... all) {
        super(session, NFilterOp.OR);
        List<NDescriptorFilter> valid = new ArrayList<>();
        if (all != null) {
            for (NDescriptorFilter filter : all) {
                if (filter != null) {
                    valid.add(filter);
                }
            }
        }
        this.all = valid.toArray(new NDescriptorFilter[0]);
    }

    @Override
    public boolean acceptDescriptor(NDescriptor id, NSession session) {
        if (all.length == 0) {
            return true;
        }
        for (NDescriptorFilter filter : all) {
            if (filter.acceptDescriptor(id, session)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NDescriptorFilter simplify() {
        return CoreFilterUtils.simplifyFilterOr(getSession(), NDescriptorFilter.class,this,all);
    }

    @Override
    public String toString() {
        return String.join(" Or ", Arrays.asList(all).stream().map(x -> "(" + x.toString() + ")").collect(Collectors.toList()));
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
