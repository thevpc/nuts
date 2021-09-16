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

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdBold extends MdAbstractElement {

    private String type;
    private MdElement content;

    public MdBold(MdElement content) {
        this("**",content);
    }

    public MdBold(String type, MdElement content) {
        this.type = type;
        this.content = content;
    }

    @Override
    public boolean isBlank() {
        return content.isBlank();
    }

    public String getType() {
        return type;
    }

    public MdElement getContent() {
        return content;
    }

    @Override
    public MdElementType type() {
        return MdElementType.BOLD;
    }

    @Override
    public String toString() {
        return "**" + content + "**";
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
        MdBold mdBold = (MdBold) o;
        return Objects.equals(type, mdBold.type) && Objects.equals(content, mdBold.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, content);
    }
}
