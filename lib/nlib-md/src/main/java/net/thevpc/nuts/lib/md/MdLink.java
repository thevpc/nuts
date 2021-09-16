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

import net.thevpc.nuts.NutsBlankable;

import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdLink extends MdAbstractElement {

    private String type;
    private String linkTitle;
    private String linkUrl;

    public MdLink(String type, String linkTitle, String linkUrl) {
        this.type = type;
        this.linkTitle = linkTitle;
        this.linkUrl = linkUrl;
    }

    public String getType() {
        return type;
    }

    public String getLinkTitle() {
        return linkTitle;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    @Override
    public MdElementType type() {
        return MdElementType.LINK;
    }

    @Override
    public String toString() {
        return "[" + linkTitle + "](" + linkUrl + ")";
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
        MdLink mdLink = (MdLink) o;
        return Objects.equals(type, mdLink.type) && Objects.equals(linkTitle, mdLink.linkTitle) && Objects.equals(linkUrl, mdLink.linkUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, linkTitle, linkUrl);
    }

    @Override
    public boolean isBlank() {
        return NutsBlankable.isBlank(linkTitle) &&  NutsBlankable.isBlank(linkUrl);
    }
}
