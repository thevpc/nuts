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

import net.thevpc.nuts.internal.rpi.NTextRPI;

/**
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
public interface NTableCellSpecBuilder {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTableCellSpecBuilder of() {
        return NTextRPI.of().createCellSpecBuilder();
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @return of result
     */
    static NTableCellSpecBuilder of(NText content) {
        /**
         * Creates a new instance of of.
         *
         * @param ).content(content ).content(content
         * @return of result
         */
        return of().content(content);
    }

    /**
     * Creates a new instance of of.
     *
     * @param content content
     * @param colspan colspan
     * @param rowspan rowspan
     * @return of result
     */
    static NTableCellSpecBuilder of(NText content, int colspan, int rowspan) {
        /**
         * Creates a new instance of of.
         *
         * @param ).content(content).colspan(colspan).rowspan(rowspan ).content(content).colspan(colspan).rowspan(rowspan
         * @return of result
         */
        return of().content(content).colspan(colspan).rowspan(rowspan);
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
     * Vertical align.
     *
     * @param align align
     * @return vertical align result
     */
    NTableCellSpecBuilder verticalAlign(NPositionType align);

    /**
     * Horizontal align.
     *
     * @param align align
     * @return horizontal align result
     */
    NTableCellSpecBuilder horizontalAlign(NPositionType align);

    /**
     * Colspan.
     *
     * @return colspan result
     */
    int colspan();

    /**
     * Colspan.
     *
     * @param colspan colspan
     * @return colspan result
     */
    NTableCellSpecBuilder colspan(int colspan);

    /**
     * Rowspan.
     *
     * @return rowspan result
     */
    int rowspan();

    /**
     * Rowspan.
     *
     * @param rowspan rowspan
     * @return rowspan result
     */
    NTableCellSpecBuilder rowspan(int rowspan);

    /**
     * Content.
     *
     * @return content result
     */
    NText content();

    /**
     * Content.
     *
     * @param content content
     * @return content result
     */
    NTableCellSpecBuilder content(NText content);

    /**
     * Build.
     *
     * @return build result
     */
    NTableCell build();
}
