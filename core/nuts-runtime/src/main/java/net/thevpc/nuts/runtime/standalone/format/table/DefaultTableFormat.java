/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementEntry;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringBuilder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 2/17/17.
 */
public class DefaultTableFormat extends DefaultFormatBase<NTableFormat> implements NTableFormat {

    public static NTableBordersFormat NO_BORDER = new DefaultTableFormatBorders(
            "", "", "", "",
            "", "", "",
            "", "", "", "",
            "", "", "", ""
    );

    public static NTableBordersFormat UNICODE_BORDER = new DefaultTableFormatBorders(
            "╭", "─", "┬", "╮",
            "│", "│", "│",
            "├", "─", "┼", "┤",
            "╰", "─", "┴", "╯"
    );

    public static NTableBordersFormat SIMPLE_BORDER = new DefaultTableFormatBorders(
            "-", "-", "-", "-",
            "|", " | ", "|",
            "|", "-", "+", "|",
            "-", "-", "-", "-"
    );
    public static NTableBordersFormat FANCY_ROWS_BORDER = new DefaultTableFormatBorders(
            "", "", "", "",
            "", " ", "",
            "", "─", "─", "",
            "", "", "", ""
    );
    public static NTableBordersFormat SPACE_BORDER = new DefaultTableFormatBorders(
            "", "", "", "",
            "", " ", "",
            "", "", "", "",
            "", "", "", ""
    );
    public static NTableBordersFormat FANCY_COLUMNS_BORDER = new DefaultTableFormatBorders(
            "", "", "", "",
            "", " │ ", "",
            "", "", "", "",
            "", "", "", ""
    );
    private NTableCellFormat defaultCellFormatter = DefaultTableCellFormat.INSTANCE;
    private NTableCellFormat defaultHeaderFormatter = DefaultTableHeaderFormat.INSTANCE;
    /**
     * <pre>
     * ABBBBCBBBBD
     * E    F    G
     * HIIIIJIIIIK
     * E    F    G
     * LMMMMNMMMMO
     * </pre>
     */
    private NTableBordersFormat border = CorePlatformUtils.SUPPORTS_UTF_ENCODING ? UNICODE_BORDER : SIMPLE_BORDER;
    private Object model;
    private final List<Boolean> visibleColumns = new ArrayList<>();
    private boolean visibleHeader = true;

    public DefaultTableFormat(NSession session) {
        super(session, "table-format");
    }

    public static Set<String> getAvailableTableBorders() {
        return new HashSet<>(Arrays.asList(
                "default",
                "spaces",
                "simple",
                "columns",
                "rows",
                "none"
        )
        );
    }

    public static NTableBordersFormat parseTableBorders(String borderName) {
        switch (borderName) {
            case "spaces": {
                return (SPACE_BORDER);
            }
            case "default": {
                return CorePlatformUtils.SUPPORTS_UTF_ENCODING ? UNICODE_BORDER : SIMPLE_BORDER;
            }
            case "unicode": {
                return (UNICODE_BORDER);
            }
            case "simple": {
                return (SIMPLE_BORDER);
            }
            case "rows": {
                return (FANCY_ROWS_BORDER);
            }
            case "columns": {
                return (FANCY_COLUMNS_BORDER);
            }
            case "none": {
                return (NO_BORDER);
            }
        }
        return null;
    }

