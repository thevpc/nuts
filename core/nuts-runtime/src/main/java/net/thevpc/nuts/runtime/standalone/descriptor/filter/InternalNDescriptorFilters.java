//package net.thevpc.nuts.runtime.standalone.descriptor.filter;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.NConstants;
//import net.thevpc.nuts.ext.NExtensions;
//import net.thevpc.nuts.runtime.standalone.util.filters.InternalNTypedFilters;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//
//import net.thevpc.nuts.spi.NSupportLevelContext;
//import net.thevpc.nuts.util.NAssert;
//import net.thevpc.nuts.util.NFilter;
//
//public class InternalNDescriptorFilters extends InternalNTypedFilters<NDescriptorFilter>
//        implements NDescriptorFilters {
//
//
//    public InternalNDescriptorFilters(NWorkspace workspace) {
//        super(NDescriptorFilter.class);
////        localModel = model.getShared(LocalModel.class, () -> new LocalModel(ws));
//    }
//
//    @Override
//    public NDescriptorFilter always() {
//        return new NDescriptorFilterTrue();
//    }
//
//    @Override
//    public NDescriptorFilter never() {
//        return new NDescriptorFilterFalse();
//    }
//
//    @Override
//    public NDescriptorFilter not(NFilter other) {
//        return new NDescriptorFilterNone((NDescriptorFilter) other);
//    }
//
//    @Override
//    public NDescriptorFilter byPackaging(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterPackaging(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byArch(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterArch(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byOsDist(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterOsDist(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byOs(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterOs(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byPlatform(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterPlatform(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byDesktopEnvironment(String... values) {
//        if (values == null || values.length == 0) {
//            return always();
//        }
//        List<NDescriptorFilter> packs = new ArrayList<>();
//        for (String v : values) {
//            packs.add(new NDescriptorFilterDesktopEnvironment(v));
//        }
//        if (packs.size() == 1) {
//            return packs.get(0);
//        }
//        return all(packs.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byFlag(NDescriptorFlag... flags) {
//        return new NDescriptorFlagsIdFilter(flags);
//    }
//
//    @Override
//    public NDescriptorFilter byExtension(NVersion targetApiVersion) {
//        return new NExecExtensionFilter(
//                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build()
//        );
//    }
//
//    @Override
//    public NDescriptorFilter byRuntime(NVersion targetApiVersion) {
//        return new NDescriptorExecRuntimeFilter(
//                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build(),
//                false
//        );
//    }
//
//    @Override
//    public NDescriptorFilter byCompanion(NVersion targetApiVersion) {
//        return new NDescriptorExecCompanionFilter(
//                targetApiVersion == null ? null : NId.get(NConstants.Ids.NUTS_API).get().builder().setVersion(targetApiVersion).build(),
//                NExtensions.of().getCompanionIds().stream().map(NId::getShortName).toArray(String[]::new)
//        );
//    }
//
//    @Override
//    public NDescriptorFilter byApiVersion(NVersion apiVersion) {
//        if (apiVersion == null) {
//            apiVersion = NWorkspace.of().getApiVersion();
//        }
//        return new NutsAPINDescriptorFilter(
//                apiVersion
//        );
//    }
//
//    @Override
//    public NDescriptorFilter byBootVersion(NVersion bootVersion) {
//        if (bootVersion == null) {
//            bootVersion = NWorkspace.of().getBootVersion();
//        }
//        return new NutsBootNDescriptorFilter(
//                bootVersion
//        );
//    }
//
//    @Override
//    public NDescriptorFilter byLockedIds(String... ids) {
//        return new NLockedIdExtensionFilter(
//                Arrays.stream(ids).map(x -> NId.get(x).get()).toArray(NId[]::new)
//        );
//    }
//
//    @Override
//    public NDescriptorFilter as(NFilter a) {
//        if (a instanceof NDescriptorFilter) {
//            return (NDescriptorFilter) a;
//        }
//        if (a instanceof NIdFilter) {
//            return new NDescriptorFilterById((NIdFilter) a);
//        }
//        return null;
//    }
//
//    @Override
//    public NDescriptorFilter from(NFilter a) {
//        if (a == null) {
//            return null;
//        }
//        NDescriptorFilter t = as(a);
//        NAssert.requireNonNull(t, "DescriptorFilter");
//        return t;
//    }
//
//    @Override
//    public NDescriptorFilter all(NFilter... others) {
//        List<NDescriptorFilter> all = convertList(others);
//        for (int i = all.size() - 1; i >= 0; i--) {
//            NDescriptorFilter c = (NDescriptorFilter) all.get(i).simplify();
//            if (c != null) {
//                if (c.equals(always())) {
//                    if (all.size() > 1) {
//                        all.remove(i);
//                    }
//                } else if (c.equals(never())) {
//                    return never();
//                }
//            } else {
//                all.remove(i);
//            }
//        }
//        if (all.isEmpty()) {
//            return always();
//        }
//        if (all.size() == 1) {
//            return all.get(0);
//        }
//        return new NDescriptorFilterAnd(all.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter any(NFilter... others) {
//        List<NDescriptorFilter> all = convertList(others);
//        for (int i = all.size() - 1; i >= 0; i--) {
//            NDescriptorFilter c = (NDescriptorFilter) all.get(i).simplify();
//            if (c != null) {
//                if (c.equals(never())) {
//                    if (all.size() > 1) {
//                        all.remove(i);
//                    }
//                } else if (c.equals(always())) {
//                    return always();
//                }
//            } else {
//                all.remove(i);
//            }
//        }
//        if (all.isEmpty()) {
//            return always();
//        }
//        if (all.size() == 1) {
//            return all.get(0);
//        }
//        return new NDescriptorFilterOr(all.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter none(NFilter... others) {
//        List<NDescriptorFilter> all = convertList(others);
//        if (all.isEmpty()) {
//            return always();
//        }
//        return new NDescriptorFilterNone(all.toArray(new NDescriptorFilter[0]));
//    }
//
//    @Override
//    public NDescriptorFilter parse(String expression) {
//        return new NDescriptorFilterParser(expression).parse();
//    }
//
//    @Override
//    public int getSupportLevel(NSupportLevelContext context) {
//        return NConstants.Support.DEFAULT_SUPPORT;
//    }
//
//    @Override
//    public NDescriptorFilter byPackaging(List<String> values) {
//        return byPackaging(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byArch(List<String> values) {
//        return byArch(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byOs(List<String> values) {
//        return byOs(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byOsDist(List<String> values) {
//        return byOsDist(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byPlatform(List<String> values) {
//        return byPlatform(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byDesktopEnvironment(List<String> values) {
//        return byDesktopEnvironment(values.toArray(new String[0]));
//    }
//
//    @Override
//    public NDescriptorFilter byFlag(List<NDescriptorFlag> flags) {
//        return byFlag(flags.toArray(new NDescriptorFlag[0]));
//    }
//}
