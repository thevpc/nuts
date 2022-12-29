/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
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
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.NBlankable;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdColumn extends MdAbstractElement{

    private MdElement name;
    private MdHorizontalAlign horizontalAlign;

    public MdColumn(MdElement name, MdHorizontalAlign horizontalAlign) {
        this.name = name;
        this.horizontalAlign = horizontalAlign;
    }

    
    public MdElement getName() {
        return name;
    }

    public MdHorizontalAlign getHorizontalAlign() {
        return horizontalAlign;
    }

    @Override
    public MdElementType type() {
        return MdElementType.COLUMN;
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
        MdColumn mdColumn = (MdColumn) o;
        return Objects.equals(name, mdColumn.name) && horizontalAlign == mdColumn.horizontalAlign;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, horizontalAlign);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(name);
    }
}
