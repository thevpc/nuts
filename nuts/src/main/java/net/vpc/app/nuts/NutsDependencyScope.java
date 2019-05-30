/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;

/**
 * 
 * @author vpc
 * @since 0.5.4
 */
public enum NutsDependencyScope{
    /**
     * dependencies needed for running/executing the nuts : includes
     * 'compile,system,runtime' witch are NOT optional
     */
    PROFILE_RUN,
    PROFILE_RUN_STANDALONE,
    PROFILE_RUN_TEST,
    PROFILE_RUN_TEST_STANDALONE,
    GROUP_TEST,
    GROUP_COMPILE,
    COMPILE,
    IMPLEMENTATION,
    PROVIDED,
    IMPORT,
    RUNTIME,
    SYSTEM,
    TEST_PROVIDED,
    TEST_RUNTIME,
    OTHER,
    /**
     * dependencies needed for running/executing unit tests the nuts : includes
     * 'test,compile,system,runtime' witch are NOT optional
     */
    TEST,
    /**
     * all dependencies (no restriction)
     */
    ALL;

    public EnumSet<NutsDependencyScope> expand() {
        EnumSet<NutsDependencyScope> v = EnumSet.noneOf(NutsDependencyScope.class);
        switch (this) {
            case PROFILE_RUN: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                break;
            }
            case PROFILE_RUN_STANDALONE: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
//                v.add(NutsDependencyScope.PROVIDED);
                break;
            }
            case PROFILE_RUN_TEST: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.TEST);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                break;
            }
            case PROFILE_RUN_TEST_STANDALONE: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                v.add(NutsDependencyScope.TEST);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                break;
            }
            case GROUP_TEST: {
                v.add(NutsDependencyScope.TEST);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                break;
            }
            case GROUP_COMPILE: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                break;
            }
            case ALL: {
                v.add(NutsDependencyScope.COMPILE);
                v.add(NutsDependencyScope.IMPLEMENTATION);
                v.add(NutsDependencyScope.RUNTIME);
                v.add(NutsDependencyScope.SYSTEM);
                v.add(NutsDependencyScope.PROVIDED);
                v.add(NutsDependencyScope.TEST);
                v.add(NutsDependencyScope.TEST_RUNTIME);
                v.add(NutsDependencyScope.TEST_PROVIDED);
                v.add(NutsDependencyScope.OTHER);
                break;
            }
            default: {
                v.add(this);
            }
        }
        return v;
    }

    public static EnumSet<NutsDependencyScope> expand(NutsDependencyScope other) {
        return other == null ? EnumSet.noneOf(NutsDependencyScope.class) : other.expand();
    }

    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, Collection<NutsDependencyScope> b) {
        EnumSet<NutsDependencyScope> aa = expand(a);
        EnumSet<NutsDependencyScope> bb = expand(b);
        aa.addAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> add(Collection<NutsDependencyScope> a, NutsDependencyScope ... b) {
        EnumSet<NutsDependencyScope> aa = expand(a);
        EnumSet<NutsDependencyScope> bb = expand(b==null?null:Arrays.asList(b));
        aa.addAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> remove(Collection<NutsDependencyScope> a, Collection<NutsDependencyScope> b) {
        EnumSet<NutsDependencyScope> aa = expand(a);
        EnumSet<NutsDependencyScope> bb = expand(b);
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> remove(Collection<NutsDependencyScope> a, NutsDependencyScope ... b) {
        EnumSet<NutsDependencyScope> aa = expand(a);
        EnumSet<NutsDependencyScope> bb = expand(b==null?null:Arrays.asList(b));
        aa.removeAll(bb);
        return aa;
    }

    public static EnumSet<NutsDependencyScope> expand(Collection<NutsDependencyScope> other) {
        EnumSet<NutsDependencyScope> a = EnumSet.noneOf(NutsDependencyScope.class);
        if (other != null) {
            for (NutsDependencyScope s : other) {
                if (s != null) {
                    a.addAll(s.expand());
                }
            }
        }
        return a;
    }

    public static NutsDependencyScope lenientParse(String s) {
        if (s == null) {
            s = "";
        }
        s = s.trim().toLowerCase();
        switch (s) {
            case "":
            case "compile":
                return COMPILE;
            case "api":
                return COMPILE;
            case "implementation":
                return IMPLEMENTATION;
            case "provided": //maven
            case "compileOnly": //gradle
                return PROVIDED;
            case "runtime":
                return RUNTIME;
            case "import":
                return IMPORT;
            case "system":
                return SYSTEM;
            case "test":
                return TEST;
            case "test-provided":
                return TEST_PROVIDED;
            case "test-runtime":
                return TEST_RUNTIME;
            default:
                return OTHER;
        }
    }
}
