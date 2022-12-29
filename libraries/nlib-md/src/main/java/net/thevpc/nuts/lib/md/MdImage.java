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

import net.thevpc.nuts.NBlankable;

import java.util.Objects;

/**
 *
 * @author thevpc
 */
public class MdImage extends MdAbstractElement {

    private String type;
    private ImageFormat imageFormat;
    private String imageTitle;
    private String imageUrl;

    public MdImage(String type, ImageFormat imageFormat,String imageTitle, String imageUrl) {
        this.type = type;
        this.imageFormat = imageFormat;
        this.imageTitle = imageTitle;
        this.imageUrl = imageUrl;
    }

    public ImageFormat getImageFormat() {
        return imageFormat;
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
    public MdElementType type() {
        return MdElementType.IMAGE;
    }

    @Override
    public String toString() {
        return "![" + imageTitle + "](" + imageUrl + ")";
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
        MdImage mdImage = (MdImage) o;
        return Objects.equals(type, mdImage.type) && Objects.equals(imageTitle, mdImage.imageTitle) && Objects.equals(imageUrl, mdImage.imageUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, imageTitle, imageUrl);
    }

    public enum ImageFormat{
        PATH,
        ID,
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(imageTitle) && NBlankable.isBlank(imageUrl);
    }
}
