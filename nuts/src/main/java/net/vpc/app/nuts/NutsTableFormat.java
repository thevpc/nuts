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

import java.io.PrintStream;

/**
 * Created by vpc on 2/17/17.
 */
public interface NutsTableFormat {

    public boolean isVisibleHeader();

    public NutsTableFormat setVisibleHeader(boolean visibleHeader);

    public NutsTableFormat setColumnsConfig(String... names);

    public NutsTableFormat setColumnConfigIndex(String name, int index);

    public NutsTableFormatterBorders getBorder();

    public NutsTableFormat setBorder(NutsTableFormatterBorders border);

    public void print(PrintStream out);

    public NutsTableFormat setVisibleColumn(int col, boolean visible);

    public NutsTableFormat unsetVisibleColumn(int col);

    public Boolean getVisibleColumn(int col);

    NutsTableFormat setCellFormatter(NutsTableCellFormat formatter);

    public NutsTableFormat newRow();

    public NutsTableFormat setHeader(Object... values);

    public NutsTableFormat addHeaderCells(Object... values);

    public NutsTableCell addHeaderCell(Object value);

    public NutsTableFormat addRow(Object... values);

    public NutsTableFormat addCells(Object... values);

    public NutsTableCell addCell(Object value);

    public boolean configure(NutsCommandLine cmdLine);

    public enum Separator {
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

        Separator(char c) {
            this.c = c;
        }
    }

}
