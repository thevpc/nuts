/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util.cmdline.format;


import java.io.PrintStream;
import java.util.*;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsTableCell;
import net.vpc.app.nuts.NutsTableCellFormat;
import net.vpc.app.nuts.NutsTableFormatterBorders;
import net.vpc.app.nuts.NutsTableFormat;

/**
 * Created by vpc on 2/17/17.
 */
public class DefaultTableFormat implements NutsTableFormat{
    public static NutsTableFormatterBorders NO_BORDER = new DefaultTableFormatterBorders(
            "", "", "", "",
            "", "", "",
            "", "", "", "",
            "", "", "", ""
    );
    public static NutsTableFormatterBorders SIMPLE_BORDER = new DefaultTableFormatterBorders(
            ".", "-", "-", ".",
            "|", "|", "|",
            "|", "-", "+", "|",
            ".", "-", "-", "."
    );
    public static NutsTableFormatterBorders FANCY_ROWS_BORDER = new DefaultTableFormatterBorders(
            "", "", "", "",
            "", " ", "",
            "", "-", "-", "",
            "", "", "", ""
    );
    public static NutsTableFormatterBorders SPACE_BORDER = new DefaultTableFormatterBorders(
            "", "", "", "",
            "", " ", "",
            "", "", "", "",
            "", "", "", ""
    );
    public static NutsTableFormatterBorders FANCY_COLUMNS_BORDER = new DefaultTableFormatterBorders(
            "", "", "", "",
            "", "|", "",
            "", "", "", "",
            "", "", "", ""
    );
    private NutsTableCellFormat defaultFormatter;
    /**
     * ABBBBCBBBBD
     * E    F    G
     * HIIIIJIIIIK
     * E    F    G
     * LMMMMNMMMMO
     */
    private NutsTableFormatterBorders border = SPACE_BORDER;
    private Row header = new Row();
    private List<Row> rows = new ArrayList<>();
    private List<Boolean> visibleColumns = new ArrayList<>();
    private Map<String, Integer> columns = new HashMap<>();
    private boolean visibleHeader = true;


    
//    public static void main(String[] args) {
//        TableFormatter t = new TableFormatter();
//        t.addCell("****");//.setColspan(2).setRowspan(2);
//        t.addCell("****");
//        t.addCell("****");
//        t.newRow();
//        t.addCell("****");
//        t.addCell("****");
//        t.addCell("");
//        t.setBorder(SPACE_BORDER);
//
//        System.out.println(t);
//    }

    public DefaultTableFormat(NutsTableCellFormat formatter) {
        setCellFormatter(formatter);
    }

    public boolean isVisibleHeader() {
        return visibleHeader;
    }

    public DefaultTableFormat setVisibleHeader(boolean visibleHeader) {
        this.visibleHeader = visibleHeader;
        return this;
    }

    public DefaultTableFormat setColumnsConfig(String... names) {
        for (int i = 0; i < names.length; i++) {
            this.columns.put(names[i], i);
        }
        return this;
    }

    public DefaultTableFormat setColumnConfigIndex(String name, int index) {
        this.columns.put(name, index);
        return this;
    }

    public NutsTableFormatterBorders getBorder() {
        return border;
    }

    public DefaultTableFormat setBorder(NutsTableFormatterBorders border) {
        this.border = border;
        return this;
    }

