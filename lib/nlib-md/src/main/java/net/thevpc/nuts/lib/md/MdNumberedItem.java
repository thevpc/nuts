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
public class MdNumberedItem extends MdAbstractElement {

    private int number;
    private String sep;
    private MdElement value;
    private int depth;
    private MdElementType id;
    private MdElement[] children;

    public MdNumberedItem(int number, int depth, String sep, MdElement value,MdElement[] children) {
        this.number = number;
        this.sep = sep;
        this.value = value;
        this.depth = depth;
        this.children = children;
        switch (depth) {
            case 1: {
                id = MdElementType.NUMBRED_ITEM1;
                break;
            }
            case 2: {
                id = MdElementType.NUMBRED_ITEM2;
                break;
            }
            case 3: {
                id = MdElementType.NUMBRED_ITEM3;
                break;
            }
            case 4: {
                id = MdElementType.NUMBRED_ITEM4;
                break;
            }
            case 5: {
                id = MdElementType.NUMBRED_ITEM5;
                break;
            }
            case 6: {
                id = MdElementType.NUMBRED_ITEM6;
                break;
            }
            default: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
    }

    public MdElement[] getChildren() {
        return children;
    }

    public int getDepth() {
        return depth;
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
    public MdElementType getElementType() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        sb.append(number).append(sep).append(" ").append(getValue());
        return sb.toString();
    }
    
}
