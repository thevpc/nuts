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
package net.vpc.app.nuts.runtime.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.vpc.app.nuts.NutsConstants;

/**
 *
 * @author vpc
 */
public class NutsAuthorizations {

    private final Set<String> allowed = new HashSet<>();
    private final Set<String> denied = new HashSet<>();

    public NutsAuthorizations(Collection<String> all) {
        for (String s : all) {
            if (s.startsWith("!")) {
                String ss = s.substring(1);
                if (isValidPermission(ss)) {
                    denied.add(ss);
                }
            } else {
                allowed.addAll(Arrays.asList(computePermissions(s)));
            }
        }
    }

    public Boolean explicitAccept(String right) {
        if (denied.contains(right)) {
            return false;
        }
        if (allowed.contains(right)) {
            return true;
        }
        return null;
    }

    public boolean accept(String right) {
        if (denied.contains(right)) {
            return false;
        }
        return allowed.contains(right);
    }

    private static String[] computePermissions(String r) {
        if (r == null) {
            return new String[0];
        }
        switch (r) {
            case NutsConstants.Permissions.FETCH_DESC:
                return new String[]{r};

            case NutsConstants.Permissions.FETCH_CONTENT:
                return new String[]{r};

//        case  NutsConstants.Rights.SAVE_REPOSITORY :return new String[]{r};
            case NutsConstants.Permissions.SAVE:
                return new String[]{r};
            case NutsConstants.Permissions.AUTO_INSTALL:
                return new String[]{r};
            case NutsConstants.Permissions.INSTALL:
                return new String[]{r};
            case NutsConstants.Permissions.UPDATE:
                return new String[]{r};
            case NutsConstants.Permissions.UNINSTALL:
                return new String[]{r};
            case NutsConstants.Permissions.EXEC:
                return new String[]{r};
            case NutsConstants.Permissions.DEPLOY:
                return new String[]{r};
            case NutsConstants.Permissions.UNDEPLOY:
                return new String[]{r};
            case NutsConstants.Permissions.PUSH:
                return new String[]{r};
            case NutsConstants.Permissions.ADD_REPOSITORY:
                return new String[]{r};
            case NutsConstants.Permissions.REMOVE_REPOSITORY:
                return new String[]{r};
            case NutsConstants.Permissions.SET_PASSWORD:
                return new String[]{r};
            case NutsConstants.Permissions.ADMIN:
                return new String[]{r};

        }
        return new String[0];
    }

    private static boolean isValidPermission(String r) {
        if (r == null) {
            return false;
        }
        switch (r) {
            case NutsConstants.Permissions.FETCH_DESC:
            case NutsConstants.Permissions.FETCH_CONTENT:
            case NutsConstants.Permissions.SAVE:
            case NutsConstants.Permissions.AUTO_INSTALL:
            case NutsConstants.Permissions.INSTALL:
            case NutsConstants.Permissions.UPDATE:
            case NutsConstants.Permissions.UNINSTALL:
            case NutsConstants.Permissions.EXEC:
            case NutsConstants.Permissions.DEPLOY:
            case NutsConstants.Permissions.UNDEPLOY:
            case NutsConstants.Permissions.PUSH:
            case NutsConstants.Permissions.ADD_REPOSITORY:
            case NutsConstants.Permissions.REMOVE_REPOSITORY:
            case NutsConstants.Permissions.SET_PASSWORD:
            case NutsConstants.Permissions.ADMIN:
                return true;

        }
        return false;
    }
}
