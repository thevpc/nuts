//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.NConstants;
//import net.thevpc.nuts.util.NFilterOp;
//
//import java.util.List;
//
//public class NutsAPINDescriptorFilter extends AbstractDescriptorFilter {
//
//    private final NVersion apiVersion;
//
//    public NutsAPINDescriptorFilter(NVersion apiVersion) {
//        super(NFilterOp.CUSTOM);
//        this.apiVersion = apiVersion;
//    }
//
//    @Override
//    public boolean acceptDescriptor(NDescriptor descriptor) {
//        for (NDependency dependency : descriptor.getDependencies()) {
//            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_API)) {
//                if (apiVersion.filter().acceptVersion(dependency.getVersion())) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        }
//        // check now all transitive
//        List<NDependency> allDeps = NFetchCmd.of(descriptor.getId()).setDependencies(true)
//                .setDependencyFilter(NDependencyFilters.of().byRunnable()).getResultDefinition().getDependencies().get()
//                .transitive().toList();
//        for (NDependency dependency : allDeps) {
//            if (dependency.getSimpleName().equals(NConstants.Ids.NUTS_API)) {
//                if (apiVersion.filter().acceptVersion(dependency.getVersion())) {
//                    return true;
//                } else {
//                    return false;
//                }
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public String toString() {
//        return "NutsAPI(" + apiVersion + ')';
//    }
//
//    @Override
//    public NDescriptorFilter simplify() {
//        return this;
//    }
//}