    public static void formatAndHorizontalAlign(StringBuilder sb, NPositionType a, int columns, NTexts tf, NSession session) {
        int length = tf.parse(sb.toString()).textLength();
        switch (a) {
            case FIRST: {
//                if (sb.length() > length) {
//                    sb.delete(length, sb.length());
//                }
                while (length < columns) {
                    sb.append(' ');
                    length++;
                }
                break;
            }
            case LAST: {
//                if (sb.length() > length) {
//                    sb.delete(length, sb.length());
//                }
                while (length < columns) {
                    sb.insert(0, ' ');
                    length++;
                }
                break;
            }
            case CENTER: {
//                if (sb.length() > length) {
//                    sb.delete(length, sb.length());
//                }
                boolean after = true;
                while (length < columns) {
                    if (after) {
                        sb.append(' ');
                    } else {
                        sb.insert(0, ' ');
                    }
                    after = !after;
                    length++;
                }
                break;
            }
            case HEADER: {
                boolean after = true;
                int maxBefore = 10;
                while (length < columns) {
                    if (after || maxBefore <= 0) {
                        sb.append(' ');
                    } else {
                        sb.insert(0, ' ');
                        maxBefore--;
                    }
                    after = !after;
                    length++;
                }
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(session, NMsg.ofC("unsupported position type %s", a));
            }
        }
    }

    @Override
    public boolean isVisibleHeader() {
        return visibleHeader;
    }

    @Override
    public DefaultTableFormat setVisibleHeader(boolean visibleHeader) {
        this.visibleHeader = visibleHeader;
        return this;
    }

    @Override
    public NTableBordersFormat getBorder() {
        return border;
    }

    @Override
    public DefaultTableFormat setBorder(NTableBordersFormat border) {
        this.border = border;
        return this;
    }

    @Override
    public NTableFormat setBorder(String borderName) {
        NTableBordersFormat n = parseTableBorders(borderName);
        if (n == null) {
            throw new NIllegalArgumentException(getSession(), NMsg.ofC("unsupported border. use one of : %s", getAvailableTableBorders()));
        }
        setBorder(n);
        return this;
    }

    public DefaultTableFormat setVisibleColumn(int col, Boolean visible) {
        if (visible == null) {
            if (col >= 0 && col < visibleColumns.size()) {
                visibleColumns.set(col, null);
            }
        } else {
            if (col >= 0) {
                while (col < visibleColumns.size()) {
                    visibleColumns.add(null);
                }
                visibleColumns.set(col, visible);
            }
        }
        return this;
    }

    public Boolean getVisibleColumn(int col) {
        if (col >= 0 && col < visibleColumns.size()) {
            return visibleColumns.get(col);
        }
        return null;
    }

    public NTableFormat setCellFormat(NTableCellFormat formatter) {
        defaultCellFormatter = formatter == null ? DefaultTableCellFormat.INSTANCE : formatter;
        return this;
    }

    @Override
    public NTableModel getModel() {
        return createTableModel(model);
    }

    @Override
    public NTableFormat setValue(Object value) {
        this.model = value;
        return this;
    }

    private String getSeparator(NTableSeparator id) {
        String s = border.format(id);
        if (s == null) {
            return "";
        }
        return s;
    }

