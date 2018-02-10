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
package net.vpc.app.nuts.extensions.util;

import java.util.*;

/**
 * Created by vpc on 2/17/17.
 */
public class TableFormatter {

    List<Row> rows = new ArrayList<>();

//    public static void main(String[] args) {
//        TableFormatter t = new TableFormatter();
//        t.addCell("AA AA").setColspan(2).setRowspan(2);
//        t.addCell("B B");
//        t.newRow();
//        t.addCell("C");
//        t.addCell("D");
//        t.rebuild();
//
//
//        for (Row row : t.rows) {
//            for (Cell cell : row.cells) {
//                System.out.print(cell.rendered);
//            }
//            System.out.println();
//        }
//
////        RenderedCell r1 = new RenderedCell("AA\nAA");
////        RenderedCell r2 = new RenderedCell("C\nB");
////        RenderedCell resized2 = r1.replaceContent(r2, 1, 1);
////        System.out.println();
//    }
    private static class Row {

        CellFormatter formatter;
        List<Cell> cells = new ArrayList<>();
    }

    private static String repeat(char c, int count) {
        char[] a = new char[count];
        Arrays.fill(a, 0, count, c);
        return new String(a);
    }

    private static String repeatV(char c, int count) {
        char[] a = new char[2 * count - 1];
        for (int i = 0; i < count; i += 2) {
            a[i] = c;
            if (i + 1 < a.length) {
                a[i + 1] = '\n';
            }
        }
        return new String(a);
    }

    private static class RenderedCell {

        char[][] rendered;
        int rows;
        int columns;

        private RenderedCell() {

        }

        public RenderedCell(String str) {
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
                    columns = Math.max(columns, last.length());
                }
            }
            rows = strings.size();
            rendered = new char[rows][];
            for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                StringBuilder s = strings.get(i);
                while (s.length() < columns) {
                    s.append(' ');
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
                    rendered0[i] = new char[columns];
                    int min = Math.min(columns, rendered[i].length);
                    System.arraycopy(rendered[i], 0, rendered0[i], 0, min);
                    if (min < columns) {
                        Arrays.fill(rendered0[i], min, columns, ' ');
                    }
                } else {
                    rendered0[i] = new char[columns];
                    Arrays.fill(rendered0[i], 0, columns, ' ');
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

    private static class Cell {

        int colspan = 1;
        int rowspan = 1;
        int x;
        int y;
        int cx;
        int cy;
        int cw;
        int ch;

        Object value;
        CellFormatter formatter;
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

        public Cell setColspan(int colspan) {
            this.colspan = colspan <= 0 ? 1 : colspan;
            return this;
        }

        public int getRowspan() {
            return rowspan;
        }

        public Cell setRowspan(int rowspan) {
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

        public Cell setValue(Object value) {
            this.value = value;
            return this;
        }

        public CellFormatter getFormatter() {
            return formatter;
        }

        public Cell setFormatter(CellFormatter formatter) {
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

    private void rebuild() {
        class Widths {

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
        class Interval {

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
        class Bounds {

            List<Integer> columnSize = new ArrayList<>();
            List<Integer> rowSize = new ArrayList<>();
            Map<Interval, Integer> columnIntervalSize = new HashMap<Interval, Integer>();
            Map<Interval, Integer> rowIntervalSize = new HashMap<Interval, Integer>();

            Map<Integer, Set<Integer>> reservedColumnsByRow = new HashMap<>();

            public void discardRow(int row) {
                reservedColumnsByRow.remove(row);
            }

            public boolean isReserved(int row, int col) {
                Set<Integer> r = reservedColumnsByRow.get(row);
                if (r == null) {
                    return false;
                }
                return r.contains(col);
            }

            public void addReservation(int row, int col) {
                Set<Integer> r = reservedColumnsByRow.get(row);
                if (r == null) {
                    r = new HashSet<>();
                    reservedColumnsByRow.put(row, r);
                }
                r.add(col);
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

                columnIntervalSize.put(key, Math.max(columnIntervalSize.getOrDefault(key, 0), size));
            }

            public void setRowIntervalMinSize(int from, int to, int size) {
                if (from + 1 == to) {
                    setRowMinSize(from, size);
                    return;
                }
                Interval key = new Interval(from, to);
                rowIntervalSize.put(key, Math.max(rowIntervalSize.getOrDefault(key, 0), size));
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
        //first pass to eval renderedText and effective positions
        Bounds b = new Bounds();
        Bounds cb = new Bounds();
        int r = 0;
        int cr = 0;
        for (Row row : rows) {
            int c = 0;
            int cc = 0;
            for (Cell cell : row.cells) {
                int r0 = r;
                int c0 = c;
                int cr0 = cr;
                int cc0 = cc;
//                while(b.isReserved(r0,c0)){
//                    r0++;
//                }
                while (b.isReserved(r0, c0)) {
                    c0++;
                }
                while (cb.isReserved(cr0, cc0)) {
                    cc0++;
                }
                cell.cx = cc0;
                cell.cy = cr0;
                cell.x = c0;
                cell.y = r0;
                CellFormatter formatter = cell.formatter;
                if (formatter == null) {
                    formatter = row.formatter;
                }
                if (formatter == null) {
                    formatter = new CellFormatter() {
                        @Override
                        public String format(int row, int col, Object value) {
                            return String.valueOf(value);
                        }
                    };
                }
                cell.setRendered(new RenderedCell(
                        formatter.format(r0, c0, cell.getValue())
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
        for (Row row : rows) {
            for (Cell cell : row.cells) {
                int rows = b.evalRowSize(cell.y, cell.rowspan);
                int columns = b.evalColumnSize(cell.x, cell.rowspan);
                cell.rendered = cell.rendered.resize(rows, columns);
                cell.cw = cell.getRendered().columns;
                cell.ch = cell.getRendered().rows;
            }
        }

    }

    private interface CellFormatter {

        String format(int row, int col, Object value);
    }

    public void newRow() {
        rows.add(new Row());
    }

    public Cell addCell(Object value) {
        if (rows.size() == 0) {
            newRow();
        }
        Cell c = new Cell();
        c.value = value;
        rows.get(rows.size() - 1).cells.add(c);
        return c;
    }
}
