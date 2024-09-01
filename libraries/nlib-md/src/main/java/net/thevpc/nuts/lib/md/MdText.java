/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
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

import net.thevpc.nuts.util.NBlankable;

import java.util.Objects;

/**
 * @author thevpc
 */
public class MdText extends MdAbstractElement {
    private String text;
    private boolean inline;

    public MdText(String value, boolean inline) {
        this.text = value;
        this.inline = inline;
    }

    public static MdText phrase(String s) {
        return new MdText(s, true);
    }

    public static MdText empty() {
        return new MdText("", true);
    }

    public static MdText paragraph(String s) {
        return new MdText(s, false);
    }

    public String getText() {
        return text;
    }

    @Override
    public MdElementType type() {
        return MdElementType.TEXT;
    }

    @Override
    public boolean isInline() {
        return inline;
    }

    @Override
    public String toString() {
        return String.valueOf(text);
    }

    public MdText toNewline() {
        if(!isInline()){
            return this;
        }
        return new MdText(text, false);
    }

    public MdText toInline() {
        if (isInline()) {
            return this;
        }
        return new MdText(text, true);
    }

    @Override
    public boolean isEndWithNewline() {
        return !isInline();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdText mdText = (MdText) o;
        return inline == mdText.inline && Objects.equals(text, mdText.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text, inline);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(text);
    }
}
