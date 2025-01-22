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

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdNumberedItem extends MdAbstractElement {

    private int number;
    private String sep;
    private MdElement value;
    private MdElementType id;
    private MdElement[] children;

    public MdNumberedItem(int number, int depth, String sep, MdElement value,MdElement[] children) {
        this.number = number;
        this.sep = sep;
        this.value = value;
        if(!value.isInline()){
            throw new IllegalArgumentException("unexpected newline element in numbered item: "+value.type());
        }
        this.children = children==null?new MdElement[0] :children;
        id = new MdElementType(MdElementTypeGroup.NUMBERED_ITEM,depth);
    }

    public MdElement[] getChildren() {
        return children;
    }

    public String getSep() {
        return sep;
    }

    public int getNumber() {
        return number;
    }

    public MdElement getValue() {
        return value;
    }

    @Override
    public MdElementType type() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(number).append(sep).append(" ").append(getValue());
        return sb.toString();
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdNumberedItem that = (MdNumberedItem) o;
        return number == that.number && Objects.equals(sep, that.sep) && Objects.equals(value, that.value) && Objects.equals(id, that.id) && Arrays.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(number, sep, value, id);
        result = 31 * result + Arrays.hashCode(children);
        return result;
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value)
                && (children==null || Arrays.stream(children).allMatch(x-> NBlankable.isBlank(x)));
    }
}
