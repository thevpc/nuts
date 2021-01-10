/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.core.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.InstalledVsNonInstalledSearch;
import net.thevpc.nuts.runtime.standalone.util.io.NutsInstallStatusIdFilter;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

/**
 * @author thevpc
 */
public class CoreFilterUtils {

    public static NutsIdFilter idFilterOf(NutsDescriptorFilter other) {
        if (other == null) {
            return null;
        }
        return new NutsDescriptorIdFilter(other);
    }

//    public static NutsIdFilter idFilterOf(NutsVersionFilter other) {
//        return new NutstVersionIdFilter(other);
//    }
//
//    private static boolean isNutsInstallStatusIdFilter(NutsFilter filter) {
//        if (filter instanceof NutsInstallStatusIdFilter) {
//            return true;
//        }
//        if (filter instanceof NutsIdFilterAnd) {
//            return Arrays.stream(((NutsIdFilterAnd) filter).getChildren()).allMatch(CoreFilterUtils::isNutsInstallStatusIdFilter);
//        }
//        if (filter instanceof NutsIdFilterOr) {
//            return Arrays.stream(((NutsIdFilterOr) filter).getChildren()).allMatch(CoreFilterUtils::isNutsInstallStatusIdFilter);
//        }
//        if (filter instanceof NutsIdFilterNone) {
//            return Arrays.stream(((NutsIdFilterNone) filter).getChildren()).allMatch(CoreFilterUtils::isNutsInstallStatusIdFilter);
//        }
//        return false;
//    }

//    private static NutsInstallStatusFilter2 toFilter(Predicate<NutsInstallStatus> a) {
//        if (a instanceof NutsInstallStatusFilter2) {
//            return (NutsInstallStatusFilter2) a;
//        }
//        return NutsInstallStatusFilter2.ANY;
//    }

    private static int andInts(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return b ? 1 : -1;
        } else if (b == null) {
            return a ? 1 : -1;
        } else if (a.equals(b)) {
            return a ? 1 : -1;
        } else {
            return 2;
        }
    }

    private static int orInts(Boolean a, Boolean b) {
        if (a == null && b == null) {
            return 0;
        } else if (a == null) {
            return b ? 1 : -1;
        } else if (b == null) {
            return a ? 1 : -1;
        } else if (a.equals(b)) {
            return a ? 1 : -1;
        } else {
            return 0;
        }
    }

//    public static NutsInstallStatusFilter2 andOrNull(NutsInstallStatusFilter2 me, NutsInstallStatusFilter2 other) {
//        if (me == null && other == null) {
//            return NutsInstallStatusFilter2.ANY;
//        }
//        if (me == null) {
//            return other;
//        }
//        if (other == null) {
//            return me;
//        }
//        int _installed = andInts(me.getInstalled(), other.getInstalled());
//        if (_installed == 2) {
//            return null;
//        }
//        int _required = andInts(me.getRequired(), other.getRequired());
//        if (_required == 2) {
//            return null;
//        }
//        int _obsolete = andInts(me.getObsolete(), other.getObsolete());
//        if (_obsolete == 2) {
//            return null;
//        }
//        int _defaultVersion = andInts(me.getDefaultVersion(), other.getDefaultVersion());
//        if (_defaultVersion == 2) {
//            return null;
//        }
//        return NutsInstallStatusFilter2.of(
//                _installed == 1 ? Boolean.TRUE : _installed == -1 ? Boolean.FALSE : null,
//                _required == 1 ? Boolean.TRUE : _required == -1 ? Boolean.FALSE : null,
//                _obsolete == 1 ? Boolean.TRUE : _obsolete == -1 ? Boolean.FALSE : null,
//                _defaultVersion == 1 ? Boolean.TRUE : _defaultVersion == -1 ? Boolean.FALSE : null
//        );
//    }

//    public static NutsInstallStatusFilter2 orAll(Set<Predicate<NutsInstallStatus>> aa) {
//        NutsInstallStatusFilter2 x = null;
//        for (Predicate<NutsInstallStatus> a : aa) {
//            NutsInstallStatusFilter2 r = toFilter(a);
//            if (x == null) {
//                x = r;
//            } else {
//                x = or(x, r);
//            }
//        }
//        if (x == null) {
//            x = NutsInstallStatusFilter2.ANY;
//        }
//        return x;
//    }

//    public static NutsInstallStatusFilter2 andAll(Set<Predicate<NutsInstallStatus>> aa) {
//        NutsInstallStatusFilter2 x = null;
//        for (Predicate<NutsInstallStatus> a : aa) {
//            NutsInstallStatusFilter2 r = toFilter(a);
//            if (x == null) {
//                x = r;
//            } else {
//                x = or(x, r);
//            }
//        }
//        if (x == null) {
//            x = NutsInstallStatusFilter2.ANY;
//        }
//        return x;
//    }

