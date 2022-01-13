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
public abstract class MdAbstractElement implements MdElement {

    @Override
    public MdAdmonition asAdmonition() {
        return (MdAdmonition) this;
    }

    @Override
    public MdBold asBold() {
        return (MdBold) this;
    }

    @Override
    public MdItalic asItalic() {
        return (MdItalic) this;
    }

    @Override
    public MdCode asCode() {
        return (MdCode) this;
    }

    @Override
    public MdImage asImage() {
        return (MdImage) this;
    }

    @Override
    public MdHr asHr() {
        return (MdHr) this;
    }

    @Override
    public MdNumberedItem asNumItem() {
        return (MdNumberedItem) this;
    }

    @Override
    public MdUnNumberedItem asUnNumItem() {
        return (MdUnNumberedItem) this;
    }

    @Override
    public MdColumn asColumn() {
        return (MdColumn) this;
    }

    @Override
    public MdRow asRow() {
        return (MdRow) this;
    }

    @Override
    public MdTable asTable() {
        return (MdTable) this;
    }

    @Override
    public MdText asText() {
        return (MdText) this;
    }

    @Override
    public MdTitle asTitle() {
        return (MdTitle) this;
    }

    @Override
    public MdLink asLink() {
        return (MdLink) this;
    }

    @Override
    public MdCodeLink asCodeLink() {
        return (MdCodeLink) this;
    }

    @Override
    public MdXml asXml() {
        return (MdXml) this;
    }

    @Override
    public boolean isText() {
        return this instanceof MdText;
    }

    @Override
    public boolean isXml() {
        return this instanceof MdXml;
    }

    @Override
    public boolean isLink() {
        return this instanceof MdLink;
    }

    @Override
    public boolean isCodeLink() {
        return this instanceof MdCodeLink;
    }

    @Override
    public boolean isTitle() {
        return this instanceof MdTitle;
    }

    @Override
    public boolean isTable() {
        return this instanceof MdTable;
    }

    @Override
    public boolean isBold() {
        return this instanceof MdBold;
    }

    @Override
    public boolean isItalic() {
        return this instanceof MdItalic;
    }

    @Override
    public boolean isRow() {
        return this instanceof MdRow;
    }

    @Override
    public boolean isColumn() {
        return this instanceof MdColumn;
    }

    @Override
    public boolean isNumberedItem() {
        return this instanceof MdNumberedItem;
    }

    @Override
    public boolean isUnNumberedItem() {
        return this instanceof MdUnNumberedItem;
    }

    @Override
    public boolean isAdmonition() {
        return this instanceof MdAdmonition;
    }

    @Override
    public boolean isImage() {
        return this instanceof MdImage;
    }

    @Override
    public boolean isList() {
        return this instanceof MdListElement;
    }

    @Override
    public MdListElement asList() {
        return (MdListElement) this;
    }

    @Override
    public MdNumberedList asNumList() {
        return (MdNumberedList) this;
    }

    @Override
    public MdBody asBody() {
        return (MdBody) this;
    }

    @Override
    public MdPhrase asPhrase() {
        return (MdPhrase) this;
    }

    @Override
    public MdUnNumberedList asUnNumList() {
        return (MdUnNumberedList)this;
    }

    @Override
    public boolean isBody() {
        return this instanceof MdBody;
    }

    @Override
    public boolean isPhrase() {
        return this instanceof MdPhrase;
    }

}
