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

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdItalic extends MdAbstractElement {

    private String type;
    private MdElement content;

    public MdItalic(MdElement content) {
        this("__",content);
    }

    public MdItalic(String type, MdElement content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public MdElement getContent() {
        return content;
    }

    @Override
    public MdElementType type() {
        return MdElementType.ITALIC;
    }

    @Override
    public String toString() {
        return "__" + content + "__";
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
        MdItalic mdItalic = (MdItalic) o;
        return Objects.equals(type, mdItalic.type) && Objects.equals(content, mdItalic.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }

    @Override
    public boolean isBlank() {
        return content.isBlank();
    }
}