//    public static NutsInstallStatusFilter2 or(NutsInstallStatusFilter2 me, NutsInstallStatusFilter2 other) {
//        if (me == null && other == null) {
//            return NutsInstallStatusFilter2.ANY;
//        }
//        if (me == null) {
//            return other;
//        }
//        if (other == null) {
//            return me;
//        }
//        int _installed = andInts(me.getInstalled(), other.getInstalled());
//        if (_installed == 2) {
//            return null;
//        }
//        int _required = andInts(me.getRequired(), other.getRequired());
//        if (_required == 2) {
//            return null;
//        }
//        int _obsolete = andInts(me.getObsolete(), other.getObsolete());
//        if (_obsolete == 2) {
//            return null;
//        }
//        int _defaultVersion = andInts(me.getDefaultVersion(), other.getDefaultVersion());
//        if (_defaultVersion == 2) {
//            return null;
//        }
//        return NutsInstallStatusFilter2.of(
//                _installed == 1 ? Boolean.TRUE : _installed == -1 ? Boolean.FALSE : null,
//                _required == 1 ? Boolean.TRUE : _required == -1 ? Boolean.FALSE : null,
//                _obsolete == 1 ? Boolean.TRUE : _obsolete == -1 ? Boolean.FALSE : null,
//                _defaultVersion == 1 ? Boolean.TRUE : _defaultVersion == -1 ? Boolean.FALSE : null
//        );
//    }

//    private static NutsInstallStatusFilter2 resolveNutsInstallStatusUsage(NutsFilter filter) {
//        if (filter instanceof NutsInstallStatusIdFilter) {
//            return toFilter(((NutsInstallStatusIdFilter) filter).getInstallStatus());
//        }
//        if (filter instanceof NutsIdFilterAnd) {
//            NutsInstallStatusFilter2 a = null;
//            for (NutsIdFilter child : ((NutsIdFilterAnd) filter).getChildren()) {
//                NutsInstallStatusFilter2 r = resolveNutsInstallStatusUsage(child);
//                a = a == null ? r : andOrNull(a, r);
//                if (a == null) {
//                    a = NutsInstallStatusFilter2.ANY;
//                }
//            }
//            if (a == null) {
//                a = NutsInstallStatusFilter2.ANY;
//            }
//        }
//        if (filter instanceof NutsIdFilterOr) {
//            NutsInstallStatusFilter2 a = null;
//            for (NutsIdFilter child : ((NutsIdFilterOr) filter).getChildren()) {
//                NutsInstallStatusFilter2 r = resolveNutsInstallStatusUsage(child);
//                a = a == null ? r : or(a, r);
//            }
//            if (a == null) {
//                a = NutsInstallStatusFilter2.ANY;
//            }
//        }
//
//        if (filter instanceof NutsIdFilterNone) {
//            NutsInstallStatusFilter2 a = null;
//            for (NutsIdFilter child : ((NutsIdFilterNone) filter).getChildren()) {
//                NutsInstallStatusFilter2 r = resolveNutsInstallStatusUsage(child);
//                r = NutsInstallStatusFilter2.of(
//                        r.isInstalled() ? false : r.isNotInstalled() ? true : null,
//                        r.isRequired() ? false : r.isNotRequired() ? true : null,
//                        r.isObsolete() ? false : r.isNotObsolete() ? true : null,
//                        r.isDefaultVersion() ? false : r.isNotDefaultVersion() ? true : null
//                );
//                a = a == null ? r : or(a, r);
//            }
//            if (a == null) {
//                a = NutsInstallStatusFilter2.ANY;
//            }
//        }
//        return NutsInstallStatusFilter2.ANY;
//    }

