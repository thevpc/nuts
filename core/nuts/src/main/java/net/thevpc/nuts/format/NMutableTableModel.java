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
package net.thevpc.nuts.format;

import net.thevpc.nuts.NExtensions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.spi.NComponent;

/**
 * Mutable Table Model
 *
 * @author thevpc
 * @app.category Format
 */
public interface NMutableTableModel extends NTableModel, NComponent {

    static NMutableTableModel of(NSession session) {
        return NExtensions.of(session).createComponent(NMutableTableModel.class).get();
    }

    /**
     * add new row to the model
     *
     * @return {@code this} instance
     */
    NMutableTableModel newRow();

    /**
     * clear header
     *
     * @return {@code this} instance
     */
    NMutableTableModel clearHeader();

    /**
     * add header cells
     *
     * @param values cells
     * @return {@code this} instance
     */
    NMutableTableModel addHeaderCells(Object... values);

    /**
     * add header cell
     *
     * @param value cell
     * @return {@code this} instance
     */
    NMutableTableModel addHeaderCell(Object value);

    /**
     * add row cells
     *
     * @param values row cells
     * @return {@code this} instance
     */
    NMutableTableModel addRow(Object... values);

    /**
     * add row cells
     *
     * @param values row cells
     * @return {@code this} instance
     */
    NMutableTableModel addCells(Object... values);

    /**
     * add row cell
     *
     * @param value cell
     * @return {@code this} instance
     */
    NMutableTableModel addCell(Object value);

    /**
     * update cell at the given position
     *
     * @param row    row index
     * @param column column index
     * @param value  cell value
     * @return {@code this} instance
     */
    NMutableTableModel setCellValue(int row, int column, Object value);

    /**
     * update cell colspan
     *
     * @param row    row index
     * @param column column index
     * @param value  new value
     * @return {@code this} instance
     */
    NMutableTableModel setCellColSpan(int row, int column, int value);

    /**
     * update cell rowspan
     *
     * @param row    row index
     * @param column column index
     * @param value  new value
     * @return {@code this} instance
     */
    NMutableTableModel setCellRowSpan(int row, int column, int value);

    /**
     * update header value
     *
     * @param column header column
     * @param value  new value
     * @return {@code this} instance
     */
    NMutableTableModel setHeaderValue(int column, Object value);

    /**
     * update header colspan
     *
     * @param column new value
     * @param value  new value
     * @return {@code this} instance
     */
    NMutableTableModel setHeaderColSpan(int column, int value);

}
