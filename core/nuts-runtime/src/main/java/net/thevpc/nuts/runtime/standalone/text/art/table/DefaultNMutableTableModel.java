/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.text.*;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;

/**
 *
 * @author thevpc
 */
@NScore(fixed = NScorable.DEFAULT_SCORE)
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
    public NMutableTableModel addHeaderRow(NTableCell... values) {
        for (NTableCell value : values) {
            addHeaderCell(value);
        }
        return this;
    }

    @Override
    public NMutableTableModel addHeaderCell(NTableCell value) {
        DefaultCellDef c = new DefaultCellDef(true);
        c.copyFrom(value);
        header.cells.add(c);
        return this;
    }

    @Override
    public NMutableTableModel addRow(NTableCell... values) {
        newRow();
        addCells(values);
        return this;
    }

    @Override
    public NMutableTableModel addCells(NTableCell... values) {
        for (NTableCell value : values) {
            addCell(value);
        }
        return this;
    }

    @Override
    public NMutableTableModel addCell(NTableCell value) {
        if (rows.isEmpty()) {
            newRow();
        }
        DefaultCellDef c = new DefaultCellDef(false);
        c.copyFrom(value);
        rows.get(rows.size() - 1).cells.add(c);
        return this;
    }

    @Override
    public int columnsCount() {
        int c = header.cells.size();
        for (Row row : rows) {
            c = Math.max(c, row.cells.size());
        }
        return c;
    }

    @Override
    public NTableCellDef getCell(int row, int column) {
        return rows.get(row).cells.get(column);
    }

    @Override
    public NTableCellDef getHeader(int column) {
        return header.cells.get(column);
    }

    @Override
    public int getHeaderColSpan(int column) {
        return header.cells.get(column).colspan();
    }

    @Override
    public int rowsCount() {
        return rows.size();
    }

    @Override
    public NMutableTableModel setCell(int column, int row, NTableCell value) {
        DefaultCellDef c = (DefaultCellDef) rows.get(row).cells.get(column);
        c.copyNonDefaultFrom(value);
        return this;
    }

    @Override
    public NMutableTableModel setHeader(int column, NTableCell value) {
        DefaultCellDef c = (DefaultCellDef) header.cells.get(column);
        c.copyFrom(value);
        return this;
    }

    public static class Row {
        List<NTableCellDef> cells = new ArrayList<>();
    }

}
