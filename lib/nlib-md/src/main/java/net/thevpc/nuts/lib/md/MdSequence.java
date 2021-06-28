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
public class MdSequence extends MdAbstractElement {

    private String code;
    private MdElement[] elements;
    private boolean inline;

    public MdSequence(String code, MdElement[] content, boolean inline) {
        this.code = code;
        this.elements = content;
//        if (inline) {
//            for (MdElement mdElement : content) {
//                if (mdElement.toString().startsWith("##")) {
//                    System.out.println("Why");
//                }
//            }
//        }
        this.inline = inline;
    }

    public boolean isInline() {
        return inline;
    }

    public String getCode() {
        return code;
    }

    public MdElement get(int i) {
        return elements[i];
    }

    public int size() {
        return elements.length;
    }

    public MdElement[] getElements() {
        return elements;
    }

    @Override
    public MdElementType getElementType() {
        return MdElementType.SEQ;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < elements.length; i++) {
            if (i > 0) {
                if (!inline) {
                    sb.append("\n");
                }
            }
            sb.append(elements[i]);
        }
        return sb.toString();
    }

}
