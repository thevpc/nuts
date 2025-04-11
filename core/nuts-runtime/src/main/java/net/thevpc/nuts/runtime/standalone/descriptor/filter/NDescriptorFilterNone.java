//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.standalone.dependency.util.NComplexExpressionString;
//import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
//import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
//import net.thevpc.nuts.util.NFilter;
//import net.thevpc.nuts.util.NFilterOp;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.stream.Collectors;
//
//public class NDescriptorFilterNone extends AbstractDescriptorFilter {
//
//    private NDescriptorFilter[] all;
//
//    public NDescriptorFilterNone(NDescriptorFilter... all) {
//        super(NFilterOp.NOT);
//        List<NDescriptorFilter> valid = new ArrayList<>();
//        if (all != null) {
//            for (NDescriptorFilter filter : all) {
//                if (filter != null) {
//                    valid.add(filter);
//                }
//            }
//        }
//        this.all = valid.toArray(new NDescriptorFilter[0]);
//    }
//
//    @Override
//    public boolean acceptDescriptor(NDescriptor id) {
//        if (all.length == 0) {
//            return true;
//        }
//        for (NDescriptorFilter filter : all) {
//            if (filter.acceptDescriptor(id)) {
//                return false;
//            }
//        }
//        return true;
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        return CoreFilterUtils.simplifyFilterNone( NDescriptorFilter.class,this,all);
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 53 * hash + Arrays.deepHashCode(this.all);
//        return hash;
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if (this == obj) {
//            return true;
//        }
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final NDescriptorFilterNone other = (NDescriptorFilterNone) obj;
//        if (!Arrays.deepEquals(this.all, other.all)) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public String toString() {
//        return CoreStringUtils.trueOrNone(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
//    }
//
//    public List<NFilter> getSubFilters() {
//        return Arrays.asList(all);
//    }
//}