    @Override
    public void print(NPrintStream w) {
        NPrintStream out = getValidPrintStream(w);
        NStringBuilder line = new NStringBuilder();
        List<Row> rows = rebuild(getSession());
        if (rows.size() > 0) {
            List<DefaultCell> cells = rows.get(0).cells;
            NSession session = getSession();
            if ((getSeparator(NTableSeparator.FIRST_ROW_START)
                    + getSeparator(NTableSeparator.FIRST_ROW_SEP)
                    + getSeparator(NTableSeparator.FIRST_ROW_LINE)
                    + getSeparator(NTableSeparator.FIRST_ROW_END)).length() > 0) {
                line.append(getSeparator(NTableSeparator.FIRST_ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(getSeparator(NTableSeparator.FIRST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String B = getSeparator(NTableSeparator.FIRST_ROW_LINE);
                    String s = cell.rendered.toString();
                    line.append(CoreStringUtils.fillString(B, NTexts.of(session).parse(s).textLength()));
                }
                line.append(getSeparator(NTableSeparator.FIRST_ROW_END));

                out.print(line.trim().newLine().toString());
                out.flush();
                line.clear();
            }
            for (int i1 = 0; i1 < rows.size(); i1++) {
                if (i1 > 0) {
                    if ((getSeparator(NTableSeparator.MIDDLE_ROW_START)
                            + getSeparator(NTableSeparator.MIDDLE_ROW_SEP)
                            + getSeparator(NTableSeparator.MIDDLE_ROW_LINE)
                            + getSeparator(NTableSeparator.MIDDLE_ROW_END)).length() > 0) {
                        line.append(getSeparator(NTableSeparator.MIDDLE_ROW_START));
                        for (int i = 0; i < cells.size(); i++) {
                            if (i > 0) {
                                line.append(getSeparator(NTableSeparator.MIDDLE_ROW_SEP));
                            }
                            DefaultCell cell = cells.get(i);
                            String B = getSeparator(NTableSeparator.MIDDLE_ROW_LINE);
                            String s = cell.rendered.toString();
                            line.append(CoreStringUtils.fillString(B, NTexts.of(session).parse(s).textLength()));
                        }
                        line.append(getSeparator(NTableSeparator.MIDDLE_ROW_END));

                        out.print(line.trim().newLine().toString());
                        out.flush();
                        line.clear();
                    }
                }

                Row row = rows.get(i1);
                cells = row.cells;

                line.append(getSeparator(NTableSeparator.ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(getSeparator(NTableSeparator.ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String s = cell.rendered.toString();
                    line.append(s);
                }
                line.append(getSeparator(NTableSeparator.ROW_END));

                out.print(line.trim().newLine().toString());
                out.flush();
                line.clear();
            }

            if ((getSeparator(NTableSeparator.LAST_ROW_START)
                    + getSeparator(NTableSeparator.LAST_ROW_SEP)
                    + getSeparator(NTableSeparator.LAST_ROW_LINE)
                    + getSeparator(NTableSeparator.LAST_ROW_END)).length() > 0) {
                line.append(getSeparator(NTableSeparator.LAST_ROW_START));
                cells = rows.get(0).cells;
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(getSeparator(NTableSeparator.LAST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    String B = getSeparator(NTableSeparator.LAST_ROW_LINE);
                    String s = cell.rendered.toString();
                    line.append(CoreStringUtils.fillString(B, NTexts.of(session).parse(s).textLength()));
                }
                line.append(getSeparator(NTableSeparator.LAST_ROW_END));
            }
        }
        out.print(line.trim().toString());
        out.flush();
        line.clear();
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(out);
        print(w);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(getSession(), ex);
        }
        return out.toString();
    }

    private NTableCellFormat getTableCellFormat(DefaultCell dc) {
        return dc.isHeader() ? defaultHeaderFormatter : defaultCellFormatter;
    }

    private List<Row> rebuild(NSession session) {
        NTexts metrics = NTexts.of(session);
        List<Row> rows1 = new ArrayList<>();
        NTableModel model = getModel();
        int columnsCount = model.getColumnsCount();
        int rowsCount = model.getRowsCount();
        for (int rowIndex = 0; rowIndex < rowsCount; rowIndex++) {
            Row r = new Row();
            rows1.add(r);
            for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
                DefaultCell c = new DefaultCell(false);
                try {
                    c.value = model.getCellValue(rowIndex, columnIndex);
                    c.colspan = model.getCellColSpan(rowIndex, columnIndex);
                    c.rowspan = model.getCellRowSpan(rowIndex, columnIndex);
                    r.cells.add(c);
                } catch (Exception ex) {
                    //ignore
                }
            }
        }
        List<Row> effectiveRows = new ArrayList<>();
        Row header = new Row();
        try {
            for (int columnIndex = 0; columnIndex < columnsCount; columnIndex++) {
                DefaultCell c = new DefaultCell(true);
                try {
                    c.value = model.getHeaderValue(columnIndex);
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
        if (header.cells.size() > 0 && isVisibleHeader()) {
            effectiveRows.add(header);
        }
        boolean p = isVisibleColumnPositive();
        boolean n = isVisibleColumnNegative();
        for (int i = 0; i < rows1.size(); i++) {
            Row row = rows1.get(i);
            Row r2 = new Row();
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
                NTableCellFormat formatter = getTableCellFormat(cell);
                Object cvalue = cell.getValue();
                cell.setRendered(new RenderedCell(
                        c0, r0, cvalue,
                        formatter.format(r0, c0, cvalue, session),
                        formatter,
                        formatter.getVerticalAlign(r0, c0, cvalue, session),
                        formatter.getHorizontalAlign(r0, c0, cvalue, session),
                        metrics,
                        session
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
                int columns = b.evalColumnSize(cell.x, cell.colspan);
                cell.rendered = cell.rendered.resize(rows, columns);
                cell.cw = cell.getRendered().columns;
                cell.ch = cell.getRendered().rows;
            }
        }
        return effectiveRows;
    }

    public NTableFormat setHeaderFormat(NTableCellFormat formatter) {
        defaultHeaderFormatter = formatter == null ? DefaultTableHeaderFormat.INSTANCE : formatter;
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

    private static class SimpleRow {
        List<SimpleCell> cells = new ArrayList<>();
    }

    private static class SimpleCell {
        private String title;
        private NString value;

        public SimpleCell(String title, NString value) {
            this.title = title;
            this.value = value;
        }
    }

    private NTableModel createTableModel(Object o) {
        NSession session = getSession();
        if (o == null) {
            return NMutableTableModel.of(session);
        }
        if (o instanceof NTableModel) {
            return (NTableModel) o;
        }
        if (o instanceof String || o instanceof Number || o instanceof Date || o instanceof Temporal || o instanceof Path || o instanceof File) {
            List<NElement> a = new ArrayList<>();
            a.add(_elems().toElement(o));
            return createTableModel(_elems().toElement(a));
        }
        o = _elems().setIndestructibleFormat().destruct(o);
        if (o instanceof Collection) {
            return _model2(o, session);
        }
        if (o instanceof Map) {
            NMutableTableModel model = NMutableTableModel.of(session);
            LinkedHashSet<String> columns = new LinkedHashSet<>();
            columns.add("Name");
            columns.add("Value");
//            resolveColumns(o, columns);
            for (String column : columns) {
                model.addHeaderCell(column);
            }
            for (Map.Entry<Object, Object> eoelem2 : ((Map<Object, Object>) o).entrySet()) {
                model.newRow();
                model.addCell(formatObject(eoelem2.getKey()));
                model.addCell(formatObject(eoelem2.getValue()));
            }
            return model;
        }
        if (
                (o instanceof NMsg)
                        || (o instanceof NString)
                        || (o instanceof NFormattable)
        ) {
            NMutableTableModel model = NMutableTableModel.of(session);
            model.newRow();
            model.addCell(formatObject(o));
            return model;
        }
        if (!(o instanceof NElement)) {
            return createTableModel(_elems().toElement(o));
        }
        NElement elem = (NElement) o;
        switch (elem.type()) {
            case BOOLEAN:
            case INSTANT:
            case STRING:
//            case NUTS_STRING:
            case INTEGER:
            case FLOAT:
            case NULL: {
                List<NElement> a = new ArrayList<>();
                a.add(elem);
                return createTableModel(_elems().toElement(a));
            }
            case OBJECT: {
                return createTableModel(_elems().toElement(elem.asObject().get(session).entries()));
            }
            case ARRAY: {

                return _model2(elem,session);
            }
            default: {
                throw new NUnsupportedArgumentException(session, NMsg.ofC("unsupported %s", elem.type()));
            }
        }
    }

    public NMutableTableModel _model2(Object obj, NSession session) {
        NMutableTableModel model = NMutableTableModel.of(session);
        List<SimpleRow> rows=resolveColumnsFromRows(obj);
        List<String> titles=new ArrayList<>();
        Set<String> titlesSet=new HashSet<>();
        if(rows.size()>0){
            titles.addAll(rows.get(0).cells.stream().map(x->x.title).collect(Collectors.toList()));
            titlesSet.addAll(titles);
        }
        for (SimpleRow row : rows) {
            for (SimpleCell cell : row.cells) {
                if(!titlesSet.contains(cell.title)){
                    titlesSet.add(cell.title);
                    titles.add(cell.title);
                }
            }
        }
        for (String column : titles) {
            model.addHeaderCell(column);
        }
        for (SimpleRow row : rows) {
            model.newRow();
            Boolean[] visited=new Boolean[row.cells.size()];
            for (String title : titles) {
                List<SimpleCell> cells = row.cells;
                for (int i = 0; i < cells.size(); i++) {
                    SimpleCell cell = cells.get(i);
                    if(visited[i]==null){
                        if(Objects.equals(cell.title,title)){
                            if (cell.value != null) {
                                model.addCell(formatObject(cell.value));
                            } else {
                                model.addCell("");
                            }
                            visited[i]=true;
                            break;
                        }
                    }
                }
            }
        }
        return model;
    }
    public List<SimpleRow> resolveColumnsFromRows(Object obj) {
        List<SimpleRow> rows = new ArrayList<>();
        NSession session = getSession();
        if (obj instanceof NElement) {
            NElement value = (NElement) obj;
            switch (value.type()) {
                case ARRAY: {
                    for (NElement value2 : value.asArray().get(session).items()) {
                        rows.add(resolveColumnsFromRow(value2));
                    }
                    break;
                }
                default: {
                    rows.add(resolveColumnsFromRow(value));
                    break;
                }
            }
        } else if (obj instanceof Collection) {
            for (Object value2 : ((Collection) obj)) {
                rows.add(resolveColumnsFromRow(value2));
            }
        } else {
            rows.add(resolveColumnsFromRow(obj));
        }
        return rows;
    }

    public SimpleRow resolveColumnsFromRow(Object obj) {
        NSession session = getSession();
        if (obj instanceof NElement) {
            NElement value = (NElement) obj;
            switch (value.type()) {
                case OBJECT: {
                    SimpleRow e = new SimpleRow();
                    for (NElementEntry nutsNamedValue : value.asObject().get(session).entries()) {
                        NElement k = nutsNamedValue.getKey();
                        if (!k.isString()) {
                            k = _elems().ofString(
                                    k.toString()
                            );
                        }
                        e.cells.add(resolveColumnsFromCell(k.asString().get(session), nutsNamedValue.getValue()));
                    }
                    return e;
                }
                case ARRAY: {
                    SimpleRow e = new SimpleRow();
                    int column = 1;
                    for (NElement value2 : value.asArray().get(session).items()) {
                        e.cells.add(resolveColumnsFromCell("COL " + column,value2));
                        column++;
                    }
                    return e;
                }
                default: {
                    SimpleRow e = new SimpleRow();
                    e.cells.add(resolveColumnsFromCell("value", value));
                    return e;
                }
            }
        } else if (obj instanceof Collection) {
            SimpleRow e = new SimpleRow();
            int column = 1;
            for (Object value2 : ((Collection) obj)) {
                e.cells.add(resolveColumnsFromCell("COL " + column, value2));
                column++;
            }
            return e;
        } else if (obj instanceof Map) {
            Map<String, Object> m = new HashMap<>();
            Map<Object, Object> omap = (Map<Object, Object>) obj;
            SimpleRow e = new SimpleRow();
            for (Map.Entry<Object, Object> vv : omap.entrySet()) {
                String k = String.valueOf(vv.getKey());
                m.put(k, formatObject(vv.getValue()));
                e.cells.add(resolveColumnsFromCell(k, vv.getValue()));
            }
            return e;
        } else {
            SimpleRow e = new SimpleRow();
            e.cells.add(resolveColumnsFromCell("value", obj));
            return e;
        }
    }

    public SimpleCell resolveColumnsFromCell(String title, Object obj) {
        return new SimpleCell(title, obj == null ? null : formatObject(obj));
    }

    private NElements _elems() {
        return NElements.of(getSession()).setSession(getSession());
    }

    @Override
    public Object getValue() {
        return model;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session = getSession();
        NArg a;
        if ((a = cmdLine.nextFlag("--no-header").orNull()) != null) {
            boolean val = a.getBooleanValue().get(session);
            if (a.isActive()) {
                setVisibleHeader(!val);
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--header").orNull()) != null) {
            boolean val = a.getBooleanValue().get(session);
            if (a.isActive()) {
                setVisibleHeader(val);
            }
            return true;
        } else if ((a = cmdLine.nextEntry("--border").orNull()) != null) {
            if (a.isActive()) {
                setBorder(a.getValue().asString().orElse(""));
            }
            return true;
        } else if (cmdLine.hasNext() && cmdLine.isNextOption()) {
            int cc = getModel().getColumnsCount();

            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < cc; i++) {
                Object v = getModel().getHeaderValue(cc);
                if (v instanceof String) {
                    columns.put(v.toString().toLowerCase(), i);
                }
            }
            NArg a2 = null;
            for (Map.Entry<String, Integer> e : columns.entrySet()) {
                if ((a2 = cmdLine.next("--" + e.getKey()).orNull()) != null) {
                    if (a2.isActive()) {
                        setVisibleColumn(e.getValue(), true);
                    }
                    return true;
                } else if ((a2 = cmdLine.next("--no-" + e.getKey()).orNull()) != null) {
                    if (a2.isActive()) {
                        setVisibleColumn(e.getValue(), false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private NString formatObject(Object any) {
        checkSession();
        return NTextUtils.stringValueFormatted(any, false, getSession());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public static class Row {

        List<DefaultCell> cells = new ArrayList<>();
    }

    public static class RenderedCell {

        char[][] rendered;
        int rows;
        int columns;
        NTableCellFormat formatter;
        NTexts metrics;
        NPositionType valign;
        NPositionType halign;
        NWorkspace ws;
        NSession session;

        private RenderedCell(NTexts metrics, NSession session) {
            this.session = session;
            this.metrics = metrics;
            this.ws = session.getWorkspace();
        }

        public RenderedCell(int c, int r, Object o, String str, NTableCellFormat formatter, NPositionType valign, NPositionType halign, NTexts metrics, NSession session) {
            this.session = session;
            this.ws = session.getWorkspace();
            this.formatter = formatter;
            this.metrics = metrics;
            this.valign = valign;
            this.halign = halign;
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
                    columns = Math.max(columns, len(last.toString()));
                }
            }
            rows = strings.size();
            if (rows == 0) {
                rendered = new char[][]{{' '}};
                rows = 1;
                columns = 1;
            } else {
                rendered = new char[rows][];
                for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                    StringBuilder s = strings.get(i);
                    rendered[i] = s.toString().toCharArray();
                }
            }
        }

        public int len(String other) {
            return metrics.parse(other).textLength();
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
            RenderedCell c = new RenderedCell(metrics, session);
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
            RenderedCell c = new RenderedCell(metrics, session);
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
            RenderedCell c = new RenderedCell(metrics, session);
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
            RenderedCell c = new RenderedCell(metrics, session);
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public RenderedCell resize(int rows, int columns) {
            char[][] rendered0 = new char[rows][];
            switch (valign) {
                case FIRST: {
                    for (int i = 0; i < rows; i++) {
                        if (i < rendered.length) {
                            char[] chars = rendered[i];
                            int ll = len(new String(chars));
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                StringBuilder s = new StringBuilder();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics, session);
                                rendered0[i] = s.toString().toCharArray();
                            } else {
                                rendered0[i] = rendered[i];
                            }
                        } else {
                            rendered0[i] = CoreStringUtils.fillString(' ', columns).toCharArray();
                        }
                    }
                    break;
                }
                case LAST: {
                    //FIX ME
                    for (int i = 0; i < rows; i++) {
                        if (i < rows - rendered.length) {
                            rendered0[i] = CoreStringUtils.fillString(' ', columns).toCharArray();
                        } else {
                            char[] chars = rendered[i];
                            int ll = len(new String(chars));
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                StringBuilder s = new StringBuilder();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics, session);
                                rendered0[i] = s.toString().toCharArray();
                            } else {
                                rendered0[i] = rendered[i];
                            }
                        }
                    }
                    break;
                }
                case CENTER: {
                    //FIX ME
                    for (int i = 0; i < rows; i++) {
                        if (i < rendered.length) {
                            char[] chars = rendered[i];
                            int ll = len(new String(chars));
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                StringBuilder s = new StringBuilder();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics, session);
                                rendered0[i] = s.toString().toCharArray();
                            } else {
                                rendered0[i] = rendered[i];
                            }
                        } else {
                            rendered0[i] = CoreStringUtils.fillString(' ', columns).toCharArray();
                        }
                    }
                    break;
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
            RenderedCell c = new RenderedCell(metrics, session);
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
            RenderedCell c = new RenderedCell(metrics, session);
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

    public static class DefaultCell implements NTableCell {

        int colspan = 1;
        int rowspan = 1;
        int x;
        int y;
        int cx;
        int cy;
        int cw;
        int ch;

        Object value;
        RenderedCell rendered;
        boolean header;

        public DefaultCell(boolean header) {
            this.header = header;
        }

        public boolean isHeader() {
            return header;
        }

        public void setHeader(boolean header) {
            this.header = header;
        }

        public RenderedCell getRendered() {
            return rendered;
        }

        public void setRendered(RenderedCell rendered) {
            this.rendered = rendered;
        }

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

        //        public void setX(int x) {
//            this.x = x;
//        }
        @Override
        public int getY() {
            return y;
        }

        //        public void setY(int y) {
//            this.y = y;
//        }
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

    public static class Pos {

        int column;
        int row;

        public Pos(int column, int row) {
            this.column = column;
            this.row = row;
        }

        @Override
        public int hashCode() {

            return Objects.hash(column, row);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Pos pos = (Pos) o;
            return column == pos.column
                    && row == pos.row;
        }

        @Override
        public String toString() {
            return "("
                    + "" + column
                    + "," + row
                    + ')';
        }

    }

    public static class Widths {

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

    public static class Interval {

        int from;
        int to;

        public Interval(int from, int to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public int hashCode() {
            int result = from;
            result = 31 * result + to;
            return result;
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
        public String toString() {
            return "Interval{"
                    + "from=" + from
                    + ", to=" + to
                    + '}';
        }
    }

    public static class Bounds {

        List<Integer> columnSize = new ArrayList<>();
        List<Integer> rowSize = new ArrayList<>();
        Map<Interval, Integer> columnIntervalSize = new HashMap<Interval, Integer>();
        Map<Interval, Integer> rowIntervalSize = new HashMap<Interval, Integer>();

        Set<Pos> reserved = new HashSet<>();

        public void discardRow(int row) {
            for (Iterator<Pos> iterator = reserved.iterator(); iterator.hasNext(); ) {
                Pos pos = iterator.next();
                if (pos.row == row) {
                    iterator.remove();
                }
            }
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
                    (columnIntervalSize.containsKey(key) ? columnIntervalSize.get(key) : 0),
                    size));
        }

        public void setRowIntervalMinSize(int from, int to, int size) {
            if (from + 1 == to) {
                setRowMinSize(from, size);
                return;
            }
            Interval key = new Interval(from, to);
            rowIntervalSize.put(key, Math.max(
                    (rowIntervalSize.containsKey(key) ? rowIntervalSize.get(key) : 0),
                    size));
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

}
