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

import net.thevpc.nuts.lib.md.util.MdUtils;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdHr extends MdAbstractElement {

    private String type;
    public MdHr() {
        this("");
    }

    public MdHr(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public MdElementType type() {
        return MdElementType.HORIZONTAL_RULE;
    }

    @Override
    public String toString() {
        return MdUtils.times('-',7);
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isEndWithNewline() {
        return true;
    }

    @Override
    public boolean isHr() {
        return true;
    }

    @Override
    public boolean isHr(String type) {
        return Objects.equals(this.type, type);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdHr mdHr = (MdHr) o;
        return Objects.equals(type, mdHr.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type);
    }

    @Override
    public boolean isBlank() {
        return false;
    }
}
