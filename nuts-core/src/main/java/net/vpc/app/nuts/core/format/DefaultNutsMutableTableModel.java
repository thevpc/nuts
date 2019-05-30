/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

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
    public void newRow() {
        rows.add(new Row());
    }

    @Override
    public void clearHeader() {
        header.cells.clear();
    }
    
    @Override
    public void addHeaderCells(Object... values) {
        for (Object value : values) {
            addHeaderCell(value);
        }
    }

    @Override
    public void addHeaderCell(Object value) {
        DefaultCell c = new DefaultCell();
        c.value = value;
        header.cells.add(c);
    }

    @Override
    public void addRow(Object... values) {
        newRow();
        addCells(values);
    }

    @Override
    public void addCells(Object... values) {
        for (Object value : values) {
            addCell(value);
        }
    }

    @Override
    public void addCell(Object value) {
        if (rows.size() == 0) {
            newRow();
        }
        DefaultCell c = new DefaultCell();
        c.value = value;
        rows.get(rows.size() - 1).cells.add(c);
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
    public void setCellValue(int row, int column, Object value) {
        rows.get(row).cells.get(column).value = value;
    }

    @Override
    public void setCellColSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).colspan = value;
    }

    @Override
    public void setCellRowSpan(int row, int column, int value) {
        rows.get(row).cells.get(column).rowspan = value;
    }

    @Override
    public void setHeaderValue(int column, Object value) {
        header.cells.get(column).value = value;
    }

    @Override
    public void setHeaderColSpan(int column, int value) {
        header.cells.get(column).colspan = value;
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
