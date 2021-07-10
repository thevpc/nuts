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

/**
 *
 * @author thevpc
 */
public class MdElementType {
    public static final MdElementType LINE_SEPARATOR = new MdElementType(MdElementType0.LINE_SEPARATOR);
    public static final MdElementType ADMONITION = new MdElementType(MdElementType0.ADMONITION);
    public static final MdElementType BOLD = new MdElementType(MdElementType0.BOLD);
    public static final MdElementType ITALIC = new MdElementType(MdElementType0.ITALIC);
    public static final MdElementType CODE = new MdElementType(MdElementType0.CODE);
    public static final MdElementType IMAGE = new MdElementType(MdElementType0.IMAGE);
    public static final MdElementType COLUMN = new MdElementType(MdElementType0.COLUMN);
    public static final MdElementType LINE_BREAK = new MdElementType(MdElementType0.LINE_BREAK);
    public static final MdElementType HORIZONTAL_RULE = new MdElementType(MdElementType0.HORIZONTAL_RULE);
    public static final MdElementType XML = new MdElementType(MdElementType0.XML);
    public static final MdElementType CODE_LINK = new MdElementType(MdElementType0.CODE_LINK);
    public static final MdElementType LINK = new MdElementType(MdElementType0.LINK);
    public static final MdElementType TEXT = new MdElementType(MdElementType0.TEXT);
    public static final MdElementType TABLE = new MdElementType(MdElementType0.TABLE);
    public static final MdElementType ROW = new MdElementType(MdElementType0.ROW);
    public static final MdElementType SEQ = new MdElementType(MdElementType0.SEQ);
    private MdElementType0 type;
    private int depth;

    public MdElementType(MdElementType0 type) {
        this(type,0);
    }

    public MdElementType(MdElementType0 type, int depth) {
        this.type = type;
        this.depth = depth;
    }

    public MdElementType0 type() {
        return type;
    }

    public int depth() {
        return depth;
    }
}
