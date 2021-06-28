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
public class MdImage extends MdAbstractElement {

    private String type;
    private String imageTitle;
    private String imageUrl;

    public MdImage(String type, String imageTitle, String imageUrl) {
        this.type = type;
        this.imageTitle = imageTitle;
        this.imageUrl = imageUrl;
    }

    public String getType() {
        return type;
    }

    public String getImageTitle() {
        return imageTitle;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public MdElementType getElementType() {
        return MdElementType.IMAGE;
    }

    @Override
    public String toString() {
        return "![" + imageTitle + "](" + imageUrl + ")";
    }

}
