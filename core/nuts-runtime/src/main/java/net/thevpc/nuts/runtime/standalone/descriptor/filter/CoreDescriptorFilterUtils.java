//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
//
//import java.util.Map;
//
//public class CoreDescriptorFilterUtils {
//    public static NDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap) {
//        return createNutsDescriptorFilter(
//                faceMap == null ? null : faceMap.get(NConstants.IdProperties.ARCH),
//                faceMap == null ? null : faceMap.get(NConstants.IdProperties.OS),
//                faceMap == null ? null : faceMap.get(NConstants.IdProperties.OS_DIST),
//                faceMap == null ? null : faceMap.get(NConstants.IdProperties.PLATFORM),
//                faceMap == null ? null : faceMap.get(NConstants.IdProperties.DESKTOP)
//        );
//    }
//
//    public static NIdFilter idFilterOf(Map<String, String> map, NIdFilter idFilter, NDescriptorFilter
//            descriptorFilter) {
//        return NIdFilters.of().nonnull(idFilter).and(
//                createNutsDescriptorFilter(map).and(descriptorFilter).to(NIdFilter.class)
//        );
//    }
//
//    public static NDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osDist, String
//            platform, String desktopEnv) {
//        NDescriptorFilters d = NDescriptorFilters.of();
//        return d.byArch(arch)
//                .and(d.byOs(os))
//                .and(d.byOsDist(osDist))
//                .and(d.byPlatform(platform))
//                .and(d.byDesktopEnvironment(desktopEnv))
//                ;
//    }
//
//    public static NIdFilter idFilterOf(Map<String, String> map, NIdFilter idFilter, NDefinitionFilter
//            descriptorFilter) {
//        return NIdFilters.of().nonnull(idFilter).and(
//                CoreFilterUtils.createNutsDefinitionFilter(map).and(descriptorFilter).to(NIdFilter.class)
//        );
//    }
//}
