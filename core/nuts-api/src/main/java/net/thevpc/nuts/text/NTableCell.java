/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.text;

/**
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NTableCell {
    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @return of result
     */
    static NTableCell of(String content) {
        return NTableCellBuilder.of(content == null ? null : NText.of(content)).build();
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @return of result
     */
    static NTableCell of(NText content) {
        return NTableCellBuilder.of(content).build();
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @param colspan colspan
     * @param rowspan rowspan
     * @return of result
     */
    static NTableCell of(NText content, int colspan, int rowspan) {
        return NTableCellBuilder.of(content, colspan, rowspan).build();
    }

    /**
     * Vertical align.
     *
     * @return vertical align result
     */
    NPositionType verticalAlign();

    /**
     * Horizontal align.
     *
     * @return horizontal align result
     */
    NPositionType horizontalAlign();

    /**
     * Colspan.
     *
     * @return colspan result
     */
    int colspan();

    /**
     * Rowspan.
     *
     * @return rowspan result
     */
    int rowspan();

    /**
     * Content.
     *
     * @return content result
     */
    NText content();

    /**
     * Builder.
     *
     * @return builder result
     */
    NTableCellBuilder builder();
}
