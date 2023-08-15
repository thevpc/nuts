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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NStringUtils;

/**
 * @author thevpc
 */
public class NRepositorySelector {

    private final NRepositoryLocation location;
    private NSelectorOp op;

    public static NOptional<NRepositorySelector> of(String location, NRepositoryDB db, NSession session) {
        return of(null,location,db,session);
    }

    public static NOptional<NRepositorySelector> of(NSelectorOp op, String location, NRepositoryDB db, NSession session) {
        location = NStringUtils.trim(location);
        if (op == null) {
            op = NSelectorOp.INCLUDE;
        }
        if (location.length() > 0) {
            if (location.startsWith("+")) {
                op = NSelectorOp.INCLUDE;
                location = location.substring(1).trim();
            } else if (location.startsWith("-")) {
                op = NSelectorOp.EXCLUDE;
                location = location.substring(1).trim();
            } else if (location.startsWith("=")) {
                op = NSelectorOp.EXACT;
                location = location.substring(1).trim();
            }
            NOptional<NRepositoryLocation> z = NRepositoryLocation.of(location, db, session);
            if (z.isPresent()) {
                return NOptional.of(new NRepositorySelector(op, z.get(session)));
            }
        }
        String finalLocation = location;
        return NOptional.<NRepositorySelector>ofEmpty(ss -> NMsg.ofC("repository %s", finalLocation)).setSession(session);
    }

    public NRepositorySelector(NSelectorOp op, NRepositoryLocation location) {
        NAssert.requireNonNull(op, "operator");
        NAssert.requireNonNull(location, "location");
        this.op = op;
        this.location = location;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (op == NSelectorOp.EXACT) {
            sb.append("=");
        } else if (op == NSelectorOp.EXCLUDE) {
            sb.append("-");
        } else if (op == NSelectorOp.INCLUDE) {
            sb.append("+");
        }
        sb.append(location);
        return sb.toString();
    }

    public NSelectorOp getOp() {
        return op;
    }

    public String getName() {
        return location.getName();
    }

    public String getUrl() {
        return location.getFullLocation();
    }

    public boolean matches(NRepositoryLocation other) {
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
