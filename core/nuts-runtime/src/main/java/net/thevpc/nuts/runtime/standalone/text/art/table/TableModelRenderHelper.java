package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableCellFormat;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableHeaderFormat;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegionImpl;
import net.thevpc.nuts.text.*;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

class TableModelRenderHelper {
    TableRow[] tableRows;
    TableColumn[] tableColumns;

    private NTableModel model;

    List<Boolean> visibleColumns;
    boolean visibleHeader;
    int rows;
    int cols;
    private static final int NO_BORDER_TOP = 1;
    private static final int NO_BORDER_LEFT = 2;
    NTextRegion[][] grid;
    DefaultCell[][] gridc;
    int[][] noBorder;
    NTableBordersFormatHelper borderHelper;

    public TableModelRenderHelper(NTableModel model) {
        this.model = model;
    }

    private NTableCellFormat defaultCellFormatter = DefaultTableCellFormat.INSTANCE;
    private NTableCellFormat defaultHeaderFormatter = DefaultTableHeaderFormat.INSTANCE;

    public void prepare(List<Boolean> visibleColumns, boolean visibleHeader, NTableCellFormat defaultCellFormatter, NTableCellFormat defaultHeaderFormatter) {
        this.visibleHeader = visibleHeader;
        this.visibleColumns = visibleColumns;
        this.defaultCellFormatter = defaultCellFormatter;
        this.defaultHeaderFormatter = defaultHeaderFormatter;
        rebuild(model);
        if (tableRows.length == 0) return;
        computeColumns();
        for (TableRow row : tableRows) {
            for (DefaultCell cell : row.cells) {
                cell.renderedContent = cell.renderedContent.resize(
                        charWidth(cell.x,cell.x+cell.colspan), charHeight(cell.y,cell.y+cell.rowspan),
                        cell.hAlign, cell.vAlign);
            }
        }
        computeStartRowIndex();
    }

    public int charWidth(int fromColumn,int toColumnExcluded){
        int w=0;
        for (int i = fromColumn; i <toColumnExcluded; i++) {
            w+=tableColumns[i].charWidth;
        }
        return w;
    }

    public int charHeight(int fromRow,int toRowExcluded){
        int h=0;
        for (int i = fromRow; i <toRowExcluded; i++) {
            h+=tableRows[i].charHeight;
        }
        return h;
    }

    public boolean isEmpty() {
        return tableRows.length == 0;
    }

