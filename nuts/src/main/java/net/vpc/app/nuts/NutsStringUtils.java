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
package net.vpc.app.nuts;

import java.util.*;

/**
 * Created by vpc on 1/15/17.
 */
class NutsStringUtils {

    public static boolean isEmpty(String str) {
        return str == null || str.trim().length() == 0;
    }

    public static String trim(String str) {
        if (str == null) {
            return "";
        }
        return str.trim();
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static String mergeLists(String sep, String... lists) {
        LinkedHashSet<String> all = new LinkedHashSet<>(Arrays.asList(splitAndRemoveDuplicates(Arrays.asList(lists))));
        return join(sep, all);
    }

    public static String join(String sep, String[] items) {
        return join(sep, Arrays.asList(items));
    }

    public static String join(String sep, Collection<String> items) {
        StringBuilder sb = new StringBuilder();
        Iterator<String> i = items.iterator();
        if (i.hasNext()) {
            sb.append(i.next());
        }
        while (i.hasNext()) {
            sb.append(sep);
            sb.append(i.next());
        }
        return sb.toString();
    }

    public static String[] splitAndRemoveDuplicates(List<String>... possibilities) {
        LinkedHashSet<String> allValid = new LinkedHashSet<>();
        for (List<String> initial : possibilities) {
            for (String v : initial) {
                if (!isEmpty(v)) {
                    v = v.trim();
                    for (String v0 : v.split(";")) {
                        v0 = v0.trim();
                        if (!allValid.contains(v0)) {
                            allValid.add(v0);
                        }
                    }
                }
            }
        }
        return allValid.toArray(new String[allValid.size()]);
    }

}
