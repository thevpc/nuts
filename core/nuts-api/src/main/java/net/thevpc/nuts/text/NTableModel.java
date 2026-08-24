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
public interface NTableModel {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NMutableTableModel of() {
        return NMutableTableModel.of();
    }

    /**
     * Creates a new instance of of.
     *
     * @param text text
     * @return of result
     */
    static NTableModel of(NText text) {
        return NMutableTableModel.of().addRow(NTableCell.of(text));
    }

    /**
     * Columns count.
     *
     * @return columns count result
     */
    int columnsCount();

    /**
     * Rows count.
     *
     * @return rows count result
     */
    int rowsCount();

    /**
     * Returns the cell.
     *
     * @param row row
     * @param column column
     * @return get cell result
     */
    NTableCellDef getCell(int row, int column);

    /**
     * Returns the header.
     *
     * @param column column
     * @return get header result
     */
    NTableCellDef getHeader(int column);

    /**
     * Returns the header col span.
     *
     * @param column column
     * @return get header col span result
     */
    default int getHeaderColSpan(int column) {
        return 1;
    }

}
