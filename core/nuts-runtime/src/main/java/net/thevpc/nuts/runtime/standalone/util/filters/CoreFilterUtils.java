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
package net.thevpc.nuts.runtime.standalone.util.filters;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsEnvCondition;
import net.thevpc.nuts.runtime.standalone.id.NutsIdListHelper;
import net.thevpc.nuts.runtime.standalone.util.Simplifiable;
import net.thevpc.nuts.runtime.standalone.xtra.expr.CommaStringParser;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class CoreFilterUtils {

//    public static NutsIdFilter idFilterOf(NutsDescriptorFilter other) {
//        if (other == null) {
//            return null;
//        }
//        return new NutsDescriptorIdFilter(other);
//    }

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
    getTopLevelFilters(NutsFilter idFilter, Class<T> clazz, NutsSession ws) {
        return Arrays.stream(getTopLevelFilters(idFilter))
                .map(x -> NutsFilters.of(ws).as(clazz, x))
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
            descriptorFilter, NutsSession ws) {
        return (NutsIdFilter) NutsIdFilters.of(ws).nonnull(idFilter).and(
                CoreFilterUtils.createNutsDescriptorFilter(map, ws).and(descriptorFilter).to(NutsIdFilter.class)
        );
    }


    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osDist, String
            platform, String desktopEnv, NutsSession session) {
        NutsDescriptorFilters d = NutsDescriptorFilters.of(session);
        return (NutsDescriptorFilter) d.byArch(arch)
                .and(d.byOs(os))
                .and(d.byOsDist(osDist))
                .and(d.byPlatform(platform))
                .and(d.byDesktopEnvironment(desktopEnv))
                ;
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap, NutsSession ws) {
        return createNutsDescriptorFilter(
                faceMap == null ? null : faceMap.get(NutsConstants.IdProperties.ARCH),
                faceMap == null ? null : faceMap.get(NutsConstants.IdProperties.OS),
                faceMap == null ? null : faceMap.get(NutsConstants.IdProperties.OS_DIST),
                faceMap == null ? null : faceMap.get(NutsConstants.IdProperties.PLATFORM),
                faceMap == null ? null : faceMap.get(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT),
                ws);
    }

    public static <T> NutsPredicate<NutsId> createFilter(NutsIdFilter t, NutsSession session) {
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
        if (NutsBlankable.isBlank(packaging)) {
            return true;
        }
        if (NutsBlankable.isBlank(desc.getPackaging())) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId _v = parser.parse(packaging);
        NutsId _v2 = parser.parse(desc.getPackaging());
        if (_v == null || _v2 == null) {
            return _v == _v2;
        }
        if (_v.equalsShortId(_v2)) {
            if (_v.getVersion().filter().acceptVersion(_v2.getVersion(), session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean acceptDependency(NutsDependency dep, NutsSession session) {
        if(CoreFilterUtils.acceptCondition(dep.getCondition(), false, session)) {
            // fast reject jfx dependencies with different environment defined by classifier!
            if(dep.getGroupId().equals("org.openjfx") && dep.getArtifactId().startsWith("javafx")){
                String c = NutsUtilStrings.trim(dep.getClassifier());
                if(c.length()>0){
                    String[] a = c.split("-");
                    if(a.length>0){
                        NutsOsFamily o = NutsOsFamily.parseLenient(a[0], null);
                        if(o!=null){
                            if(o!=session.env().getOsFamily()){
                                return false;
                            }
                        }
                        if(a.length>1){
                            NutsArchFamily af = NutsArchFamily.parseLenient(a[1], null);
                            if(af!=null){
                                if(af!=session.env().getArchFamily()){
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            return true;
        }
        return false;
    }

    public static boolean acceptCondition(NutsEnvCondition envCond, boolean currentVMOnLy, NutsSession session) {
        if(envCond==null || envCond.isBlank()){
            return true;
        }
        NutsWorkspaceEnvManager env = session.env();
        if(!matchesArch(
                env.getArchFamily().id(),
                envCond.getArch(), session
        )){
            return false;
        }
        if(!matchesOs(
                env.getOsFamily().id(),
                envCond.getOs(), session
        )){
            return false;
        }
        if(!matchesOsDist(
                env.getOsDist().toString(),
                envCond.getOsDist(), session
        )){
            return false;
        }
        if(currentVMOnLy){
            if(!matchesPlatform(
                    env.getPlatform().toString(),
                    envCond.getPlatform(), session
            )){
                return false;
            }
        }else{
            if(!matchesPlatform(
                    env.platforms().findPlatforms(),
                    envCond.getPlatform(), session
            )){
                return false;
            }
        }

        if(!matchesDesktopEnvironment(
                env.getDesktopEnvironments(),
                envCond.getDesktopEnvironment(), session
        )){
            return false;
        }
        if(!matchesProperties(
                ((DefaultNutsEnvCondition)envCond).getProperties(), session
        )){
            return false;
        }
        return true;
    }

    private static boolean matchesProperties(Map<String, String> props, NutsSession session) {
        for (Map.Entry<String, String> kv : props.entrySet()) {
            if(!matchesProperty(kv.getKey(),kv.getValue(),session)){
                return false;
            }
        }
        return true;
    }

    private static boolean matchesProperty(String k, String expected, NutsSession session) {
        //maven always checks System Props
        String f = System.getProperty(k);
        if(expected==null){
            return f!=null;
        }
        expected=NutsUtilStrings.trim(expected);
        f=NutsUtilStrings.trim(f);
        if(expected.startsWith("!")){
            expected=expected.substring(1).trim();
            return !expected.equals(f);
        }
        return expected.equals(f);
    }

    public static boolean matchesArch(String current, String[] allConds, NutsSession session) {
        if (NutsBlankable.isBlank(current)) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId currentId = parser.parse(current);
        if (allConds != null && allConds.length > 0) {
            for (String cond : allConds) {
                if (NutsBlankable.isBlank(cond)) {
                    return true;
                }
                NutsId idCond = parser.setLenient(false).parse(cond);
                NutsArchFamily w = NutsArchFamily.parseLenient(idCond.getArtifactId(), null, null);
                if(w!=null){
                    idCond=idCond.builder().setArtifactId(w.id()).build();
                }
                if (idCond.equalsShortId(currentId)) {
                    if (idCond.getVersion().filter().acceptVersion(currentId.getVersion(), session)) {
                        return true;
                    }
                }
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOs(String os, String[] allConds, NutsSession session) {
        if (NutsBlankable.isBlank(os)) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId currentId = parser.parse(os);
        if (allConds != null && allConds.length > 0) {
            for (String cond : allConds) {
                if (NutsBlankable.isBlank(cond)) {
                    return true;
                }
                NutsId condId = parser.setLenient(false).parse(cond);
                NutsOsFamily w = NutsOsFamily.parseLenient(condId.getArtifactId(), null, null);
                if(w!=null){
                    condId=condId.builder().setArtifactId(w.id()).build();
                }
                return condId.compatNewer().filter().acceptId(currentId,session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesOsDist(String current, String[] allConds, NutsSession session) {
        if (NutsBlankable.isBlank(current)) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId currentId = parser.parse(current);
        if (allConds != null && allConds.length > 0) {
            for (String cond : allConds) {
                if (NutsBlankable.isBlank(cond)) {
                    return true;
                }
                NutsId y = parser.setLenient(false).parse(cond);
                return y.compatNewer().filter().acceptId(currentId,session);
            }
            return false;
        } else {
            return true;
        }

    }

    public static boolean matchesPlatform(NutsPlatformLocation[] platforms, String[] allCond, NutsSession session) {
        for (NutsPlatformLocation platform : platforms) {
            NutsId id = platform.getId();
            if(id!=null){
                if(matchesPlatform(id.toString(),allCond, session)){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean matchesPlatform(String current, String[] allConds, NutsSession session) {
        if (NutsBlankable.isBlank(current)) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId currentId = parser.parse(current);
        if (allConds != null && allConds.length > 0) {
            for (String cond : allConds) {
                if (NutsBlankable.isBlank(cond)) {
                    return true;
                }
                NutsId idCond = parser.setLenient(false).parse(cond);
                NutsPlatformFamily w = NutsPlatformFamily.parseLenient(idCond.getArtifactId(), null, null);
                if(w!=null){
                    idCond=idCond.builder().setArtifactId(w.id()).build();
                }
                return idCond.compatNewer().filter().acceptId(currentId,session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesDesktopEnvironment(NutsId[] platforms, String[] allConds, NutsSession session) {
        for (NutsId platform : platforms) {
            if(matchesDesktopEnvironment(platform.toString(),allConds,session)){
                return true;
            }
        }
        return false;
    }

    public static boolean matchesDesktopEnvironment(String current, String[] allConds, NutsSession session) {
        if (NutsBlankable.isBlank(current)) {
            return true;
        }
        NutsIdParser parser = NutsIdParser.of(session);
        NutsId currentId = parser.parse(current);
        if (allConds != null && allConds.length > 0) {
            for (String cond : allConds) {
                if (NutsBlankable.isBlank(cond)) {
                    return true;
                }
                NutsId idCond = parser.setLenient(false).parse(cond);
                NutsDesktopEnvironmentFamily w = NutsDesktopEnvironmentFamily.parseLenient(idCond.getArtifactId(), null, null);
                if(w!=null){
                    idCond=idCond.builder().setArtifactId(w.id()).build();
                }
                return idCond.compatNewer().filter().acceptId(currentId,session);
            }
            return false;
        } else {
            return true;
        }
    }

    public static boolean matchesEnv(String arch, String os, String dist, String platform, String de, NutsEnvCondition
            desc, NutsSession session) {
        if (!matchesArch(arch, desc.getArch(), session)) {
            return false;
        }
        if (!matchesOs(os, desc.getOs(), session)) {
            return false;
        }
        if (!matchesOsDist(dist, desc.getOsDist(), session)) {
            return false;
        }
        if (!matchesPlatform(platform, desc.getPlatform(), session)) {
            return false;
        }
        if (!matchesDesktopEnvironment(de, desc.getDesktopEnvironment(), session)) {
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

    public static boolean matchesSimpleNameStaticVersion(NutsId id, NutsId pattern) {
        if (pattern == null) {
            return id == null;
        }
        if (id == null) {
            return false;
        }
        if (pattern.getVersion().isBlank()) {
            return pattern.getShortName().equals(id.getShortName());
        }
        return pattern.getLongName().equals(id.getLongName());
    }

    public static boolean acceptClassifier(NutsIdLocation location, String classifier) {
        if (location == null) {
            return false;
        }
        String c0 = NutsUtilStrings.trim(classifier);
        String c1 = NutsUtilStrings.trim(location.getClassifier());
        return c0.equals(c1);
    }

    public static NutsEnvCondition blankCondition(NutsSession session) {
        return new DefaultNutsEnvCondition(session);
    }

    public static NutsEnvCondition trimToNull(NutsEnvCondition c, NutsSession session) {
        if (c == null || c.isBlank()) {
            return null;
        }
        return c;
    }

    public static NutsEnvCondition trimToBlank(NutsEnvCondition c, NutsSession session) {
        if (c == null) {
            return blankCondition(session);
        }
        return c;
    }

    public static Map<String, String> toMap(NutsEnvCondition condition,NutsSession session) {
        LinkedHashMap<String, String> m = new LinkedHashMap<>();
        String s = Arrays.stream(condition.getArch()).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.ARCH, s);
        }
        s = Arrays.stream(condition.getOs()).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.OS, s);
        }
        s = Arrays.stream(condition.getOsDist()).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.OS_DIST, s);
        }
        s = NutsIdListHelper.formatIdList(condition.getPlatform(),session);
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.PLATFORM, s);
        }
        s = Arrays.stream(condition.getDesktopEnvironment()).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT, s);
        }
        s = Arrays.stream(condition.getProfile()).map(String::trim).filter(x -> !x.isEmpty()).collect(Collectors.joining(","));
        if (!NutsBlankable.isBlank(s)) {
            m.put(NutsConstants.IdProperties.PROFILE, s);
        }
        Map<String, String> properties = ((DefaultNutsEnvCondition) condition).getProperties();
        if(!properties.isEmpty()){
            m.put(/*NutsConstants.IdProperties.PROPERTIES*/"properties", CommaStringParser.formatPropertiesQuery(properties));
        }
        return m;
    }

    public static <T extends NutsFilter> T simplifyFilterOr(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        boolean someFalse = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return NutsFilters.of(ws).always(cls);
                    }
                    case FALSE: {
                        someFalse = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.isEmpty()) {
            if (someFalse) {
                return NutsFilters.of(ws).never(cls);
            }
            return NutsFilters.of(ws).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).any(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NutsFilter> T simplifyFilterAnd(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        if (all.length == 1) {
            return (T) all[0].simplify();
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case FALSE: {
                        return NutsFilters.of(ws).never(cls);
                    }
                    case TRUE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        if (all2.size() == 1) {
            return all2.get(0);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).all(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T extends NutsFilter> T simplifyFilterNone(NutsSession ws, Class<T> cls, T base, NutsFilter... all) {
        if (all.length == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        List<T> all2 = new ArrayList<>();
        boolean updates = false;
        for (NutsFilter t : all) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                switch (t2.getFilterOp()) {
                    case TRUE: {
                        return NutsFilters.of(ws).never(cls);
                    }
                    case FALSE: {
                        updates = true;
                        break;
                    }
                    default: {
                        if (t2 != t) {
                            updates = true;
                        }
                        all2.add(t2);
                    }
                }
            } else {
                updates = true;
            }
        }
        if (all2.size() == 0) {
            return NutsFilters.of(ws).always(cls);
        }
        if (!updates) {
            return base;
        }
        return NutsFilters.of(ws).none(cls, all2.toArray((T[]) Array.newInstance(cls, 0)));
    }

    public static <T> T simplify(T any) {
        if (any == null) {
            return null;
        }
        if (any instanceof Simplifiable) {
            return ((Simplifiable<T>) any).simplify();
        }
        return any;
    }

    public static <T> T[] simplifyAndShrink(Class<T> cls, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = simplify(t);
            if (t2 != null) {
                if (t2 != t) {
                    updates = true;
                }
                all.add(t2);
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, 0));
    }

    public static <T extends NutsFilter> T[] simplifyAndShrinkFilters(Class<T> cls, Predicate<T> onRemove, T... any) {
        List<T> all = new ArrayList<>();
        boolean updates = false;
        for (T t : any) {
            T t2 = t == null ? null : (T) t.simplify();
            if (t2 != null) {
                if (onRemove != null && onRemove.test(t2)) {
                    updates = true;
                } else {
                    if (t2 != t) {
                        updates = true;
                    }
                    all.add(t2);
                }
            } else {
                updates = true;
            }
        }
        if (!updates) {
            return null;
        }
        return all.toArray((T[]) Array.newInstance(cls, 0));
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
        public int hashCode() {
            return Objects.hash(t, session);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NutsIdFilterToPredicate that = (NutsIdFilterToPredicate) o;
            return Objects.equals(t, that.t) && Objects.equals(session, that.session);
        }

        @Override
        public String toString() {
            return t.toString();
        }
    }
}