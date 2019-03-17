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
package net.vpc.app.nuts.core.util;

import net.vpc.app.nuts.NutsVersionFilter;
import net.vpc.app.nuts.core.filters.version.DefaultNutsVersionFilter;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Convert;

import java.util.ArrayList;
import java.util.List;

public class CoreVersionUtils {

//    public static final NutsVersionFilter ALL_VERSIONS = new AllNutsVersionFilter();

    public static boolean versionMatches(String version, String pattern) {
        if (pattern == null || StringUtils.isEmpty(pattern) || pattern.equals("LATEST") || pattern.equals("RELEASE")) {
            return true;
        }
        return pattern.equals(version);
    }

    public static String incVersion(String oldVersion, int level) {
        VersionParts parts = splitVersionParts2(oldVersion);
        int digitCount = parts.getDigitCount();
        if (digitCount == 0) {
            parts.addDigit(0, ".");
        }
        digitCount = parts.getDigitCount();
        if (level <= 0) {
            level = digitCount;
        }
        for (int i = digitCount; i < level; i++) {
            parts.addDigit(0, ".");
        }
        VersionPart digit = parts.getDigit(level);
        digit.string = String.valueOf(Long.parseLong(digit.string) + 1);
        return parts.toString();
    }

    public static int compareVersions(String v1, String v2) {
        v1 = StringUtils.trim(v1);
        v2 = StringUtils.trim(v2);
        if (v1.equals(v2)) {
            return 0;
        }
        if ("LATEST".equals(v1)) {
            return 1;
        }
        if ("LATEST".equals(v2)) {
            return -1;
        }
        if ("RELEASE".equals(v1)) {
            return 1;
        }
        if ("RELEASE".equals(v2)) {
            return -1;
        }
        String[] v1arr = splitVersionParts(v1);
        String[] v2arr = splitVersionParts(v2);
        for (int i = 0; i < Math.max(v1arr.length, v2arr.length); i++) {
            if (i >= v1arr.length) {
                if (v2arr[i].equalsIgnoreCase("SNAPSHOT")) {
                    return 1;
                }
                return -1;
            }
            if (i >= v2arr.length) {
                if (v1arr[i].equalsIgnoreCase("SNAPSHOT")) {
                    return -1;
                }
                return 1;
            }
            int x = compareVersionItem(v1arr[i], v2arr[i]);
            if (x != 0) {
                return x;
            }
        }
        return 0;
    }

    private static class VersionPart {
        String string;
        boolean digit;

        public VersionPart(String string, boolean digit) {
            this.string = string;
            this.digit = digit;
        }

        @Override
        public String toString() {
            String name = digit ? "digit" : "sep";
            return name + "(" + string + ")";
        }
    }

    private static class VersionParts {
        List<VersionPart> all;

        public VersionParts(List<VersionPart> all) {
            this.all = all;
        }

        public int getDigitCount() {
            int c = 0;
            for (VersionPart s : all) {
                if (s.digit) {
                    c++;
                }
            }
            return c;
        }

        public VersionPart getDigit(int index) {
            int c = 0;
            for (VersionPart s : all) {
                if (s.digit) {
                    c++;
                    if (c == index) {
                        return s;
                    }
                }
            }
            return null;
        }

        public void addDigit(long val, String sep) {
            if (all.size() == 0) {
                all.add(new VersionPart(String.valueOf(val), true));
            } else if (all.get(all.size() - 1).digit) {
                all.add(new VersionPart(sep, false));
                all.add(new VersionPart(String.valueOf(val), true));
            } else {
                all.add(new VersionPart(String.valueOf(val), true));
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (VersionPart versionPart : all) {
                sb.append(versionPart.string);
            }
            return sb.toString();
        }
    }

    private static VersionParts splitVersionParts2(String v1) {
        v1 = StringUtils.trim(v1);
        List<VersionPart> parts = new ArrayList<>();
        StringBuilder last = new StringBuilder();
        boolean digit = false;
        for (char c : v1.toCharArray()) {
            if (Character.isDigit(c)) {
                if (last.length() == 0 || digit) {
                    digit = true;
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), false));
                    StringUtils.clear(last);
                    digit = true;
                    last.append(c);
                }
            } else {
                if (last.length() == 0) {
                    digit = false;
                    last.append(c);
                } else if (!digit) {
                    last.append(c);
                } else {
                    parts.add(new VersionPart(last.toString(), true));
                    StringUtils.clear(last);
                    digit = false;
                    last.append(c);
                }
            }
        }
        if (last.length() > 0) {
            parts.add(new VersionPart(last.toString(), digit));
        }
        return new VersionParts(parts);
    }

    private static String[] splitVersionParts(String v1) {
        v1 = StringUtils.trim(v1);
        List<String> parts = new ArrayList<>();
        StringBuilder last = new StringBuilder();
        for (char c : v1.toCharArray()) {
            if (last.length() == 0) {
                last.append(c);
            } else if (Character.isDigit(last.charAt(0)) == Character.isDigit(c)) {
                last.append(c);
            } else {
                parts.add(last.toString());
                StringUtils.clear(last);
            }
        }
        if (last.length() > 0) {
            parts.add(last.toString());
        }
        return parts.toArray(new String[0]);
    }

    private static int compareVersionItem(String v1, String v2) {
        Integer i1 = null;
        Integer i2 = null;

        if (v1.equals(v2)) {
            return 0;
        } else if (
                (i1 = Convert.toInt(v1, CoreNutsUtils.INTEGER_LENIENT_NULL)) != null
                        && (i2 = Convert.toInt(v2, CoreNutsUtils.INTEGER_LENIENT_NULL)) != null
        ) {
            return i1 - i2;
        } else if ("SNAPSHOT".equalsIgnoreCase(v1)) {
            return -1;
        } else if ("SNAPSHOT".equalsIgnoreCase(v2)) {
            return 1;
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
        if (StringUtils.isEmpty(pattern)) {
            return false;
        }
        if (pattern.contains("[") || pattern.contains("]") || pattern.contains(",") || pattern.contains("*")) {
            return false;
        } else {
            return !"LATEST".equals(pattern) && !"RELEASE".equals(pattern);
        }
    }

    public static NutsVersionFilter createNutsVersionFilter(String pattern) {
        if (pattern == null || StringUtils.isEmpty(pattern) || pattern.equals("LAST") || pattern.equals("LATEST") || pattern.equals("RELEASE")) {
            return null;//ALL_VERSIONS;
        }
        return DefaultNutsVersionFilter.parse(pattern);
    }

}