    private void rebuild(NTableModel model) {
        List<TableRow> rows1 = new ArrayList<>();
        int columnsCount = model.getColumnsCount();
        int rowsCount = model.getRowsCount();
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            TableRow r = new TableRow();
            r.index = rowIndex;
            rows1.add(r);
            for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
                DefaultCell c = new DefaultCell(false);
                try {
                    c.content = model.getCellValue(rowIndex, columnIndex);
                    c.colspan = model.getCellColSpan(rowIndex, columnIndex);
                    c.rowspan = model.getCellRowSpan(rowIndex, columnIndex);
                    r.cells.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        List<TableRow> effectiveRows = new ArrayList<>();
        TableRow header = new TableRow();
        try {
            for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
                DefaultCell c = new DefaultCell(true);
                try {
                    c.content = model.getHeaderValue(columnIndex);
                    header.cells.add(c);
                    c.colspan = model.getHeaderColSpan(columnIndex);
                    //header has no rowspan
                    //c.rowspan = model.getHeaderRowSpan(columnIndex);
                } catch (Exception ex) {
                    //ignore
                }
            }
        } catch (NoSuchElementException ex) {
            //ignore
        }
        if (header.cells.size() > 0 && visibleHeader) {
            effectiveRows.add(header);
        }
        boolean p = isVisibleColumnPositive();
        boolean n = isVisibleColumnNegative();
        for (int i = 0; i < rows1.size(); i++) {
            TableRow row = rows1.get(i);
            TableRow r2 = new TableRow();
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
        //ensure rows!
        {
            TableRow[] effectiveRows0 = effectiveRows.toArray(new TableRow[0]);
            for (int j = 0; j < effectiveRows0.length; j++) {
                TableRow tableRow = effectiveRows0[j];
                //recompute index after visibility applied
                tableRow.index=j;
                for (DefaultCell cell : tableRow.cells) {
                    if (cell.rowspan > 1) {
                        for (int i = 1; i < cell.rowspan; i++) {
                            if (j + i >= effectiveRows.size()) {
                                effectiveRows.add(new TableRow());
                            }
                        }
                    }
                }
            }
        }

        //first pass to eval renderedText and effective positions
        Bounds b = new Bounds();
        Bounds cb = new Bounds();
        int r = 0;
        int cr = 0;
        for (TableRow row : effectiveRows) {
            row.index=r;
            int c = 0;
            int cc = 0;
            b.setRowIntervalMinSize(r, r, 0);
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
                cell.x = c0;
                cell.y = r0;
//                NAssert.requireTrue(cell.y == r, cell.y +"(cell.y) == "+r+"(r)");
                NTableCellFormat formatter = getTableCellFormat(cell);
                NText cvalue = cell.getContent();
                cell.vAlign = formatter.getVerticalAlign(r0, c0, cvalue);
                cell.hAlign = formatter.getHorizontalAlign(r0, c0, cvalue);
                cell.setRenderedContent(new NTextRegionImpl(
                        formatter.format(r0, c0, cvalue)
                ));
                b.setColumnIntervalMinSize(cell.x, cell.x + cell.colspan, cell.getRenderedContent().columns());
                b.setRowIntervalMinSize(cell.y, cell.y + cell.rowspan, cell.getRenderedContent().rows());
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
        for (int k = 0; k < effectiveRows.size(); k++) {
            TableRow row = effectiveRows.get(k);
//            row.index=k;
            List<DefaultCell> cells = row.cells;
            for (int j = 0; j < cells.size(); j++) {
                DefaultCell cell = cells.get(j);
                int e = (int) Math.ceil(cell.renderedContent.rows() * 1.0 / Math.max(1,cell.rowspan));
                //int e = cell.rendered.rows();
                for (int i = 0; i < cell.rowspan; i++) {
                    TableRow rr = effectiveRows.get(k + i);
                    rr.charHeight = Math.max(rr.charHeight, e);
                }
            }
        }
        tableRows = effectiveRows.toArray(new TableRow[0]);
    }

    private NTableCellFormat getTableCellFormat(DefaultCell dc) {
        return dc.isHeader() ? defaultHeaderFormatter : defaultCellFormatter;
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
            } else return !p || !n;
        }
        return true;
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


    private void computeColumns() {
        int totalTableColumns = 0;
        for (TableRow row : tableRows) {
            for (DefaultCell cell : row.cells) {
                totalTableColumns = Math.max(totalTableColumns, cell.x+Math.max(1, cell.colspan));
            }
        }
        tableColumns = new TableColumn[totalTableColumns];
        for (int i = 0; i < totalTableColumns; i++) {
            tableColumns[i] = new TableColumn();
            tableColumns[i].index = i;
        }
        for (TableRow row : tableRows) {
            for (DefaultCell cell : row.cells) {
                int e = (int) Math.ceil(cell.renderedContent.columns() * 1.0 / Math.max(1, cell.colspan));
                for (int i = 0; i < cell.colspan; i++) {
                    tableColumns[cell.x+i].charWidth = Math.max(tableColumns[cell.x+i].charWidth, e);
                }
            }
        }
    }

    private void computeStartRowIndex() {
        for (int r = 0; r < tableRows.length; r++) {
            TableRow row = tableRows[r];
            row.index = r;
        }
    }

    public void dump() {
        dump(System.out, "");
    }

    public void dump(PrintStream out, String prefix) {
        out.println(prefix + "tableColumns x tableRows = " + tableColumns.length + " x " + tableRows.length);
        out.println(prefix + "tableColumns  = ");
        for (TableColumn td : tableColumns) {
            td.dump(out, "   ");
        }
        out.println(prefix + "tableRows  = ");
        for (TableRow tr : tableRows) {
            tr.dump(out, "   ");
        }
        out.println(prefix + "tableCells  = ");
        for (DefaultCell cell : allCells()) {
            cell.dump(out, "   ");
        }
    }

    public DefaultCell[] allCells() {
        List<DefaultCell> list = new ArrayList<>();
        for (TableRow tr : tableRows) {
            for (DefaultCell cell : tr.cells) {
                list.add(cell);
            }
        }
        return list.toArray(new DefaultCell[0]);
    }


    /// /////////////


    public NTextRegion renderTable(NTableBordersFormat border) {
            this.borderHelper = new NTableBordersFormatHelper(border);
            rows = tableRows.length;
            cols = tableColumns.length;
            grid = new NTextRegion[rows][cols];
            gridc = new DefaultCell[rows][cols];
            noBorder = new int[rows][cols];

        // Step 1: Prepare cell regions
        for (DefaultCell cell : allCells()) {
            int w = 0;
            for (int i = 0; i < cell.colspan; i++) {
                int xx = cell.x + i;
                w += tableColumns[xx].charWidth;
                if (i > 0) {
                    noBorder[cell.y][xx] |= NO_BORDER_LEFT;
                }
            }
            int h = 0;
            for (int i = 0; i < cell.rowspan; i++) {
                int yy = cell.y + i;
                h += tableRows[yy].charHeight;
                if (i > 0) {
                    noBorder[yy][cell.x] |= NO_BORDER_TOP;
                }
            }

            NTextRegion contentRegion = cell.renderedContent;
            contentRegion = contentRegion.resize(Math.max(w, contentRegion.columns()), Math.max(h, contentRegion.rows()), cell.hAlign, cell.vAlign);
            grid[cell.y][cell.x] = contentRegion;
            gridc[cell.y][cell.x] = cell;
        }

        // process overflow
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                NTextRegion contentRegion = grid[r][c];
                if (contentRegion != null) {
                    if (contentRegion.rows() > tableRows[r].charHeight) {
                        if (r + 1 < grid.length) {
                            NTextRegion sr1 = contentRegion.subRegion(0, 0, contentRegion.columns(), tableRows[r].charHeight);
                            NTextRegion sr2 = contentRegion.subRegion(0, tableRows[r].charHeight, contentRegion.columns(), contentRegion.rows());
                            NTextRegion contentRegion2 = grid[r + 1][c];
                            if (contentRegion2 == null) {
                                grid[r + 1][c] = sr2;
                            } else {
                                grid[r + 1][c] = sr2.concatVertically(contentRegion2);
                            }
                            grid[r][c] = sr1;
                        }
                    }
                    if (contentRegion.columns() > tableColumns[c].charWidth) {
                        if (c + 1 < grid[r].length) {
                            NTextRegion sr1 = contentRegion.subRegion(0, 0, tableColumns[c].charWidth, contentRegion.rows());
                            NTextRegion sr2 = contentRegion.subRegion(tableColumns[c].charWidth, 0, contentRegion.columns(), contentRegion.rows());
                            NTextRegion contentRegion2 = grid[r][c + 1];
                            if (contentRegion2 == null) {
                                grid[r][c + 1] = sr2;
                            } else {
                                grid[r][c + 1] = sr2.concatHorizontally(contentRegion2);
                            }
                            grid[r][c] = sr1;
                        }
                    }
                } else {
                    grid[r][c] = NTextRegion.ofWhitespace(tableColumns[c].charWidth, tableRows[r].charHeight);
                }
            }
        }

