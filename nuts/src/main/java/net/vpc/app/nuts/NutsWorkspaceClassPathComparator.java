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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author vpc
 */
class NutsWorkspaceClassPathComparator implements Comparator<NutsBootConfig> {

    public NutsWorkspaceClassPathComparator() {
    }

    @Override
    public int compare(NutsBootConfig o1, NutsBootConfig o2) {
        return compareVersion(
                o1.getBootRuntime().substring(o1.getBootRuntime().indexOf("#")+1),
                o2.getBootRuntime().substring(o2.getBootRuntime().indexOf("#")+1));
    }

    public int compareVersion(String o1, String o2) {
        String[] split1 = splitByDigit(o1);
        String[] split2 = splitByDigit(o2);
        for (int i = 0; i < Math.max(split1.length, split2.length); i++) {
            if (i >= split1.length) {
                return -1;
            }
            if (i >= split2.length) {
                return 1;
            }
            int x = compareVersionDigit(split1[i], split2[i]);
            if (x != 0) {
                return x;
            }
        }
        return 0;
    }

    private int compareVersionDigit(String version1, String version2) {
        if (version1.equals(version2)) {
            return 0;
        }
        if (version1.isEmpty()) {
            return -1;
        }
        if (version2.isEmpty()) {
            return 1;
        }
        if (Character.isDigit(version1.charAt(0)) && Character.isDigit(version1.charAt(1))) {
            return Integer.compare(Integer.parseInt(version1), Integer.parseInt(version2));
        }
        return version1.compareTo(version2);
    }

    private String[] splitByDigit(String version) {
        List<String> all = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        if (version == null) {
            version = "";
        } else {
            version = version.trim();
        }
        if (version.isEmpty()) {
            return new String[0];
        }
        int type = -1;
        final int TYPE_D = 1;
        final int TYPE_C = 2;
        for (char cc : version.toCharArray()) {
            int t = Character.isDigit(cc) ? TYPE_D : TYPE_C;
            if (sb.length() == 0) {
                type = t;
            } else if (t == type) {
                sb.append(cc);
            } else {
                all.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }
        if (sb.length() > 0) {
            all.add(sb.toString());
        }
        return all.toArray(new String[0]);
    }

}
