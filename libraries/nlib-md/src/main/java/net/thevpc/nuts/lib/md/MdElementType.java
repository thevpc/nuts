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

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdElementType {
//    public static final MdElementType LINE_SEPARATOR = new MdElementType(MdElementTypeGroup.LINE_SEPARATOR);
    public static final MdElementType ADMONITION = new MdElementType(MdElementTypeGroup.ADMONITION);
    public static final MdElementType BOLD = new MdElementType(MdElementTypeGroup.BOLD);
    public static final MdElementType ITALIC = new MdElementType(MdElementTypeGroup.ITALIC);
    public static final MdElementType CODE = new MdElementType(MdElementTypeGroup.CODE);
    public static final MdElementType IMAGE = new MdElementType(MdElementTypeGroup.IMAGE);
    public static final MdElementType COLUMN = new MdElementType(MdElementTypeGroup.COLUMN);
    public static final MdElementType LINE_BREAK = new MdElementType(MdElementTypeGroup.LINE_BREAK);
    public static final MdElementType HORIZONTAL_RULE = new MdElementType(MdElementTypeGroup.HORIZONTAL_RULE);
    public static final MdElementType XML = new MdElementType(MdElementTypeGroup.XML);
    public static final MdElementType CODE_LINK = new MdElementType(MdElementTypeGroup.CODE_LINK);
    public static final MdElementType LINK = new MdElementType(MdElementTypeGroup.LINK);
    public static final MdElementType TEXT = new MdElementType(MdElementTypeGroup.TEXT);
    public static final MdElementType TABLE = new MdElementType(MdElementTypeGroup.TABLE);
    public static final MdElementType ROW = new MdElementType(MdElementTypeGroup.ROW);
    public static final MdElementType BODY = new MdElementType(MdElementTypeGroup.BODY);
    public static final MdElementType PHRASE = new MdElementType(MdElementTypeGroup.PHRASE);
//    public static final MdElementType NUMBERED_LIST = new MdElementType(MdElementTypeGroup.NUMBERED_LIST);
//    public static final MdElementType UNNUMBERED_LIST = new MdElementType(MdElementTypeGroup.UNNUMBERED_LIST);
    private MdElementTypeGroup group;
    private int depth;

    public MdElementType(MdElementTypeGroup group) {
        this(group,0);
    }

    public MdElementType(MdElementTypeGroup group, int depth) {
        this.group = group;
        this.depth = depth;
    }

    public MdElementTypeGroup group() {
        return group;
    }

    public int depth() {
        return depth;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdElementType that = (MdElementType) o;
        return depth == that.depth && group == that.group;
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, depth);
    }

    @Override
    public String toString() {
        switch (group){
            case TITLE:
            case NUMBERED_ITEM:
            case UNNUMBERED_ITEM:
            case NUMBERED_LIST:
            case UNNUMBERED_LIST:
            {
                return group +"_"+depth;
            }
        }
        return group.toString();
    }
}