        // add borders
        NTextRegion[][] gridWithBorder = new NTextRegion[rows][cols];
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                NTextRegion contentRegion = applyBorder(c, r);
//                NOut.println("" + c + ";" + r + " :: top=" + isTopBorder(c, r) + " left:" + isLeftBorder(c, r));
//                NOut.println(contentRegion.toText());
                gridWithBorder[r][c] = contentRegion;
            }
        }

        // Build the table row by row
        NTextRegion table = null;
        for (int r = 0; r < rows; r++) {
            NTextRegion rowRegion = null;
            int c = 0;
            while (c < cols) {
                if (rowRegion == null) {
                    rowRegion = gridWithBorder[r][c];
                } else {
                    rowRegion = rowRegion.concatHorizontally(gridWithBorder[r][c]);
                }
                c++;
            }
            if (table == null) {
                table = rowRegion;
            } else {
                table = table.concatVertically(rowRegion);
            }
        }

        return table.ensureColumns();
    }

    private NTextRegion applyBorder(int c, int r) {
        NTextRegion contentRegion = grid[r][c];
        Borders b = createBorders(c, r);
        NTextRegion rc = null;
        if (!isTopBorder(c, r) && !isLeftBorder(c, r)) {
            //expand vertically and horizontally to make room instead of border
            contentRegion = contentRegion.concatVertically(NTextRegion.ofWhitespace(contentRegion.columns(), b.startHorizontalBorderSize));
            contentRegion = contentRegion.concatHorizontally(NTextRegion.ofWhitespace(b.startVerticalBorderSize, contentRegion.rows()));
            if (b.middleRight != null) {
                contentRegion = contentRegion.concatHorizontally(b.middleRight);
            }
            if (b.bottomCenter != null) {
                if (b.bottomRight != null) {
                    if (rc == null) {
                        rc = b.bottomRight;
                    } else {
                        rc = rc.concatHorizontally(b.bottomRight);
                    }
                }
                contentRegion = contentRegion.concatVertically(rc);
            }
        } else if (!isTopBorder(c, r)) {
            contentRegion = contentRegion.concatVertically(NTextRegion.ofWhitespace(contentRegion.columns(), b.startHorizontalBorderSize));
            if (b.middleLeft != null) {
                contentRegion = b.middleLeft.concatHorizontally(contentRegion);
            }
            if (b.middleRight != null) {
                contentRegion = contentRegion.concatHorizontally(b.middleRight);
            }
            if (rc != null) {
                contentRegion = rc.concatVertically(contentRegion);
            }
            if (b.bottomCenter != null) {
                if (b.bottomLeft != null) {
                    rc = b.bottomLeft.concatHorizontally(b.bottomCenter);
                }
                if (b.bottomRight != null) {
                    if (rc == null) {
                        rc = b.bottomRight;
                    } else {
                        rc = rc.concatHorizontally(b.bottomRight);
                    }
                }
                contentRegion = contentRegion.concatVertically(rc);
            }
        } else if (!isLeftBorder(c, r)) {
            contentRegion = contentRegion.concatHorizontally(NTextRegion.ofWhitespace(b.startVerticalBorderSize, contentRegion.rows()));
            if (b.topCenter != null) {
                if (rc == null) {
                    rc = b.topCenter;
                } else {
                    rc = rc.concatHorizontally(b.topCenter);
                }
                if (b.topRight != null) {
                    rc = rc.concatHorizontally(b.topRight);
                }
            }
            if (b.middleRight != null) {
                contentRegion = contentRegion.concatHorizontally(b.middleRight);
            }
            if (rc != null) {
                contentRegion = rc.concatVertically(contentRegion);
            }
            if (b.bottomCenter != null) {
                if (b.bottomLeft != null) {
                    rc = b.bottomLeft.concatHorizontally(b.bottomCenter);
                }
                if (b.bottomRight != null) {
                    if (rc == null) {
                        rc = b.bottomRight;
                    } else {
                        rc = rc.concatHorizontally(b.bottomRight);
                    }
                }
                contentRegion = contentRegion.concatVertically(rc);
            }
        } else {
            if (b.topCenter != null) {
                if (b.topLeft != null) {
                    rc = b.topLeft.concatHorizontally(b.topCenter);
                }
                if (b.topRight != null) {
                    if (rc == null) {
                        rc = b.topRight;
                    } else {
                        rc = rc.concatHorizontally(b.topRight);
                    }
                }
            }
            if (b.middleLeft != null) {
                contentRegion = b.middleLeft.concatHorizontally(contentRegion);
            }
            if (b.middleRight != null) {
                contentRegion = contentRegion.concatHorizontally(b.middleRight);
            }
            if (rc != null) {
                contentRegion = rc.concatVertically(contentRegion);
            }
            if (b.bottomCenter != null) {
                if (b.bottomLeft != null) {
                    rc = b.bottomLeft.concatHorizontally(b.bottomCenter);
                }
                if (b.bottomRight != null) {
                    if (rc == null) {
                        rc = b.bottomRight;
                    } else {
                        rc = rc.concatHorizontally(b.bottomRight);
                    }
                }
                contentRegion = contentRegion.concatVertically(rc);
            }
        }
        return contentRegion;
    }

    private Borders createBorders(int c, int r) {
        Borders b = new Borders();
        b.startVerticalBorderSize = borderHelper.getStartVerticalBorderSize();
        b.startHorizontalBorderSize = borderHelper.getStartHorizontalBorderSize();
        int cColumns = tableColumns[c].charWidth;
        int cRows = tableRows[r].charHeight;
        BorderInfos bi = new BorderInfos();
        bi.lastRow = r == rows - 1;
        bi.lastColumn = c == cols - 1;
        bi.firstRow = r == 0;
        bi.firstColumn = c == 0;
        bi.previousColumnHasTopBorder = c > 0 && isTopBorder(c - 1, r);
        bi.previousRowHasLeftBorder = r > 0 && isLeftBorder(c, r - 1);
        bi.previousRowIsSpanned = r > 0 && gridc[r - 1][c] == null/*colspan*/;
        bi.hasTopBorder = isTopBorder(c, r);
        bi.hasLeftBorder = isLeftBorder(c, r);

        if (bi.hasTopBorder) {
            b.topCenter = borderHelper.line(NTableSeparator.FIRST_ROW_LINE, cColumns + (bi.hasLeftBorder ? 0 : b.startVerticalBorderSize));
        }
        if (bi.hasLeftBorder) {
            b.middleLeft = borderHelper.column(NTableSeparator.ROW_START, cRows + (bi.hasTopBorder ? 0 : b.startHorizontalBorderSize));
        }

        if (bi.lastColumn) {
            b.middleRight = borderHelper.column(NTableSeparator.ROW_END, cRows);
        }
        if (bi.lastRow) {
            b.bottomCenter = borderHelper.line(NTableSeparator.LAST_ROW_LINE, cColumns);
        }
        b.topLeft = createTopLeftBorderCorner(bi);
        b.bottomLeft = createBottomLeftBorderCorner(bi);
        b.topRight = createTopRightBorderCorner(bi);
        b.bottomRight = createBottomRightBorderCorner(bi);
        return b;
    }

    private NTextRegion createBottomRightBorderCorner(BorderInfos bi) {
        if (bi.lastRow && bi.lastColumn) {
            return borderHelper.get(NTableSeparator.LAST_ROW_END);
        }
        return null;
    }

    private NTextRegion createTopRightBorderCorner(BorderInfos bi) {
        if (bi.hasTopBorder && bi.lastColumn) {
            if (bi.firstRow) {
                return borderHelper.get(NTableSeparator.FIRST_ROW_END);
            } else{
                return borderHelper.get(NTableSeparator.MIDDLE_ROW_END);
            }
        }
        return null;
    }

    private NTextRegion createBottomLeftBorderCorner(BorderInfos bi) {
        if (bi.lastRow && bi.hasLeftBorder) {
            if (bi.firstColumn) {
                return borderHelper.get(NTableSeparator.LAST_ROW_START);
            } else {
                return borderHelper.get(NTableSeparator.LAST_ROW_SEP);
            }
        }
        return null;
    }

    private NTextRegion createTopLeftBorderCorner(BorderInfos bi) {
        if (bi.hasTopBorder && bi.hasLeftBorder) {
            if (bi.firstRow && bi.firstColumn) {
                return borderHelper.get(NTableSeparator.FIRST_ROW_START);
            } else if (bi.firstColumn) {
                if (!bi.previousRowHasLeftBorder) {
                    return borderHelper.get(NTableSeparator.FIRST_ROW_SEP);
                } else {
                    return borderHelper.get(NTableSeparator.MIDDLE_ROW_START);
                }
            } else if (bi.firstRow) {
                return borderHelper.get(NTableSeparator.FIRST_ROW_SEP);
            } else {
                if (!bi.previousColumnHasTopBorder) {
                    return borderHelper.get(NTableSeparator.MIDDLE_ROW_START);
                } else if (bi.previousRowIsSpanned) {
                    return borderHelper.get(NTableSeparator.FIRST_ROW_SEP);
                } else {
                    return borderHelper.get(NTableSeparator.MIDDLE_ROW_SEP);
                }
            }
        }
        return null;
    }

    private boolean isLeftBorder(int c, int r) {
        return (noBorder[r][c] & NO_BORDER_LEFT) == 0;
    }

    private boolean isTopBorder(int c, int r) {
        return (noBorder[r][c] & NO_BORDER_TOP) == 0;
    }

}
