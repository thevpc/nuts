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
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.text.parser;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public abstract class NTextSpecialBase extends AbstractNText {

    private final String start;
    private final String kind;
    private final String separator;
    private final String end;

    public NTextSpecialBase(String start, String kind, String separator, String end) {
        super();
        this.start=start==null?"":start;
        this.end=end==null?"":end;
        this.kind = kind==null?"":kind;
        this.separator = separator==null?"":separator;
    }

    public String getKind() {
        return kind;
    }

    public String getSeparator() {
        return separator;
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NTextSpecialBase that = (NTextSpecialBase) o;
        return Objects.equals(start, that.start) && Objects.equals(kind, that.kind) && Objects.equals(separator, that.separator) && Objects.equals(end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, kind, separator, end);
    }
}
