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
 * Copyright (C) 2016-2017 Taha BEN SALAH
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
package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.extensions.filters.version.AllNutsVersionFilter;
import net.vpc.app.nuts.extensions.filters.version.DefaultNutsVersionFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreVersionUtils {
    public static final NutsVersionFilter ALL_VERSIONS = new AllNutsVersionFilter();

    public static boolean versionMatches(String version, String pattern) {
        if (pattern == null || CoreStringUtils.isEmpty(pattern) || pattern.equals("LAST")) {
            return true;
        }
        return pattern.equals(version);
    }

    public static String incVersion(String oldVersion){
        if(CoreStringUtils.isEmpty(oldVersion)){
            return "1";
        }
        int t = oldVersion.lastIndexOf('.');
        if(t>=0){
            String a=oldVersion.substring(0,t);
            String b=oldVersion.substring(t+1);
            if(CoreStringUtils.isLong(b)){
                return a+"."+(Long.parseLong(b)+1L);
            }else {
                Matcher m = Pattern.compile("^(<C>.+)(?<N>[0-9]+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C")+String.valueOf(Long.parseLong(m.group("N")) + 1);
                }
                m = Pattern.compile("^(?<N>[0-9]+)(?<C>.+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C")+String.valueOf(Long.parseLong(m.group("N")) + 1);
                }
                m = Pattern.compile("^(?<C1>.+)(?<N>[0-9]+)(?<C2>.+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C1")+String.valueOf(Long.parseLong(m.group("N")) + 1)+m.group("C2");
                }
                return oldVersion+"."+1;
            }
        }else{
            return oldVersion+"."+1;
        }
    }

    public static int compareVersions(String v1, String v2) {
        if ("LATEST".equals(v2)) {
            if (v1.equals("LATEST")) {
                return 0;
            }
            return -1;
        }
        if ("LATEST".equals(v1)) {
            if (v2.equals("LATEST")) {
                return 0;
            }
            return 1;
        }
        String[] v1arr = splitVersionParts(v1);
        String[] v2arr = splitVersionParts(v2);
        for (int i = 0; i < Math.max(v1arr.length, v2arr.length); i++) {
            if (i >= v1arr.length) {
                return -1;
            }
            if (i >= v2arr.length) {
                return 1;
            }
            int x = compareVersionItem(v1arr[i], v2arr[i]);
            if (x != 0) {
                return x;
            }
        }
        return 0;
    }

    private static String[] splitVersionParts(String v1) {
        v1 = CoreStringUtils.trim(v1);
        List<String> parts = new ArrayList<>();
        StringBuilder last = new StringBuilder();
        for (char c : v1.toCharArray()) {
            if (last.length() == 0) {
                last.append(c);
            } else if (Character.isDigit(last.charAt(0)) == Character.isDigit(c)) {
                last.append(c);
            } else {
                parts.add(last.toString());
                CoreStringUtils.clear(last);
            }
        }
        if (last.length() > 0) {
            parts.add(last.toString());
        }
        return parts.toArray(new String[parts.size()]);
    }

    private static int compareVersionItem(String v1, String v2) {
        if (CoreStringUtils.isInt(v1) && CoreStringUtils.isInt(v2)) {
            return Integer.parseInt(v1) - Integer.parseInt(v2);
        } else {
            int a = CoreStringUtils.getStartingInt(v1);
            int b = CoreStringUtils.getStartingInt(v2);
            if (a != -1 && b != -1 && a != b) {
                return a - b;
            } else {
                return v1.compareTo(v2);
            }
        }
    }

    public static boolean isStaticVersionPattern(String pattern) {
        if (CoreStringUtils.isEmpty(pattern)) {
            return false;
        }
        if (pattern.contains("[") || pattern.contains("]") || pattern.contains(",")) {
            return false;
        } else return !"LATEST".equals(pattern);
    }

    public static NutsVersionFilter createNutsVersionFilter(String pattern) {
        if (pattern == null || CoreStringUtils.isEmpty(pattern) || pattern.equals("LAST")) {
            return ALL_VERSIONS;
        }
        return DefaultNutsVersionFilter.parse(pattern);
    }

}
