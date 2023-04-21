package net.thevpc.nuts.runtime.standalone.descriptor.filter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class NDescriptorFilterAnd extends AbstractDescriptorFilter implements NComplexExpressionString {

    private NDescriptorFilter[] all;

    public NDescriptorFilterAnd(NSession session, NDescriptorFilter... all) {
        super(session, NFilterOp.AND);
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
            if (!filter.acceptDescriptor(id, session)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NDescriptorFilter simplify() {
        return CoreFilterUtils.simplifyFilterAnd(getSession(), NDescriptorFilter.class,this,all);
    }


    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Arrays.deepHashCode(this.all);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NDescriptorFilterAnd other = (NDescriptorFilterAnd) obj;
        if (!Arrays.deepEquals(this.all, other.all)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return CoreStringUtils.trueOrAnd(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
    }

    public List<NFilter> getSubFilters() {
        return Arrays.asList(all);
    }
}