    private String getSeparator(Separator id) {
        String s = border.get(id);
        if (s == null) {
            return "";
        }
        return s;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        List<Row> rows = rebuild();
        if (rows.size() > 0) {
            List<DefaultCell> cells = rows.get(0).cells;
            if (
                    (getSeparator(Separator.FIRST_ROW_START) +
                            getSeparator(Separator.FIRST_ROW_SEP) +
                            getSeparator(Separator.FIRST_ROW_LINE) +
                            getSeparator(Separator.FIRST_ROW_END)
                    ).length() > 0
                    ) {
                sb.append(getSeparator(Separator.FIRST_ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        sb.append(getSeparator(Separator.FIRST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String B = getSeparator(Separator.FIRST_ROW_LINE);
                    String s = cell.rendered.toString();
                    sb.append(FormatUtils.fillString(B, s.length()));
                }
                sb.append(getSeparator(Separator.FIRST_ROW_END));
                sb.append("\n");
            }
            for (int i1 = 0; i1 < rows.size(); i1++) {
                if (i1 > 0) {
                    if (
                            (getSeparator(Separator.INTER_ROW_START) +
                                    getSeparator(Separator.INTER_ROW_SEP) +
                                    getSeparator(Separator.INTER_ROW_LINE) +
                                    getSeparator(Separator.INTER_ROW_END)
                            ).length() > 0
                            ) {
                        sb.append(getSeparator(Separator.INTER_ROW_START));
                        for (int i = 0; i < cells.size(); i++) {
                            if (i > 0) {
                                sb.append(getSeparator(Separator.INTER_ROW_SEP));
                            }
                            DefaultCell cell = cells.get(i);
                            String B = getSeparator(Separator.INTER_ROW_LINE);
                            String s = cell.rendered.toString();
                            sb.append(FormatUtils.fillString(B, s.length()));
                        }
                        sb.append(getSeparator(Separator.INTER_ROW_END));
                        sb.append("\n");
                    }
                }


                Row row = rows.get(i1);
                cells = row.cells;

                sb.append(getSeparator(Separator.ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        sb.append(getSeparator(Separator.ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String s = cell.rendered.toString();
                    sb.append(s);
                }
                sb.append(getSeparator(Separator.ROW_END));

                sb.append("\n");
            }

            if (
                    (getSeparator(Separator.LAST_ROW_START) +
                            getSeparator(Separator.LAST_ROW_SEP) +
                            getSeparator(Separator.LAST_ROW_LINE) +
                            getSeparator(Separator.LAST_ROW_END)
                    ).length() > 0
                    ) {
                sb.append(getSeparator(Separator.LAST_ROW_START));
                cells = rows.get(0).cells;
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        sb.append(getSeparator(Separator.LAST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String B = getSeparator(Separator.LAST_ROW_LINE);
                    String s = cell.rendered.toString();
                    sb.append(FormatUtils.fillString(B, s.length()));
                }
                sb.append(getSeparator(Separator.LAST_ROW_END));
            }
        }
        return sb.toString();
    }

    public void print(PrintStream out) {
        rebuild();
        for (Row row : this.rows) {
            for (DefaultCell cell : row.cells) {
                out.print(cell.rendered);
            }
            out.println();
        }
    }

    private static class Row {

        NutsTableCellFormat formatter;
        List<DefaultCell> cells = new ArrayList<>();
    }


    private static class RenderedCell {

        char[][] rendered;
        int rows;
        int columns;
        NutsTableCellFormat formatter;

        private RenderedCell() {

        }

        public RenderedCell(String str, NutsTableCellFormat formatter) {
            this.formatter = formatter;
            if (str == null) {
                str = "";
            }
            List<StringBuilder> strings = new ArrayList<>();
            StringTokenizer st = new StringTokenizer(str, "\n", true);
            columns = 0;
            while (st.hasMoreElements()) {
                String e = st.nextToken();
                if (e.equals("\n")) {
                    strings.add(new StringBuilder());
                } else {
                    if (strings.isEmpty()) {
                        strings.add(new StringBuilder());
                    }
                    StringBuilder last = strings.get(strings.size() - 1);
                    last.append(e);
                    columns = Math.max(columns, formatter.stringLength(last.toString()));
                }
            }
            rows = strings.size();
            rendered = new char[rows][];
            for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                StringBuilder s = strings.get(i);
                int length = formatter.stringLength(s.toString());
                while (length < columns) {
                    s.append(' ');
                    length++;
                }
                rendered[i] = s.toString().toCharArray();
            }
        }

        public RenderedCell appendHorizontally(RenderedCell other) {
            char[][] rendered0 = new char[Math.max(rows, other.rows)][];
            for (int i = 0; i < rendered0.length; i++) {
                StringBuilder sb = new StringBuilder();
                if (i < rendered.length) {
                    sb.append(rendered[i]);
                } else {
                    char[] a = new char[columns];
                    Arrays.fill(a, 0, columns, ' ');
                    sb.append(a);
                }
                if (i < other.rendered.length) {
                    sb.append(other.rendered[i]);
                } else {
                    char[] a = new char[other.columns];
                    Arrays.fill(a, 0, other.columns, ' ');
                    sb.append(a);
                }
                rendered0[i] = sb.toString().toCharArray();
            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns + other.columns;
            c.rows = rendered0.length;
            return c;
        }

        public RenderedCell appendVertically(RenderedCell other) {
            char[][] rendered0 = new char[rows + other.rows][];
            int cols = Math.max(columns, other.columns);
            for (int i = 0; i < rendered.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(rendered[i]);
                int remaining = cols - columns;
                if (remaining > 0) {
                    char[] a = new char[remaining];
                    Arrays.fill(a, 0, remaining, ' ');
                    sb.append(a);
                }
                rendered0[i] = sb.toString().toCharArray();
            }
            for (int i = 0; i < other.rendered.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append(other.rendered[i]);
                int remaining = cols - other.columns;
                if (remaining > 0) {
                    char[] a = new char[remaining];
                    Arrays.fill(a, 0, remaining, ' ');
                    sb.append(a);
                }
                rendered0[i + rendered.length] = sb.toString().toCharArray();
            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns + other.columns;
            c.rows = rendered0.length;
            return c;
        }

        public RenderedCell replaceContent(RenderedCell other, int row, int col) {
            char[][] rendered0 = resize(Math.max(rows, row + other.rows), Math.max(columns, col + other.columns)).rendered;
            for (int r = 0; r < other.rows; r++) {
                for (int c = 0; c < other.columns; c++) {
                    rendered0[row + r][col + c] = other.rendered[r][c];
                }
            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public RenderedCell subCell(int row, int col, int toRow, int toCol) {
            char[][] rendered0 = new char[toRow - row][toCol - col];
            for (int i = 0; i < rendered0.length; i++) {
                System.arraycopy(rendered[i + row], col, rendered0[i], 0, toCol - col);
            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public RenderedCell resize(int rows, int columns) {
            char[][] rendered0 = new char[rows][];
            for (int i = 0; i < rows; i++) {
                if (i < rendered.length) {
                    char[] chars = rendered[i];
                    int ll = formatter.stringLength(new String(chars));
                    int min = Math.min(columns, ll);
                    if (min < columns) {
                        int x = columns - min;
                        StringBuilder s = new StringBuilder();
                        s.append(chars);
                        for (int j = 0; j < x; j++) {
                            s.append(' ');
                        }
                        rendered0[i] = s.toString().toCharArray();
                    } else {
                        rendered0[i] = rendered[i];
                    }
                } else {
                    rendered0[i] = FormatUtils.fillString(' ',columns).toCharArray();
                }
            }
//            for (int i = 0; i < rendered0.length; i++) {
//                if(i<rendered.length) {
//                    rendered0[i] = new char[rendered[i].length];
//                    System.arraycopy(rendered[i], 0, rendered0[i], 0, rendered[i].length);
//                }else{
//                    rendered0[i] = new char[columns];
//                    Arrays.fill(rendered0[i],0,columns,' ');
//                }
//            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            c.formatter = formatter;
            return c;
        }

        public RenderedCell copy() {
            char[][] rendered0 = new char[rendered.length][];
            for (int i = 0; i < rendered0.length; i++) {
                rendered0[i] = new char[rendered[i].length];
                System.arraycopy(rendered[i], 0, rendered0[i], 0, rendered[i].length);
            }
            RenderedCell c = new RenderedCell();
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (int i = 0, renderedLength = rendered.length; i < renderedLength; i++) {
                char[] chars = rendered[i];
                if (i > 0) {
                    sb.append("\n");
                }
                sb.append(chars);
            }
            return sb.toString();
        }
    }

    private static class DefaultCell implements NutsTableCell{

        int colspan = 1;
        int rowspan = 1;
        int x;
        int y;
        int cx;
        int cy;
        int cw;
        int ch;

        Object value;
        NutsTableCellFormat formatter;
        RenderedCell rendered;

        public RenderedCell getRendered() {
            return rendered;
        }

        public void setRendered(RenderedCell rendered) {
            this.rendered = rendered;
        }

        public int getColspan() {
            return colspan;
        }

        public DefaultCell setColspan(int colspan) {
            this.colspan = colspan <= 0 ? 1 : colspan;
            return this;
        }

        public int getRowspan() {
            return rowspan;
        }

        public DefaultCell setRowspan(int rowspan) {
            this.rowspan = rowspan <= 0 ? 1 : rowspan;
            return this;
        }

        public int getX() {
            return x;
        }

        //        public void setX(int x) {
//            this.x = x;
//        }
        public int getY() {
            return y;
        }

        //        public void setY(int y) {
//            this.y = y;
//        }
        public Object getValue() {
            return value;
        }

        public DefaultCell setValue(Object value) {
            this.value = value;
            return this;
        }

        public NutsTableCellFormat getFormatter() {
            return formatter;
        }

        public DefaultCell setFormatter(NutsTableCellFormat formatter) {
            this.formatter = formatter;
            return this;
        }

        @Override
        public String toString() {
            return "Cell{"
                    + "" + x + "->" + (x + colspan)
                    + ", " + y + "->" + (y + rowspan)
                    + ", " + value
                    + (formatter == null ? "" : (", formatter=" + formatter))
                    + '}';
        }
    }

    private List<Row> rebuild() {
        List<Row> rows1 = this.rows;
        List<Row> effectiveRows = new ArrayList<>();
        if (header.cells.size() > 0 && isVisibleHeader()) {
            effectiveRows.add(this.header);
        }
        boolean p = isVisibleColumnPositive();
        boolean n = isVisibleColumnNegative();
        for (int i = 0; i < rows1.size(); i++) {
            Row row = rows1.get(i);
            Row r2 = new Row();
            r2.formatter = row.formatter;
            List<DefaultCell> cells = row.cells;
            for (int i1 = 0; i1 < cells.size(); i1++) {
                DefaultCell cell = cells.get(i1);
                if (isVisibleColumn(i1, p, n)) {
                    r2.cells.add(cell);
                }
            }
            if (r2.cells.size() > 0) {
                effectiveRows.add(r2);
            }
        }


        //first pass to eval renderedText and effective positions
        Bounds b = new Bounds();
        Bounds cb = new Bounds();
        int r = 0;
        int cr = 0;
        for (Row row : effectiveRows) {
            int c = 0;
            int cc = 0;
            for (DefaultCell cell : row.cells) {
                int r0 = r;
                int c0 = c;
                int cr0 = cr;
                int cc0 = cc;
//                while(b.isReserved(r0,c0)){
//                    r0++;
//                }
                while (b.isReserved(c0, r0)) {
                    c0++;
                }
                while (cb.isReserved(cc0, cr0)) {
                    cc0++;
                }
                cell.cx = cc0;
                cell.cy = cr0;
                cell.x = c0;
                cell.y = r0;
                NutsTableCellFormat formatter = cell.formatter;
                if (formatter == null) {
                    formatter = row.formatter;
                }
                if (formatter == null) {
                    formatter = defaultFormatter;
                }
                cell.setRendered(new RenderedCell(
                        formatter.format(r0, c0, cell.getValue()),
                        formatter
                ));
                cell.cw = cell.getRendered().columns;
                cell.ch = cell.getRendered().rows;
                b.setColumnIntervalMinSize(cell.x, cell.x + cell.colspan, cell.cw);
                b.setRowIntervalMinSize(cell.y, cell.y + cell.rowspan, cell.ch);
                for (int i = 0; i < cell.rowspan; i++) {
                    for (int j = 0; j < cell.colspan; j++) {
                        b.addReservation(c0 + j, r0 + i);
                    }
                }
                c++;
            }
            b.discardRow(r);
            r++;
        }
        // second pass to update sizes
        for (Row row : effectiveRows) {
            for (DefaultCell cell : row.cells) {
                int rows = b.evalRowSize(cell.y, cell.rowspan);
                int columns = b.evalColumnSize(cell.x, cell.rowspan);
                cell.rendered = cell.rendered.resize(rows, columns);
                cell.cw = cell.getRendered().columns;
                cell.ch = cell.getRendered().rows;
            }
        }
        return effectiveRows;
    }

    public NutsTableFormat setCellFormatter(NutsTableCellFormat formatter){
        defaultFormatter = formatter == null ? new NutsTableCellFormat() {
            @Override
            public int stringLength(String value) {
                return value.length();
            }

            @Override
            public String format(int row, int col, Object value) {
                return String.valueOf(value);
            }
        } : formatter
        ;
        return this;
    }
    
    private boolean isVisibleColumnPositive() {
        for (Boolean visibleColumn : visibleColumns) {
            if (visibleColumn != null && visibleColumn) {
                return true;
            }
        }
        return false;
    }

    private boolean isVisibleColumnNegative() {
        for (Boolean visibleColumn : visibleColumns) {
            if (visibleColumn != null && !visibleColumn) {
                return true;
            }
        }
        return false;
    }

    public DefaultTableFormat setVisibleColumn(int col, boolean visible) {
        if (col >= 0) {
            while (col < visibleColumns.size()) {
                visibleColumns.add(null);
            }
            visibleColumns.set(col, visible);
        }
        return this;
    }

    public DefaultTableFormat unsetVisibleColumn(int col) {
        if (col >= 0 && col < visibleColumns.size()) {
            visibleColumns.set(col, null);
        }
        return this;
    }

    public Boolean getVisibleColumn(int col) {
        if (col >= 0 && col < visibleColumns.size()) {
            return visibleColumns.get(col);
        }
        return null;
    }

    private boolean isVisibleColumn(int col, boolean p, boolean n) {
        Boolean b = null;
        if (col >= 0 && col < visibleColumns.size()) {
            b = visibleColumns.get(col);
        }
        if (b == null) {
            if (!p && !n) {
                return true;
            } else if (p && !n) {
                return false;
            } else if (!p && n) {
                return true;
            } else if (p && n) {
                return false;
            }
        }
        return true;
    }

    public DefaultTableFormat newRow() {
        rows.add(new Row());
        return this;
    }

    public DefaultTableFormat setHeader(Object... values) {
        header.cells.clear();
        addHeaderCell(values);
        return this;
    }

    public DefaultTableFormat addHeaderCells(Object... values) {
        for (Object value : values) {
            addHeaderCell(value);
        }
        return this;
    }

    public DefaultCell addHeaderCell(Object value) {
        DefaultCell c = new DefaultCell();
        c.value = value;
        header.cells.add(c);
        return c;
    }

    public DefaultTableFormat addRow(Object... values) {
        newRow();
        addCells(values);
        return this;
    }

    public DefaultTableFormat addCells(Object... values) {
        for (Object value : values) {
            addCell(value);
        }
        return this;
    }

    public DefaultCell addCell(Object value) {
        if (rows.size() == 0) {
            newRow();
        }
        DefaultCell c = new DefaultCell();
        c.value = value;
        rows.get(rows.size() - 1).cells.add(c);
        return c;
    }

    private static class Pos {
        int column;
        int row;

        public Pos(int column, int row) {
            this.column = column;
            this.row = row;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pos pos = (Pos) o;
            return column == pos.column &&
                    row == pos.row;
        }

        @Override
        public int hashCode() {

            return Objects.hash(column, row);
        }

        @Override
        public String toString() {
            return "(" +
                    "" + column +
                    "," + row +
                    ')';
        }

    }

    private static class Widths {

        Map<Integer, Integer> colWidth = new HashMap<>();
        Map<Integer, Integer> rowHeight = new HashMap<>();

        public void updateCellSize(int row, int col, int width, int height) {
            Integer v = colWidth.get(col);
            if (v == null) {
                v = width;
            } else {
                v = Math.max(width, v);
            }
            colWidth.put(col, v);
            v = rowHeight.get(row);
            if (v == null) {
                v = height;
            } else {
                v = Math.max(height, v);
            }
            rowHeight.put(row, v);
        }

        private RenderedCell resize(int row, int col, RenderedCell r) {
            return r.resize(rowHeight.get(row), colWidth.get(col));
        }
    }

    private static class Interval {

        int from;
        int to;

        public Interval(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Interval interval = (Interval) o;

            if (from != interval.from) {
                return false;
            }
            return to == interval.to;

        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + to;
            return result;
        }

        @Override
        public String toString() {
            return "Interval{"
                    + "from=" + from
                    + ", to=" + to
                    + '}';
        }
    }

    private static class Bounds {

        List<Integer> columnSize = new ArrayList<>();
        List<Integer> rowSize = new ArrayList<>();
        Map<Interval, Integer> columnIntervalSize = new HashMap<Interval, Integer>();
        Map<Interval, Integer> rowIntervalSize = new HashMap<Interval, Integer>();

        Set<Pos> reserved = new HashSet<>();

        public void discardRow(int row) {
            reserved.remove(row);
        }

        public boolean isReserved(int col, int row) {
            return reserved.contains(new Pos(col, row));
        }

        public void addReservation(int col, int row) {
            reserved.add(new Pos(col, row));
        }

        public void setColumnMinSize(int index, int size) {
            while (columnSize.size() < index + 1) {
                columnSize.add(0);
            }
            columnSize.set(index, Math.max(size, columnSize.get(index)));
        }

        public void setRowMinSize(int index, int size) {
            while (rowSize.size() < index + 1) {
                rowSize.add(0);
            }
            rowSize.set(index, Math.max(size, rowSize.get(index)));
        }

        public void setColumnSize(int index, int size) {
            while (columnSize.size() < index + 1) {
                columnSize.add(0);
            }
            columnSize.set(index, size);
        }

        public void setRowSize(int index, int size) {
            while (rowSize.size() < index + 1) {
                rowSize.add(0);
            }
            columnSize.set(index, size);
        }

        public void setColumnIntervalSize(int from, int to, int size) {
            if (from + 1 == to) {
                setColumnSize(from, size);
                return;
            }
            columnIntervalSize.put(new Interval(from, to), size);
        }

        public void setRowIntervalSize(int from, int to, int size) {
            if (from + 1 == to) {
                setRowSize(from, size);
                return;
            }
            rowIntervalSize.put(new Interval(from, to), size);
        }

        public void setColumnIntervalMinSize(int from, int to, int size) {
            if (from + 1 == to) {
                setColumnMinSize(from, size);
                return;
            }
            Interval key = new Interval(from, to);

            columnIntervalSize.put(key, Math.max(
                    (columnIntervalSize.containsKey(key)?columnIntervalSize.get(key):0)
                    , size));
        }

        public void setRowIntervalMinSize(int from, int to, int size) {
            if (from + 1 == to) {
                setRowMinSize(from, size);
                return;
            }
            Interval key = new Interval(from, to);
            rowIntervalSize.put(key, Math.max(
                    (rowIntervalSize.containsKey(key)?rowIntervalSize.get(key):0)
                    , size));
        }

        public int evalColumnSize(int col, int colspan) {
            if (colspan <= 0) {
                return 0;
            }
            if (colspan == 1) {
                return columnSize.get(col);
            }

            int best = 0;
            for (Map.Entry<Interval, Integer> e : columnIntervalSize.entrySet()) {
                Interval interval = e.getKey();
                if (interval.from >= col && interval.to <= col + colspan) {
                    int v = evalColumnSize(col, interval.from - col) + e.getValue() + evalColumnSize(interval.to, col + colspan - interval.to);
                    if (v > best) {
                        best = v;
                    }
                }
            }
            int v = evalColumnSize(col, 1) + evalColumnSize(col + 1, colspan - 1);
            if (v > best) {
                best = v;
            }
            return best;
        }

        public int evalRowSize(int row, int rowspan) {
            if (rowspan <= 0) {
                return 0;
            }
            if (rowspan == 1) {
                return rowSize.get(row);
            }

            int best = 0;
            for (Map.Entry<Interval, Integer> e : rowIntervalSize.entrySet()) {
                Interval interval = e.getKey();
                if (interval.from >= row && interval.to <= row + rowspan) {
                    int v = evalRowSize(row, interval.from - row) + e.getValue() + evalRowSize(interval.to, row + rowspan - interval.to);
                    if (v > best) {
                        best = v;
                    }
                }
            }
            int v = evalRowSize(row, 1) + evalRowSize(row + 1, rowspan - 1);
            if (v > best) {
                best = v;
            }
            return best;
        }

    }

    @Override
    public boolean configure(NutsCommandLine cmdLine) {
        NutsArgument a;
        if ((a=cmdLine.readBooleanOption("--no-header"))!=null) {
            setVisibleHeader(!a.getBooleanValue());
            return true;
        } else if ((a=cmdLine.readBooleanOption("--header"))!=null) {
            setVisibleHeader(a.getBooleanValue());
            return true;
        } else if ((a=cmdLine.readStringOption("--border"))!=null) {
            switch (a.getValue().strKey()) {
                case "spaces": {
                    setBorder(SPACE_BORDER);
                    break;
                }
                case "simple": {
                    setBorder(SIMPLE_BORDER);
                    break;
                }
                case "fancy-rows": {
                    setBorder(FANCY_ROWS_BORDER);
                    break;
                }
                case "fancy-columns": {
                    setBorder(FANCY_COLUMNS_BORDER);
                    break;
                }
                case "none": {
                    setBorder(NO_BORDER);
                    break;
                }
            }
            setVisibleHeader(true);
            return true;
        } else if (cmdLine.hasNext() && cmdLine.get().isOption()) {
            for (Map.Entry<String, Integer> e : columns.entrySet()) {
                if (cmdLine.readAll("--" + e.getKey())) {
                    setVisibleColumn(e.getValue(), true);
                    return true;
                } else if (cmdLine.readAll("--no-" + e.getKey())) {
                    setVisibleColumn(e.getValue(), false);
                    return true;
                }
            }
        }
        return false;
    }


}
