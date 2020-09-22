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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime.filters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyFilterAnd;
import net.vpc.app.nuts.runtime.filters.dependency.NutsDependencyFilterOr;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterAnd;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterArch;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterOr;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterOs;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterOsdist;
import net.vpc.app.nuts.runtime.filters.descriptor.NutsDescriptorFilterPlatform;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterAnd;
import net.vpc.app.nuts.runtime.filters.id.NutsIdFilterOr;
import net.vpc.app.nuts.runtime.filters.id.NutsDescriptorIdFilter;
import net.vpc.app.nuts.runtime.filters.id.NutstVersionIdFilter;
import net.vpc.app.nuts.runtime.filters.repository.NutsRepositoryFilterAnd;
import net.vpc.app.nuts.runtime.filters.repository.NutsRepositoryFilterOr;
import net.vpc.app.nuts.runtime.filters.version.NutsVersionFilterAnd;
import net.vpc.app.nuts.runtime.filters.version.NutsVersionFilterOr;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.io.NutsIdFilterTopInstalled;

/**
 * @author vpc
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

    public static NutsInstallStatus getTopLevelFilterInstallStatus(NutsIdFilter idFilter) {
        NutsIdFilterTopInstalled z = (NutsIdFilterTopInstalled) getTopLevelFilterByType(idFilter, NutsIdFilterTopInstalled.class);
        return z == null ? null : z.getInstallStatus();
    }

    public static NutsIdFilter getTopLevelFilterByType(NutsIdFilter idFilter, Class clazz) {
        if (clazz.isInstance(idFilter)) {
            return idFilter;
        }
        if (idFilter instanceof NutsIdFilterAnd) {
            for (NutsIdFilter child : ((NutsIdFilterAnd) idFilter).getChildren()) {
                if (clazz.isInstance(child)) {
                    return child;
                } else if (child instanceof NutsIdFilterAnd) {
                    return getTopLevelFilterByType(child, clazz);
                }
            }
        }
        return null;
    }

    public static NutsIdFilter idFilterOf(Map<String, String> map, NutsIdFilter idFilter, NutsDescriptorFilter descriptorFilter) {
        return
                CoreFilterUtils.AndSimplified(
                        idFilter,
                        CoreFilterUtils.idFilterOf(
                                CoreFilterUtils.AndSimplified(CoreFilterUtils.createNutsDescriptorFilter(map), descriptorFilter)
                        )
                );
    }

    public static NutsDescriptorFilter Or(NutsDescriptorFilter... all) {
        return new NutsDescriptorFilterOr(all);
    }

    public static NutsIdFilter Or(NutsIdFilter... all) {
        return new NutsIdFilterOr(all);
    }

    public static NutsVersionFilter Or(NutsVersionFilter... all) {
        return new NutsVersionFilterOr(all);
    }

    public static NutsDependencyFilter Or(NutsDependencyFilter... all) {
        return new NutsDependencyFilterOr(all);
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(String arch, String os, String osdist, String platform) {
        return AndSimplified(new NutsDescriptorFilterArch(arch), new NutsDescriptorFilterOs(os), new NutsDescriptorFilterOsdist(osdist), new NutsDescriptorFilterPlatform(platform));
    }

    public static NutsDescriptorFilter createNutsDescriptorFilter(Map<String, String> faceMap) {
        return createNutsDescriptorFilter(faceMap == null ? null : faceMap.get("arch"), faceMap == null ? null : faceMap.get("os"), faceMap == null ? null : faceMap.get("osdist"), faceMap == null ? null : faceMap.get("platform"));
    }

    public static <T> Predicate<NutsId> createFilter(NutsIdFilter t, NutsSession session) {
        if (t == null) {
            return null;
        }
        return new Predicate<NutsId>() {
            @Override
            public boolean test(NutsId value) {
                return t.accept(value, session);
            }
        };
    }

    public static NutsIdFilter And(NutsIdFilter... all) {
        return new NutsIdFilterAnd(all);
    }

    public static NutsDescriptorFilter AndSimplified(NutsDescriptorFilter... all) {
        return CoreNutsUtils.simplify(And(all));
    }

    public static NutsDescriptorFilter OrSimplified(NutsDescriptorFilter... all) {
        return CoreNutsUtils.simplify(Or(all));
    }

    public static NutsIdFilter AndSimplified(NutsIdFilter... all) {
        return CoreNutsUtils.simplify(And(all));
    }

    public static NutsIdFilter OrSimplified(NutsIdFilter... all) {
        return CoreNutsUtils.simplify(Or(all));
    }

    public static NutsRepositoryFilter AndSimplified(NutsRepositoryFilter... all) {
        return CoreNutsUtils.simplify(And(all));
    }

    public static NutsRepositoryFilter OrSimplified(NutsRepositoryFilter... all) {
        return CoreNutsUtils.simplify(Or(all));
    }

    public static NutsDependencyFilter AndSimplified(NutsDependencyFilter... all) {
        return CoreNutsUtils.simplify(And(all));
    }

    public static NutsDependencyFilter OrSimplified(NutsDependencyFilter... all) {
        return CoreNutsUtils.simplify(Or(all));
    }

    public static NutsDescriptorFilter And(NutsDescriptorFilter... all) {
        return new NutsDescriptorFilterAnd(all);
    }

    public static NutsVersionFilter And(NutsVersionFilter... all) {
        return new NutsVersionFilterAnd(all);
    }

    public static NutsRepositoryFilter And(NutsRepositoryFilter... all) {
        return new NutsRepositoryFilterAnd(all);
    }

    public static NutsRepositoryFilter Or(NutsRepositoryFilter... all) {
        return new NutsRepositoryFilterOr(all);
    }

    public static NutsDependencyFilter And(NutsDependencyFilter... all) {
        return new NutsDependencyFilterAnd(all);
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
        NutsId _v = CoreNutsUtils.parseNutsId(packaging);
        NutsId _v2 = CoreNutsUtils.parseNutsId(desc.getPackaging());
        if (_v == null || _v2 == null) {
            return _v == _v2;
        }
        if (_v.equalsShortName(_v2)) {
            if (_v.getVersion().filter().accept(_v2.getVersion(), session)) {
                return true;
            }
        }
        return false;
    }

    public static boolean matchesArch(String arch, NutsDescriptor desc, NutsSession session) {
        if (CoreStringUtils.isBlank(arch)) {
            return true;
        }
        NutsId _v = CoreNutsUtils.parseNutsId(arch);
        String[] all = desc.getArch();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = NutsWorkspaceUtils.parseRequiredNutsId0(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().accept(_v.getVersion(), session)) {
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
        NutsId _v = CoreNutsUtils.parseNutsId(os);
        String[] all = desc.getOs();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = NutsWorkspaceUtils.parseRequiredNutsId0(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().accept(_v.getVersion(), session)) {
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
        NutsId _v = CoreNutsUtils.parseNutsId(osdist);
        String[] all = desc.getOsdist();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = NutsWorkspaceUtils.parseRequiredNutsId0(v);
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().accept(_v.getVersion(), session)) {
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
        NutsId _v = CoreNutsUtils.parseNutsId(platform);
        String[] all = desc.getPlatform();
        if (all != null && all.length > 0) {
            for (String v : all) {
                if (CoreStringUtils.isBlank(v)) {
                    return true;
                }
                NutsId y = NutsWorkspaceUtils.parseRequiredNutsId0(v);
                if (y.getShortName().equals("java")) {
                    //should accept any platform !!!
                    return true;
                }
                if (y.equalsShortName(_v)) {
                    if (y.getVersion().filter().accept(_v.getVersion(), session)) {
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
            if (dependencyFilter.accept(from, nutsDependency, session)) {
                r.add(nutsDependency);
            }
        }
        return r.toArray(new NutsDependency[0]);
    }
}
