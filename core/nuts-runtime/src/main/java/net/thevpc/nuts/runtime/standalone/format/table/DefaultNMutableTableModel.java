/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NMutableTableModel;
import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.format.NTableCell;
import net.thevpc.nuts.spi.NSupportLevelContext;

/**
 *
 * @author thevpc
 */
public class DefaultNMutableTableModel implements NMutableTableModel {

    private final Row header = new Row();
    private final List<Row> rows = new ArrayList<>();

    public DefaultNMutableTableModel() {
    }

    @Override
    public NMutableTableModel newRow() {
        rows.add(new Row());
        return this;
    }

    @Override
    public NMutableTableModel clearHeader() {
        header.cells.clear();
        return this;
    }

    @Override
    public NMutableTableModel addHeaderCells(Object... values) {
        for (Object value : values) {
            addHeaderCell(value);
        }
        return this;
    }

    @Override
    public NMutableTableModel addHeaderCell(Object value) {
        DefaultCell c = new DefaultCell();
        c.value = value;
        header.cells.add(c);
        return this;
    }

    @Override
    public NMutableTableModel addRow(Object... values) {
        newRow();
        addCells(values);
        return this;
    }

    @Override
    public NMutableTableModel addCells(Object... values) {
        for (Object value : values) {
            addCell(value);
        }
        return this;
    }

    @Override
    public NMutableTableModel addCell(Object value) {
        if (rows.isEmpty()) {
            newRow();
        }
        DefaultCell c = new DefaultCell();
        c.value = value;
        rows.get(rows.size() - 1).cells.add(c);
        return this;
    }

    @Override
    public int getColumnsCount() {
        int c = header.cells.size();
        for (Row row : rows) {
            c = Math.max(c, row.cells.size());
        }
        return c;
    }

    @Override
    public Object getCellValue(int row, int column) {
        return rows.get(row).cells.get(column).value;
    }

    @Override
    public int getCellColSpan(int row, int column) {
        return rows.get(row).cells.get(column).colspan;
    }

    @Override
    public int getCellRowSpan(int row, int column) {
        return rows.get(row).cells.get(column).rowspan;
    }

    @Override
    public Object getHeaderValue(int column) {
        return header.cells.get(column).value;
    }

    @Override
    public int getHeaderColSpan(int column) {
        return header.cells.get(column).colspan;
    }

    @Override
    public int getRowsCount() {
        return rows.size();
    }

    @Override
    public NMutableTableModel setCellValue(int row, int column, Object value) {
        rows.get(row).cells.get(column).value = value;
        return this;
    }

    @Override
    public NMutableTableModel setCellColSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).colspan = value;
        return this;
    }

    @Override
    public NMutableTableModel setCellRowSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).rowspan = value;
        return this;
    }

    @Override
    public NMutableTableModel setHeaderValue(int column, Object value) {
        header.cells.get(column).value = value;
        return this;
    }

    @Override
    public NMutableTableModel setHeaderColSpan(int column, int value) {
        header.cells.get(column).colspan = value;
        return this;
    }

    public static class Row {

        List<DefaultCell> cells = new ArrayList<>();
    }

    public static class DefaultCell implements NTableCell {

        int colspan = 1;
        int rowspan = 1;
        int x;
        int y;

        Object value;

        @Override
        public int getColspan() {
            return colspan;
        }

        @Override
        public DefaultCell setColspan(int colspan) {
            this.colspan = colspan <= 0 ? 1 : colspan;
            return this;
        }

        @Override
        public int getRowspan() {
            return rowspan;
        }

        @Override
        public DefaultCell setRowspan(int rowspan) {
            this.rowspan = rowspan <= 0 ? 1 : rowspan;
            return this;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public DefaultCell setValue(Object value) {
            this.value = value;
            return this;
        }

        @Override
        public String toString() {
            return "Cell{"
                    + "" + x + "->" + (x + colspan)
                    + ", " + y + "->" + (y + rowspan)
                    + ", " + value
                    + '}';
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
