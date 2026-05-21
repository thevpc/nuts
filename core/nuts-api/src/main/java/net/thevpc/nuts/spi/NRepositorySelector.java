/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
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
package net.thevpc.nuts.spi;

import net.thevpc.nuts.util.NAssert;

/**
 * @author thevpc
 */
public class NRepositorySelector {

    private final NRepositoryLocation location;
    private NSelectorOp op;


    public NRepositorySelector(NSelectorOp op, NRepositoryLocation location) {
        NAssert.requireNamedNonNull(op, "operator");
        NAssert.requireNamedNonNull(location, "location");
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

    public NSelectorOp op() {
        return op;
    }

    public String name() {
        return location.name();
    }

    public String url() {
        return location.fullLocation();
    }

    public boolean matches(NRepositoryLocation other) {
        String otherName = other.name();
        String otherURL = other.fullLocation();
        String name0 = location.name();
        String url0 = location.fullLocation();
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
