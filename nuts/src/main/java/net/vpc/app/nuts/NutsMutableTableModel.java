/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * Mutable Table Model
 * @author vpc
 */
public interface NutsMutableTableModel extends NutsTableModel {

    /**
     * add new row to the model
     * @return {@code this} instance
     */
    NutsMutableTableModel newRow();

    /**
     * clear header
     * @return {@code this} instance
     */
    NutsMutableTableModel clearHeader();

    /**
     *  add header cells
     * @param values cells
     * @return {@code this} instance
     */
    NutsMutableTableModel addHeaderCells(Object... values);

    /**
     * add header cell
     * @param value cell
     * @return {@code this} instance
     */
    NutsMutableTableModel addHeaderCell(Object value);

    /**
     * add row cells
     * @param values row cells
     * @return {@code this} instance
     */
    NutsMutableTableModel addRow(Object... values);

    /**
     * add row cells
     * @param values row cells
     * @return {@code this} instance
     */
    NutsMutableTableModel addCells(Object... values);

    /**
     * add row cell
     * @param value cell
     * @return {@code this} instance
     */
    NutsMutableTableModel addCell(Object value);

    /**
     * update cell at the given position
     * @param row row index
     * @param column column index
     * @param value cell value
     * @return {@code this} instance
     */
    NutsMutableTableModel setCellValue(int row, int column, Object value);

    /**
     * update cell colspan
     * @param row row index
     * @param column column index
     * @param value new value
     * @return {@code this} instance
     */
    NutsMutableTableModel setCellColSpan(int row, int column, int value);

    /**
     * update cell rowspan
     * @param row row index
     * @param column column index
     * @param value new value
     * @return {@code this} instance
     */
    NutsMutableTableModel setCellRowSpan(int row, int column, int value);

    /**
     * update header value
     * @param column header column
     * @param value new value
     * @return {@code this} instance
     */
    NutsMutableTableModel setHeaderValue(int column, Object value);

    /**
     * update header colspan
     * @param column new value
     * @param value new value
     * @return {@code this} instance
     */
    NutsMutableTableModel setHeaderColSpan(int column, int value);

}
