/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.thevpc.nuts.NConstants;

/**
 *
 * @author thevpc
 */
public class NAuthorizations {

    private final Set<String> allowed = new HashSet<>();
    private final Set<String> denied = new HashSet<>();

    public NAuthorizations(Collection<String> all) {
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
            case NConstants.Permissions.FETCH_DESC:
                return new String[]{r};

            case NConstants.Permissions.FETCH_CONTENT:
                return new String[]{r};

//        case  NutsConstants.Rights.SAVE_REPOSITORY :return new String[]{r};
            case NConstants.Permissions.SAVE:
                return new String[]{r};
            case NConstants.Permissions.AUTO_INSTALL:
                return new String[]{r};
            case NConstants.Permissions.INSTALL:
                return new String[]{r};
            case NConstants.Permissions.UPDATE:
                return new String[]{r};
            case NConstants.Permissions.UNINSTALL:
                return new String[]{r};
            case NConstants.Permissions.EXEC:
                return new String[]{r};
            case NConstants.Permissions.DEPLOY:
                return new String[]{r};
            case NConstants.Permissions.UNDEPLOY:
                return new String[]{r};
            case NConstants.Permissions.PUSH:
                return new String[]{r};
            case NConstants.Permissions.ADD_REPOSITORY:
                return new String[]{r};
            case NConstants.Permissions.REMOVE_REPOSITORY:
                return new String[]{r};
            case NConstants.Permissions.SET_PASSWORD:
                return new String[]{r};
            case NConstants.Permissions.ADMIN:
                return new String[]{r};

        }
        return new String[0];
    }

    private static boolean isValidPermission(String r) {
        if (r == null) {
            return false;
        }
        switch (r) {
            case NConstants.Permissions.FETCH_DESC:
            case NConstants.Permissions.FETCH_CONTENT:
            case NConstants.Permissions.SAVE:
            case NConstants.Permissions.AUTO_INSTALL:
            case NConstants.Permissions.INSTALL:
            case NConstants.Permissions.UPDATE:
            case NConstants.Permissions.UNINSTALL:
            case NConstants.Permissions.EXEC:
            case NConstants.Permissions.DEPLOY:
            case NConstants.Permissions.UNDEPLOY:
            case NConstants.Permissions.PUSH:
            case NConstants.Permissions.ADD_REPOSITORY:
            case NConstants.Permissions.REMOVE_REPOSITORY:
            case NConstants.Permissions.SET_PASSWORD:
            case NConstants.Permissions.ADMIN:
                return true;

        }
        return false;
    }
}
