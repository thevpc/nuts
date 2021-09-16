/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
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

import net.thevpc.nuts.NutsBlankable;

/**
 *
 * @author thevpc
 */
public interface MdElement extends NutsBlankable {
    MdElementType type();

    MdAdmonition asAdmonition();

    MdBold asBold();

    MdCode asCode();

    MdImage asImage();

    MdHr asHr();

    MdNumberedItem asNumItem();

    MdListElement asList();

    MdNumberedList asNumList();

    MdBody asBody();

    MdPhrase asPhrase();

    MdTable asTable();

    MdText asText();

    MdTitle asTitle();

    MdUnNumberedItem asUnNumItem();
    MdUnNumberedList asUnNumList();

    MdLink asLink();

    MdCodeLink asCodeLink();

    MdXml asXml();

    boolean isColumn();

    boolean isLink();

    boolean isRow();

    boolean isBody();

    boolean isPhrase();

    boolean isTable();

    boolean isText();

    boolean isCodeLink();

    boolean isTitle();

    boolean isXml();

    MdColumn asColumn();

    MdRow asRow();

    boolean isUnNumberedItem();

    boolean isImage();

    boolean isAdmonition();

    boolean isNumberedItem();

    MdItalic asItalic();

    boolean isBold();

    boolean isList();

    boolean isItalic();

    boolean isInline();

    boolean isEndWithNewline();
}
