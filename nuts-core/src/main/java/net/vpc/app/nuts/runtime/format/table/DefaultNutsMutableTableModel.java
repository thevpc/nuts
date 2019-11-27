/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.table;

import net.vpc.app.nuts.NutsMutableTableModel;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsTableCell;

/**
 *
 * @author vpc
 */
public class DefaultNutsMutableTableModel implements NutsMutableTableModel {

    private final Row header = new Row();
    private final List<Row> rows = new ArrayList<>();

    @Override
    public NutsMutableTableModel newRow() {
        rows.add(new Row());
        return this;
    }

    @Override
    public NutsMutableTableModel clearHeader() {
        header.cells.clear();
        return this;
    }

    @Override
    public NutsMutableTableModel addHeaderCells(Object... values) {
        for (Object value : values) {
            addHeaderCell(value);
        }
        return this;
    }

    @Override
    public NutsMutableTableModel addHeaderCell(Object value) {
        DefaultCell c = new DefaultCell();
        c.value = value;
        header.cells.add(c);
        return this;
    }

    @Override
    public NutsMutableTableModel addRow(Object... values) {
        newRow();
        addCells(values);
        return this;
    }

    @Override
    public NutsMutableTableModel addCells(Object... values) {
        for (Object value : values) {
            addCell(value);
        }
        return this;
    }

    @Override
    public NutsMutableTableModel addCell(Object value) {
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
    public NutsMutableTableModel setCellValue(int row, int column, Object value) {
        rows.get(row).cells.get(column).value = value;
        return this;
    }

    @Override
    public NutsMutableTableModel setCellColSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).colspan = value;
        return this;
    }

    @Override
    public NutsMutableTableModel setCellRowSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).rowspan = value;
        return this;
    }

    @Override
    public NutsMutableTableModel setHeaderValue(int column, Object value) {
        header.cells.get(column).value = value;
        return this;
    }

    @Override
    public NutsMutableTableModel setHeaderColSpan(int column, int value) {
        header.cells.get(column).colspan = value;
        return this;
    }

    public static class Row {

        List<DefaultCell> cells = new ArrayList<>();
    }

    public static class DefaultCell implements NutsTableCell {

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

}
