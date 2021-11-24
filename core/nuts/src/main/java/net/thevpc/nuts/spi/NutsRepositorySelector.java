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

/**
 * @author thevpc
 */
public class NutsRepositorySelector {

    private final String name;
    private final String url;
    private NutsSelectorOp op;

    public NutsRepositorySelector(NutsSelectorOp op, String name, String url) {
        this.op = op;
        this.name = name;
        this.url = url;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (op == NutsSelectorOp.EXACT) {
            sb.append(":=");
        } else if (op == NutsSelectorOp.EXCLUDE) {
            sb.append(":-");
        } else if (op == NutsSelectorOp.INCLUDE) {
            sb.append(":+");
        }
        if (name != null && name.length() > 0) {
            sb.append(name);
        }
        if (url != null && url.length() > 0) {
            sb.append("(");
            sb.append(url);
            sb.append(")");
        }
        return sb.toString();
    }

    public NutsSelectorOp getOp() {
        return op;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public boolean matches(String otherName, String otherURL) {
        otherName = otherName == null ? "" : otherName.trim();
        otherURL = otherURL == null ? "" : otherURL.trim();
        String _name = this.name == null ? "" : this.name.trim();
        String _url = this.url == null ? "" : this.url.trim();
        otherURL = otherURL == null ? otherURL : otherURL.trim();
        if (_name.length() > 0 && _name.equals(otherName)) {
            return true;
        }
        return _url.length() > 0 && _url.equals(otherURL);
    }

}
