/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
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
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.lib.md.base.MdAbstractElement;
import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdCodeLink extends MdAbstractElement {

    private String type;
    private String linkCode;
    public MdCodeLink(String type, String linkCode) {
        this.type = type;
        this.linkCode = linkCode;
    }

    public String getType() {
        return type;
    }

    public String getLinkCode() {
        return linkCode;
    }

    @Override
    public MdElementType type() {
        return MdElementType.CODE_LINK;
    }

    @Override
    public String toString() {
        return "(@code-link:" + linkCode + ")";
    }
    @Override
    public boolean isInline() {
        return true;
    }

    @Override
    public boolean isEndWithNewline() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdCodeLink that = (MdCodeLink) o;
        return Objects.equals(type, that.type) && Objects.equals(linkCode, that.linkCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, linkCode);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(linkCode);
    }
}
