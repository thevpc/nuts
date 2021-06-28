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
public class MdCode extends MdAbstractElement {

    private String language;
    private String value;
    private boolean inline;

    public MdCode(String code, String value, boolean inline) {
        this.language = code==null?"":code;
        this.value = value;
        this.inline = inline;
    }

    public boolean isInline() {
        return inline;
    }

    public String getLanguage() {
        return language;
    }

    public String getValue() {
        return value;
    }

    @Override
    public MdElementType getElementType() {
        return MdElementType.CODE;
    }

    @Override
    public String toString() {
        return "```" + language + "\n"
                + value + "\n"
                + "```";
    }

}
