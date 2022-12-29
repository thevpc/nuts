/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.NDependencyScope;
import net.thevpc.nuts.NDependencyScopePattern;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

/**
 *
 * @author thevpc
 */
public class NDependencyScopes {

    public static boolean isDefaultScope(String s1) {
        return NDependencyScope.parse(s1).orElse(NDependencyScope.API) == NDependencyScope.API;
    }

    public static boolean isCompileScope(String scope) {
        if (scope == null) {
            return true;
        }
        NDependencyScope r = NDependencyScope.parse(scope).orElse(NDependencyScope.API);
        return r != null && r.isCompile();
    }

    public static int getScopesPriority(String s1) {
        NDependencyScope r = NDependencyScope.parse(s1).orElse(NDependencyScope.API);
        if (r == null) {
            return -1;
        }
        switch (r) {
            case IMPLEMENTATION: {
                return 26;
            }
            case API: {
                return 25;
            }
            case RUNTIME: {
                return 24;
            }
            case PROVIDED: {
                return 23;
            }
            case SYSTEM: {
                return 22;
            }
            case OTHER: {
                return 21;
            }
            case TEST_IMPLEMENTATION: {
                return 16;
            }
            case TEST_API: {
                return 15;
            }
            case TEST_RUNTIME: {
                return 14;
            }
            case TEST_PROVIDED: {
                return 13;
            }
            case TEST_SYSTEM: {
                return 12;
            }
            case TEST_OTHER: {
                return 11;
            }
            case IMPORT: {
                return 1;
            }
        }
        return -1;
    }

    //    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, Collection<NutsDependencyScopePattern> b) {
//        EnumSet<NutsDependencyScope> aa = EnumSet.copyOf(a);
//        EnumSet<NutsDependencyScope> bb = expand(b);
//        aa.addAll(bb);
//        return aa;
//    }
    public static EnumSet<NDependencyScope> add(Collection<NDependencyScope> a, NDependencyScopePattern... b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NDependencyScope> bb = expand(b == null ? null : Arrays.asList(b));
        aa.addAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> add(Collection<NDependencyScope> a, NDependencyScope... b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NDependencyScope> bb = (b == null ? Collections.emptyList() : Arrays.asList(b));
        aa.addAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> removeScopes(Collection<NDependencyScope> a, Collection<NDependencyScope> b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NDependencyScope> bb = b == null ? Collections.emptyList() : b;
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> removeScopePatterns(Collection<NDependencyScope> a, Collection<NDependencyScopePattern> b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NDependencyScope> bb = expand(b);
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> remove(Collection<NDependencyScope> a, NDependencyScopePattern... b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        EnumSet<NDependencyScope> bb = expand(b == null ? null : Arrays.asList(b));
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> remove(Collection<NDependencyScope> a, NDependencyScope... b) {
        EnumSet<NDependencyScope> aa = EnumSet.copyOf(a);
        Collection<NDependencyScope> bb = (b == null) ? Collections.emptySet() : Arrays.asList(b);
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NDependencyScope> expand(Collection<NDependencyScopePattern> other) {
        EnumSet<NDependencyScope> a = EnumSet.noneOf(NDependencyScope.class);
        if (other != null) {
            for (NDependencyScopePattern s : other) {
                if (s != null) {
                    a.addAll(s.toScopes());
                }
            }
        }
        return a;
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
