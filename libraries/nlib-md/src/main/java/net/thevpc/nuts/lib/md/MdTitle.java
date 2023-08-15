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

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.lib.md.util.MdUtils;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdTitle extends MdParent {

    private String code;
    private MdElementType id;
    private MdElement value;

    public MdTitle(String code, MdElement value, int depth) {
        this(code,value,depth,new MdElement[0]);
    }

    public MdTitle(String code, MdElement value, int depth,MdElement[] children) {
        super(children);
        this.code = code;
        this.value = value;
        if(!value.isInline()){
            throw new IllegalArgumentException("unexpected newline element title: "+value.type());
        }
        id = new MdElementType(MdElementTypeGroup.TITLE,depth);
    }

    public MdElement getValue() {
        return value;
    }

    public String getCode() {
        return code;
    }

    @Override
    public MdElementType type() {
        return id;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(MdUtils.times('#', type().depth())).append(" ").append(getValue());
        for (int i = 0; i < size(); i++) {
            if (i > 0) {
                if (!isInline()) {
                    sb.append("\n");
                }
            }
            sb.append(getChild(i));
        }
        return sb.toString();
    }
    @Override
    public boolean isEndWithNewline() {
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        MdTitle mdTitle = (MdTitle) o;
        return Objects.equals(code, mdTitle.code) && Objects.equals(id, mdTitle.id) && Objects.equals(value, mdTitle.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), code, id, value);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(value)
                && (getChildren()==null || Arrays.stream(getChildren()).allMatch(x-> NBlankable.isBlank(x)));
    }
}
