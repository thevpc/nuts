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
//public class NDescriptorFilterOr extends AbstractDescriptorFilter implements NComplexExpressionString {
//
//    private NDescriptorFilter[] all;
//
//    public NDescriptorFilterOr(NDescriptorFilter... all) {
//        super(NFilterOp.OR);
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
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        return CoreFilterUtils.simplifyFilterOr(NDescriptorFilter.class,this,all);
//    }
//
//    @Override
//    public String toString() {
//        return CoreStringUtils.trueOrOr(Arrays.stream(all).map(NComplexExpressionString::toString).collect(Collectors.toList()));
//    }
//
//    public List<NFilter> getSubFilters() {
//        return Arrays.asList(all);
//    }
//}
