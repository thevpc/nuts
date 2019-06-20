/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import net.vpc.app.nuts.NutsDependencyFilter;
import net.vpc.app.nuts.NutsDependencyScope;
import net.vpc.app.nuts.NutsDependencyScopePattern;
import net.vpc.app.nuts.core.filters.CoreFilterUtils;
import net.vpc.app.nuts.core.filters.dependency.ScopeNutsDependencyFilter;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 *
 * @author vpc
 */
public class NutsDependencyScopes {

    public static final NutsDependencyFilter SCOPE_RUN = CoreFilterUtils.And(new ScopeNutsDependencyFilter(NutsDependencyScopePattern.RUN), CoreNutsUtils.NON_OPTIONAL);
    public static final NutsDependencyFilter SCOPE_TEST = CoreFilterUtils.And(new ScopeNutsDependencyFilter(NutsDependencyScopePattern.TEST), CoreNutsUtils.NON_OPTIONAL);

    public static String normalizeScope(String s1) {
        if (s1 == null) {
            s1 = "";
        }
        s1 = s1.toLowerCase().trim();
        if (s1.isEmpty()) {
            s1 = NutsDependencyScope.API.id();
        }
        return s1;
    }

    public static int compareScopes(String s1, String s2) {
        int x = getScopesPriority(s1);
        int y = getScopesPriority(s2);
        int c = Integer.compare(x, y);
        if (c != 0) {
            return x;
        }
        if (x == -1) {
            return normalizeScope(s1).compareTo(normalizeScope(s2));
        }
        return 0;
    }

    public static NutsDependencyScope parseScope(String scope, boolean lenient) {
        scope = normalizeScope(scope);
        return CoreCommonUtils.parseEnumString(scope, NutsDependencyScope.class, lenient);
    }

    public static boolean isDefaultScope(String s1) {
        return normalizeScope(s1).equals("compile");
    }

    public static int getScopesPriority(String s1) {
        switch (normalizeScope(s1)) {
            case "compile":
                return 5;
            case "runtime":
                return 4;
            case "provided":
                return 3;
            case "system":
                return 2;
            case "test":
                return 1;
            default:
                return -1;
        }
    }

//    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, Collection<NutsDependencyScopePattern> b) {
//        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
//        EnumSet<NutsDependencyScope> bb = expand(b);
//        aa.addAll(bb);
//        return aa;
//    }

    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, NutsDependencyScopePattern... b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NutsDependencyScope> bb = expand(b == null ? null : Arrays.asList(b));
        aa.addAll(bb);
        return aa;
    }
    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, NutsDependencyScope... b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NutsDependencyScope> bb = (b == null ? Collections.emptyList() : Arrays.asList(b));
        aa.addAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> removeScopes(Collection<NutsDependencyScope> a, Collection<NutsDependencyScope> b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NutsDependencyScope> bb = b == null ? Collections.emptyList() : b;
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> removeScopePatterns(Collection<NutsDependencyScope> a, Collection<NutsDependencyScopePattern> b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NutsDependencyScope> bb = expand(b);
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> remove(Collection<NutsDependencyScope> a, NutsDependencyScopePattern... b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NutsDependencyScope> bb = expand(b == null ? null : Arrays.asList(b));
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> remove(Collection<NutsDependencyScope> a, NutsDependencyScope... b) {
        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NutsDependencyScope> bb = (b == null) ? Collections.emptySet() : Arrays.asList(b);
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> expand(Collection<NutsDependencyScopePattern> other) {
        EnumSet<NutsDependencyScope> a = EnumSet.noneOf(NutsDependencyScope.class);
        if (other != null) {
            for (NutsDependencyScopePattern s : other) {
                if (s != null) {
                    a.addAll(s.expand());
                }
            }
        }
        return a;
    }

    public static EnumSet<NutsDependencyScope> expand(NutsDependencyScopePattern other) {
        return other == null ? EnumSet.noneOf(NutsDependencyScope.class) : other.expand();
    }

    //    public static String combineScopes(String s1, String s2) {
//        s1 = normalizeScope(s1);
//        s2 = normalizeScope(s2);
//        switch (s1) {
//            case "compile": {
//                switch (s2) {
//                    case "compile":
//                        return "compile";
//                    case "runtime":
//                        return "runtime";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "test";
//                    default:
//                        return s2;
//                }
//            }
//            case "runtime": {
//                switch (s2) {
//                    case "compile":
//                        return "runtime";
//                    case "runtime":
//                        return "runtime";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "test";
//                    default:
//                        return "runtime";
//                }
//            }
//            case "provided": {
//                switch (s2) {
//                    case "compile":
//                        return "provided";
//                    case "runtime":
//                        return "provided";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "provided";
//                    case "test":
//                        return "provided";
//                    default:
//                        return "provided";
//                }
//            }
//            case "system": {
//                switch (s2) {
//                    case "compile":
//                        return "system";
//                    case "runtime":
//                        return "system";
//                    case "provided":
//                        return "system";
//                    case "system":
//                        return "system";
//                    case "test":
//                        return "system";
//                    default:
//                        return "system";
//                }
//            }
//            case "test": {
//                switch (s2) {
//                    case "compile":
//                        return "test";
//                    case "runtime":
//                        return "test";
//                    case "provided":
//                        return "provided";
//                    case "system":
//                        return "test";
//                    case "test":
//                        return "test";
//                    default:
//                        return "test";
//                }
//            }
//            default: {
//                return s1;
//            }
//        }
//    }
}
