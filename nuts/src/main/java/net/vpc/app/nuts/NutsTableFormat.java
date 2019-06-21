/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsTableFormat extends NutsFormat {

    @Override
    NutsTableFormat session(NutsSession session);

    @Override
    NutsTableFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsTableFormat configure(boolean skipUnsupported, String... args);

    boolean isVisibleHeader();

    NutsTableFormat setVisibleHeader(boolean visibleHeader);

    NutsTableFormat setColumnsConfig(String... names);

    NutsTableFormat setColumnConfigIndex(String name, int index);

    NutsTableBordersFormat getBorder();

    NutsTableFormat setBorder(NutsTableBordersFormat border);

    NutsTableFormat setVisibleColumn(int col, boolean visible);

    NutsTableFormat unsetVisibleColumn(int col);

    Boolean getVisibleColumn(int col);

    NutsTableFormat setCellFormat(NutsTableCellFormat formatter);

    NutsTableFormat newRow();

    NutsTableFormat clearHeader();

    NutsTableFormat addHeaderCells(Object... values);

    NutsTableFormat addHeaderCell(Object value);

    NutsTableFormat addRow(Object... values);

    NutsTableFormat addCells(Object... values);

    NutsTableFormat addCell(Object value);

    NutsTableModel getModel();

    void setModel(NutsTableModel model);

    enum Separator {
        FIRST_ROW_START('A'),
        FIRST_ROW_LINE('B'),
        FIRST_ROW_SEP('C'),
        FIRST_ROW_END('D'),
        ROW_START('E'),
        ROW_SEP('F'),
        ROW_END('G'),
        INTER_ROW_START('H'),
        INTER_ROW_LINE('I'),
        INTER_ROW_SEP('J'),
        INTER_ROW_END('K'),
        LAST_ROW_START('L'),
        LAST_ROW_LINE('M'),
        LAST_ROW_SEP('N'),
        LAST_ROW_END('O');
        char c;
        private final String id;

        Separator(char c) {
            this.c = c;
            this.id = name().toLowerCase().replace('_', '-');
        }

        public String id() {
            return id;
        }
    }

}
