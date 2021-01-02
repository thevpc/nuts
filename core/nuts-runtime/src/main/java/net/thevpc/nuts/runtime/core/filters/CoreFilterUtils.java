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
 *
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

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.filters.id.*;
import net.thevpc.nuts.runtime.standalone.util.InstalledVsNonInstalledSearch;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.NutsInstallStatusIdFilter;

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

    public static NutsIdFilter idFilterOf(NutsVersionFilter other) {
        return new NutstVersionIdFilter(other);
    }


    public static Set<Set<NutsInstallStatus>> getPossibleInstallStatuses(){
        Set<Set<NutsInstallStatus>> s=new HashSet<>();
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.NOT_INSTALLED)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.REQUIRED)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.REQUIRED)));

        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.REQUIRED,NutsInstallStatus.OBSOLETE)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.OBSOLETE)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.REQUIRED,NutsInstallStatus.OBSOLETE)));

        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.DEFAULT_VERSION)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.REQUIRED,NutsInstallStatus.DEFAULT_VERSION)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.OBSOLETE,NutsInstallStatus.DEFAULT_VERSION)));
        s.add(new HashSet<>(Arrays.asList(NutsInstallStatus.INSTALLED,NutsInstallStatus.REQUIRED,NutsInstallStatus.OBSOLETE,NutsInstallStatus.DEFAULT_VERSION)));

        return s;
    }
    private static boolean isNutsInstallStatusIdFilter(NutsFilter filter){
        if(filter instanceof NutsInstallStatusIdFilter){
            return true;
        }
        if(filter instanceof NutsIdFilterAnd){
            return Arrays.stream(((NutsIdFilterAnd) filter).getChildren()).allMatch(x->isNutsInstallStatusIdFilter(x));
        }
        if(filter instanceof NutsIdFilterOr){
            return Arrays.stream(((NutsIdFilterOr) filter).getChildren()).allMatch(x->isNutsInstallStatusIdFilter(x));
        }
        if(filter instanceof NutsIdFilterNone){
            return Arrays.stream(((NutsIdFilterNone) filter).getChildren()).allMatch(x->isNutsInstallStatusIdFilter(x));
        }
        return false;
    }

    private static Set<Set<NutsInstallStatus>> resolveNutsInstallStatusIdFilter(NutsFilter filter){
        if(filter instanceof NutsInstallStatusIdFilter){
            return ((NutsInstallStatusIdFilter)filter).getPossibilities();
        }
        if(filter instanceof NutsIdFilterAnd){
            Set<Set<NutsInstallStatus>> ret= getPossibleInstallStatuses();
            for (NutsIdFilter child : ((NutsIdFilterAnd) filter).getChildren()) {
                Set<Set<NutsInstallStatus>> ok = resolveNutsInstallStatusIdFilter(child);
                ret.retainAll(ok);
            }
            return ret;
        }
        if(filter instanceof NutsIdFilterOr){
            Set<Set<NutsInstallStatus>> ret=new HashSet<>();
            for (NutsIdFilter child : ((NutsIdFilterOr) filter).getChildren()) {
                Set<Set<NutsInstallStatus>> ok = resolveNutsInstallStatusIdFilter(child);
                ret.addAll(ok);
            }
            return ret;
        }
        if(filter instanceof NutsIdFilterNone){
            Set<Set<NutsInstallStatus>> ret=new HashSet<>();
            for (NutsIdFilter child : ((NutsIdFilterNone) filter).getChildren()) {
                Set<Set<NutsInstallStatus>> ok = resolveNutsInstallStatusIdFilter(child);
                Set<Set<NutsInstallStatus>> i=new HashSet<>(getPossibleInstallStatuses());
                i.remove(ok);
                ret.addAll(i);
            }
            return ret;
        }
        return getPossibleInstallStatuses();
    }
    public static InstalledVsNonInstalledSearch getTopLevelInstallRepoInclusion(NutsIdFilter filter) {
        Set<Set<NutsInstallStatus>> s = resolveNutsInstallStatusIdFilter(filter);
        boolean notInstalled=false;
        boolean installedOrRequired=false;
        for (Set<NutsInstallStatus> nutsInstallStatuses : s) {
            notInstalled|=nutsInstallStatuses.contains(NutsInstallStatus.NOT_INSTALLED);
            installedOrRequired|=nutsInstallStatuses.contains(NutsInstallStatus.INSTALLED)||nutsInstallStatuses.contains(NutsInstallStatus.REQUIRED);
        }
        return new InstalledVsNonInstalledSearch(
                installedOrRequired,
                notInstalled
        );
    }

    public static <T extends NutsFilter> T[] getTopLevelFilters(NutsFilter idFilter,Class<T> clazz,NutsWorkspace ws) {
        return Arrays.stream(getTopLevelFilters(idFilter))
                .map(x-> ws.filters().as(clazz,x))
                .toArray(value -> (T[]) Array.newInstance(clazz,value));
    }

    public static NutsFilter[] getTopLevelFilters(NutsFilter idFilter) {
        if(idFilter==null){
            return new NutsFilter[0];
        }
        if(idFilter.getFilterOp()==NutsFilterOp.AND){
            return idFilter.getSubFilters();
        }
        return new NutsFilter[]{idFilter};
    }

    public static NutsIdFilter idFilterOf(Map<String, String> map, NutsIdFilter idFilter, NutsDescriptorFilter descriptorFilter,NutsWorkspace ws) {
        return (NutsIdFilter) ws.id().filter().nonnull(idFilter).and(
                CoreFilterUtils.createNutsDescriptorFilter(map,ws).and(descriptorFilter).to(NutsIdFilter.class)
        );
    }


    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osdist, String platform, NutsWorkspace ws) {
        NutsDescriptorFilterManager d = ws.descriptor().filter();
        return (NutsDescriptorFilter) d.byArch(arch).and(d.byOsdist(osdist)).and(d.byPlatform(platform));
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap,NutsWorkspace ws) {
        return createNutsDescriptorFilter(faceMap == null ? null : faceMap.get("arch"), faceMap == null ? null : faceMap.get("os"), faceMap == null ? null : faceMap.get("osdist"), faceMap == null ? null : faceMap.get("platform"), ws);
    }

    public static <T> Predicate<NutsId> createFilter(NutsIdFilter t, NutsSession session) {
        if (t == null) {
            return null;
        }
        return new Predicate<NutsId>() {
            @Override
            public boolean test(NutsId value) {
                return t.acceptId(value, session);
            }
        };
    }






    public static List<NutsExtensionInformation> filterNutsExtensionInfoByLatestVersion(List<NutsExtensionInformation> base) {
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

    public static boolean matchesEnv(String arch, String os, String dist, String platform, NutsDescriptor desc, NutsSession session) {
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

    public static NutsDependency[] filterDependencies(NutsId from, NutsDependency[] d0, NutsDependencyFilter dependencyFilter, NutsSession session) {
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
}