//    public static InstalledVsNonInstalledSearch getTopLevelInstallRepoInclusion(NutsIdFilter filter) {
//        NutsInstallStatusFilter2 s = resolveNutsInstallStatusUsage(filter);
//        boolean searchInInstalledRepos=!(s.isNotRequired() && s.isNotInstalled());
//        boolean searchInOtherRepos=!(s.isInstalled() || s.isRequired() || s.isDefaultVersion());
//        return new InstalledVsNonInstalledSearch(
//                searchInInstalledRepos,
//                searchInOtherRepos
//        );
//    }

    public static <T extends NutsFilter> T[]
    getTopLevelFilters(NutsFilter idFilter, Class<T> clazz, NutsWorkspace ws) {
        return Arrays.stream(getTopLevelFilters(idFilter))
                .map(x -> ws.filters().as(clazz, x))
                .toArray(value -> (T[]) Array.newInstance(clazz, value));
    }

    public static NutsFilter[] getTopLevelFilters(NutsFilter idFilter) {
        if (idFilter == null) {
            return new NutsFilter[0];
        }
        if (idFilter.getFilterOp() == NutsFilterOp.AND) {
            return idFilter.getSubFilters();
        }
        return new NutsFilter[]{idFilter};
    }

    public static NutsIdFilter idFilterOf(Map<String, String> map, NutsIdFilter idFilter, NutsDescriptorFilter
            descriptorFilter, NutsWorkspace ws) {
        return (NutsIdFilter) ws.id().filter().nonnull(idFilter).and(
                CoreFilterUtils.createNutsDescriptorFilter(map, ws).and(descriptorFilter).to(NutsIdFilter.class)
        );
    }


    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osdist, String
            platform, NutsWorkspace ws) {
        NutsDescriptorFilterManager d = ws.descriptor().filter();
        return (NutsDescriptorFilter) d.byArch(arch).and(d.byOsdist(osdist)).and(d.byPlatform(platform));
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap, NutsWorkspace ws) {
        return createNutsDescriptorFilter(faceMap == null ? null : faceMap.get("arch"), faceMap == null ? null : faceMap.get("os"), faceMap == null ? null : faceMap.get("osdist"), faceMap == null ? null : faceMap.get("platform"), ws);
    }

    public static <T> Predicate<NutsId> createFilter(NutsIdFilter t, NutsSession session) {
        if (t == null) {
            return null;
        }
        return new NutsIdFilterToPredicate(t, session);
    }


    public static List<NutsExtensionInformation> filterNutsExtensionInfoByLatestVersion
            (List<NutsExtensionInformation> base) {
        LinkedHashMap<String, NutsExtensionInformation> valid = new LinkedHashMap<>();
        for (NutsExtensionInformation n : base) {
            NutsExtensionInformation old = valid.get(n.getId().getShortName());
            if (old == null || old.getId().getVersion().compareTo(n.getId().getVersion()) < 0) {
                valid.put(n.getId().getShortName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static List<NutsId> filterNutsIdByLatestVersion(List<NutsId> base) {
        LinkedHashMap<String, NutsId> valid = new LinkedHashMap<>();
        for (NutsId n : base) {
            NutsId old = valid.get(n.getShortName());
            if (old == null || old.getVersion().compareTo(n.getVersion()) < 0) {
                valid.put(n.getShortName(), n);
            }
        }
        return new ArrayList<>(valid.values());
    }

    public static boolean matchesPackaging(String packaging, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(packaging)) {
            return true;
        }
        if (CoreStringUtils.isBlank(desc.getPackaging())) {
            return true;
        }
        NutsIdParser parser = session.getWorkspace().id().parser();
        NutsId _v = parser.parse(packaging);
        NutsId _v2 = parser.parse(desc.getPackaging());
        if (_v == null || _v2 == null) {
            return _v == _v2;
        }
        if (_v.equalsShortName(_v2)) {
            if (_v.getVersion().filter().acceptVersion(_v2.getVersion(), session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesArch(String arch, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(arch)) {
            return true;
        }
        NutsIdParser parser = session.getWorkspace().id().parser();
        NutsId _v = parser.parse(arch);
        String[] all = desc.getArch();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = parser.setLenient(false).parse(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().acceptVersion(_v.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOs(String os, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(os)) {
            return true;
        }
        NutsIdParser parser = session.getWorkspace().id().parser();
        NutsId _v = parser.parse(os);
        String[] all = desc.getOs();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = parser.setLenient(false).parse(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().acceptVersion(_v.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOsdist(String osdist, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(osdist)) {
            return true;
        }
        NutsIdParser parser = session.getWorkspace().id().parser();
        NutsId _v = parser.parse(osdist);
        String[] all = desc.getOsdist();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = parser.setLenient(false).parse(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().acceptVersion(_v.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }

    }

    public static boolean matchesPlatform(String platform, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(platform)) {
            return true;
        }
        NutsIdParser parser = session.getWorkspace().id().parser();
        NutsId _v = parser.parse(platform);
        String[] all = desc.getPlatform();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = parser.setLenient(false).parse(v);
                if (y.getShortName().equals("java")) {
                    //should accept any platform !!!
                    return true;
                }
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().acceptVersion(_v.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesEnv(String arch, String os, String dist, String platform, NutsDescriptor
            desc, NutsSession session) {
        if (!matchesArch(arch, desc, session)) {
            return false;
        }
        if (!matchesOs(os, desc, session)) {
            return false;
        }
        if (!matchesOsdist(dist, desc, session)) {
            return false;
        }
        if (!matchesPlatform(platform, desc, session)) {
            return false;
        }
        return true;
    }

    public static NutsDependency[] filterDependencies(NutsId from, NutsDependency[] d0, NutsDependencyFilter
            dependencyFilter, NutsSession session) {
        if (dependencyFilter == null) {
            return d0;
        }
        List<NutsDependency> r = new ArrayList<>(d0.length);
        for (NutsDependency nutsDependency : d0) {
            if (dependencyFilter.acceptDependency(from, nutsDependency, session)) {
                r.add(nutsDependency);
            }
        }
        return r.toArray(new NutsDependency[0]);
    }

    private static class NutsIdFilterToPredicate extends NutsPredicates.BasePredicate<NutsId> {
        private final NutsIdFilter t;
        private final NutsSession session;

        public NutsIdFilterToPredicate(NutsIdFilter t, NutsSession session) {
            this.t = t;
            this.session = session;
        }

        @Override
        public boolean test(NutsId value) {
            return t.acceptId(value, session);
        }

        @Override
        public String toString() {
            return t.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NutsIdFilterToPredicate that = (NutsIdFilterToPredicate) o;
            return Objects.equals(t, that.t) && Objects.equals(session, that.session);
        }

        @Override
        public int hashCode() {
            return Objects.hash(t, session);
        }
    }
}