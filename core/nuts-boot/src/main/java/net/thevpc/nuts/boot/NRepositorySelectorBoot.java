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
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.boot.reserved.NAssertBoot;
import net.thevpc.nuts.boot.reserved.NMsgBoot;
import net.thevpc.nuts.boot.reserved.NReservedBootRepositoryDB;
import net.thevpc.nuts.boot.reserved.util.NStringUtilsBoot;
import net.thevpc.nuts.boot.reserved.util.NUtilsBoot;

/**
 * @author thevpc
 */
public class NRepositorySelectorBoot {

    private final NRepositoryLocationBoot location;
    private String op;

    public static NRepositorySelectorBoot of(String location, NReservedBootRepositoryDB db) {
        return of(null,location,db);
    }

    public static NRepositorySelectorBoot of(String op, String location, NReservedBootRepositoryDB db) {
        location = NStringUtilsBoot.trim(location);
        op = parseSelectorOp(op);
        if (location.length() > 0) {
            if (location.startsWith("+")) {
                op = "INCLUDE";
                location = location.substring(1).trim();
            } else if (location.startsWith("-")) {
                op = "EXCLUDE";
                location = location.substring(1).trim();
            } else if (location.startsWith("=")) {
                op = "EXACT";
                location = location.substring(1).trim();
            }
            NRepositoryLocationBoot z = NRepositoryLocationBoot.of(location, db);
            if (z!=null) {
                return new NRepositorySelectorBoot(op, z);
            }
        }
        String finalLocation = location;
        return null;
    }

    private static String parseSelectorOp(String op) {
        if (op == null) {
            op = "INCLUDE";
        }else{
            switch (NUtilsBoot.enumName(op)){
                case "INCLUDE":{
                    op ="INCLUDE";
                    break;
                }
                case "EXCLUDE":{
                    op ="EXCLUDE";
                    break;
                }
                case "EXACT":{
                    op ="EXACT";
                    break;
                }
                default:{
                    throw new NBootException(NMsgBoot.ofC("invalid op %s",op));
                }
            }
        }
        return op;
    }

    public NRepositorySelectorBoot(String op, NRepositoryLocationBoot location) {
        NAssertBoot.requireNonNull(op, "operator");
        NAssertBoot.requireNonNull(location, "location");
        this.op = parseSelectorOp(op);
        this.location = location;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        switch (NUtilsBoot.enumName(op)){
            case "EXACT": {
                sb.append("=");
                break;
            }
            case "EXCLUDE": {
                sb.append("-");
                break;
            }
            case "INCLUDE": {
                sb.append("+");
                break;
            }
        }
        sb.append(location);
        return sb.toString();
    }

    public String getOp() {
        return op;
    }

    public String getName() {
        return location.getName();
    }

    public String getUrl() {
        return location.getFullLocation();
    }

    public boolean matches(NRepositoryLocationBoot other) {
        String otherName = other.getName();
        String otherURL = other.getFullLocation();
        String name0 = location.getName();
        String url0 = location.getFullLocation();
        otherName = otherName == null ? "" : otherName.trim();
        otherURL = otherURL == null ? "" : otherURL.trim();
        String _name = name0 == null ? "" : name0.trim();
        String _url = url0 == null ? "" : url0.trim();
        otherURL = otherURL == null ? otherURL : otherURL.trim();
        if (_name.length() > 0 && _name.equals(otherName)) {
            return true;
        }
        return _url.length() > 0 && _url.equals(otherURL);
    }

}
