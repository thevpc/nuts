package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NUnsupportedArgumentException;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableCellFormat;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableFormatBorders;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableHeaderFormat;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextBuilder;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;

import java.util.*;

public class DefaultNTextArtTableRenderer implements NTextArtTableRenderer, NTextArtTextRenderer {
    private final List<Boolean> visibleColumns = new ArrayList<>();
    private boolean visibleHeader = true;
    private NTableCellFormat defaultCellFormatter = DefaultTableCellFormat.INSTANCE;
    private NTableCellFormat defaultHeaderFormatter = DefaultTableHeaderFormat.INSTANCE;


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

    public static DefaultTableFormatBorders NO_BORDER = new DefaultTableFormatBorders(
            "none",
            "", "", "", "",
            "", "", "",
            "", "", "", "",
            "", "", "", ""
    );

    public static DefaultTableFormatBorders UNICODE_BORDER = new DefaultTableFormatBorders(
            "unicode",
            "╭", "─", "┬", "╮",
            "│", "│", "│",
            "├", "─", "┼", "┤",
            "╰", "─", "┴", "╯"
    );

    public static DefaultTableFormatBorders SIMPLE_BORDER = new DefaultTableFormatBorders(
            "simple",
            "-", "-", "-", "-",
            "|", " | ", "|",
            "|", "-", "+", "|",
            "-", "-", "-", "-"
    );
    public static DefaultTableFormatBorders FANCY_ROWS_BORDER = new DefaultTableFormatBorders(
            "rows",
            "", "", "", "",
            "", " ", "",
            "", "─", "─", "",
            "", "", "", ""
    );
    public static DefaultTableFormatBorders SPACE_BORDER = new DefaultTableFormatBorders(
            "spaces",
            "", "", "", "",
            "", " ", "",
            "", "", "", "",
            "", "", "", ""
    );
    public static DefaultTableFormatBorders FANCY_COLUMNS_BORDER = new DefaultTableFormatBorders(
            "columns",
            "", "", "", "",
            "", " │ ", "",
            "", "", "", "",
            "", "", "", ""
    );
    private static Map<String, NTableBordersFormat> AVAILABLE = new HashMap<>();

    static {
        AVAILABLE.put(NO_BORDER.getName(), NO_BORDER);
        AVAILABLE.put(UNICODE_BORDER.getName(), UNICODE_BORDER);
        AVAILABLE.put(SIMPLE_BORDER.getName(), SIMPLE_BORDER);
        AVAILABLE.put(FANCY_ROWS_BORDER.getName(), FANCY_ROWS_BORDER);
        AVAILABLE.put(SPACE_BORDER.getName(), SPACE_BORDER);
        AVAILABLE.put(FANCY_COLUMNS_BORDER.getName(), FANCY_COLUMNS_BORDER);
    }

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

    public static NTableBordersFormat parseTableBorders(String borderName) {
        switch (borderName) {
            case "default": {
                return CorePlatformUtils.SUPPORTS_UTF_ENCODING ? UNICODE_BORDER : SIMPLE_BORDER;
            }
        }
        return AVAILABLE.get(borderName);
    }

    public NTableBordersFormat getBorder() {
        return border;
    }

    public DefaultNTextArtTableRenderer setBorder(NTableBordersFormat border) {
        this.border = border;
        return this;
    }

    public DefaultNTextArtTableRenderer setBorder(String borderName) {
        NTableBordersFormat n = parseTableBorders(borderName);
        if (n == null) {
            throw new NIllegalArgumentException(NMsg.ofC("unsupported border. use one of : %s", getAvailableTableBorders()));
        }
        setBorder(n);
        return this;
    }


    public NText getSeparators(NTableSeparator... id) {
        NTextBuilder b = NTextBuilder.of();
        for (NTableSeparator nText : id) {
            b.append(getSeparator(nText));
        }
        return b.build();
    }

    public NText getSeparator(NTableSeparator id) {
        NText s = border.format(id);
        if (s == null) {
            return NText.ofBlank();
        }
        return s;
    }

    @Override
    public String getName() {
        return ((DefaultTableFormatBorders) border).getName();
    }

    public NText render(NText model) {
        return render(NTableModel.of().addRow(model));
    }

    public NText render(NTableModel model) {
        NTextBuilder out = new DefaultNTextBuilder();
        NTextBuilder line = new DefaultNTextBuilder();
        List<Row> rows = rebuild(model);
        if (rows.size() > 0) {
            List<DefaultCell> cells = rows.get(0).cells;
            if (!this.getSeparators(NTableSeparator.FIRST_ROW_START, NTableSeparator.FIRST_ROW_SEP, NTableSeparator.FIRST_ROW_LINE, NTableSeparator.FIRST_ROW_END).isEmpty()) {
                line.append(this.getSeparator(NTableSeparator.FIRST_ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(this.getSeparator(NTableSeparator.FIRST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    NText B = this.getSeparator(NTableSeparator.FIRST_ROW_LINE);
                    NText s = cell.rendered.toText();
                    line.append(B.repeat(s.length()));
                }
                line.append(this.getSeparator(NTableSeparator.FIRST_ROW_END));

                out.append(line.trim().newLine().build());
                line.clear();
            }
            for (int i1 = 0; i1 < rows.size(); i1++) {
                if (i1 > 0) {
                    if (!this.getSeparators(NTableSeparator.MIDDLE_ROW_START, NTableSeparator.MIDDLE_ROW_SEP, NTableSeparator.MIDDLE_ROW_LINE, NTableSeparator.MIDDLE_ROW_END).isEmpty()) {
                        line.append(this.getSeparator(NTableSeparator.MIDDLE_ROW_START));
                        for (int i = 0; i < cells.size(); i++) {
                            if (i > 0) {
                                line.append(this.getSeparator(NTableSeparator.MIDDLE_ROW_SEP));
                            }
                            DefaultCell cell = cells.get(i);
                            NText B = this.getSeparator(NTableSeparator.MIDDLE_ROW_LINE);
                            NText s = cell.rendered.toText();
                            line.append(B.repeat(s.length()));
                        }
                        line.append(this.getSeparator(NTableSeparator.MIDDLE_ROW_END));
                        line.trim();
                        out.append(line.newLine().build());
                        line.clear();
                    }
                }

                Row row = rows.get(i1);
                cells = row.cells;

                line.append(this.getSeparator(NTableSeparator.ROW_START));
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(this.getSeparator(NTableSeparator.ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    NText s = cell.rendered.toText();
                    line.append(s);
                }
                line.append(this.getSeparator(NTableSeparator.ROW_END));

                out.append(line.trim().newLine().build());
                line.clear();
            }

            if (!(this.getSeparators(NTableSeparator.LAST_ROW_START, NTableSeparator.LAST_ROW_SEP, NTableSeparator.LAST_ROW_LINE, NTableSeparator.LAST_ROW_END)).isEmpty()) {
                line.append(this.getSeparator(NTableSeparator.LAST_ROW_START));
                cells = rows.get(0).cells;
                for (int i = 0; i < cells.size(); i++) {
                    if (i > 0) {
                        line.append(this.getSeparator(NTableSeparator.LAST_ROW_SEP));
                    }
                    DefaultCell cell = cells.get(i);
                    NText B = this.getSeparator(NTableSeparator.LAST_ROW_LINE);
                    NText s = cell.rendered.toText();
                    line.append(B.repeat(s.length()));
                }
                line.append(this.getSeparator(NTableSeparator.LAST_ROW_END));
            }
        }
        out.append(line.trim().build());
        line.clear();
        return out.build();
    }


    public static class Row {

        List<DefaultCell> cells = new ArrayList<>();
    }

    public static class RenderedCell {

        NPrimitiveText[][] rendered;
        int rows;
        int columns;
        NTableCellFormat formatter;
        NTexts metrics;
        NPositionType valign;
        NPositionType halign;

        private RenderedCell(NTexts metrics) {
            this.metrics = metrics;
        }

        public RenderedCell(int c, int r, Object o, NText str, NTableCellFormat formatter, NPositionType valign, NPositionType halign, NTexts metrics) {
            this.formatter = formatter;
            this.metrics = metrics;
            this.valign = valign;
            this.halign = halign;
            if (str == null) {
                str = NText.of("");
            }else{
                str=str.normalize();
            }
            List<NTextBuilder> strings = new ArrayList<>();
            List<NText> st = str.split("\n", true);
            columns = 0;
            for (NText e : st) {
                if (e.type() == NTextType.PLAIN && ((NTextPlain) e).getValue().equals("\n")) {
                    strings.add(NTextBuilder.of());
                } else {
                    if (strings.isEmpty()) {
                        strings.add(NTextBuilder.of());
                    }
                    NTextBuilder last = strings.get(strings.size() - 1);
                    last.append(e);
                    columns = Math.max(columns, last.length());
                }
            }
            rows = strings.size();
            if (rows == 0) {
                rendered = new NPrimitiveText[][]{{SPACE()}};
                rows = 1;
                columns = 1;
            } else {
                rendered = new NPrimitiveText[rows][];
                for (int i = 0, stringsSize = strings.size(); i < stringsSize; i++) {
                    NTextBuilder s = strings.get(i);
                    rendered[i] = s.toCharArray();
                }
            }
        }

        public int len(String other) {
            return metrics.of(other).length();
        }

        public RenderedCell appendHorizontally(RenderedCell other) {
            NPrimitiveText[][] rendered0 = new NPrimitiveText[Math.max(rows, other.rows)][];
            for (int i = 0; i < rendered0.length; i++) {
                NTextBuilder sb = new DefaultNTextBuilder();
                if (i < rendered.length) {
                    sb.append(rendered[i]);
                } else {
                    NPrimitiveText[] a = new NPrimitiveText[columns];
                    Arrays.fill(a, 0, columns, SPACE());
                    sb.append(a);
                }
                if (i < other.rendered.length) {
                    sb.append(other.rendered[i]);
                } else {
                    NPrimitiveText[] a = new NPrimitiveText[other.columns];
                    Arrays.fill(a, 0, other.columns, SPACE());
                    sb.append(a);
                }
                rendered0[i] = sb.toCharArray();
            }
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns + other.columns;
            c.rows = rendered0.length;
            return c;
        }

        public RenderedCell appendVertically(RenderedCell other) {
            NPrimitiveText[][] rendered0 = new NPrimitiveText[rows + other.rows][];
            int cols = Math.max(columns, other.columns);
            for (int i = 0; i < rendered.length; i++) {
                NTextBuilder sb = new DefaultNTextBuilder();
                sb.append(rendered[i]);
                int remaining = cols - columns;
                if (remaining > 0) {
                    NPrimitiveText[] a = new NPrimitiveText[remaining];
                    Arrays.fill(a, 0, remaining, SPACE());
                    sb.append(a);
                }
                rendered0[i] = sb.toCharArray();
            }
            for (int i = 0; i < other.rendered.length; i++) {
                NTextBuilder sb = new DefaultNTextBuilder();
                sb.append(other.rendered[i]);
                int remaining = cols - other.columns;
                if (remaining > 0) {
                    NPrimitiveText[] a = new NPrimitiveText[remaining];
                    Arrays.fill(a, 0, remaining, SPACE());
                    sb.append(a);
                }
                rendered0[i + rendered.length] = sb.toCharArray();
            }
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns + other.columns;
            c.rows = rendered0.length;
            return c;
        }

        public RenderedCell replaceContent(RenderedCell other, int row, int col) {
            NPrimitiveText[][] rendered0 = resize(Math.max(rows, row + other.rows), Math.max(columns, col + other.columns)).rendered;
            for (int r = 0; r < other.rows; r++) {
                for (int c = 0; c < other.columns; c++) {
                    rendered0[row + r][col + c] = other.rendered[r][c];
                }
            }
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public RenderedCell subCell(int row, int col, int toRow, int toCol) {
            NPrimitiveText[][] rendered0 = new NPrimitiveText[toRow - row][toCol - col];
            for (int i = 0; i < rendered0.length; i++) {
                System.arraycopy(rendered[i + row], col, rendered0[i], 0, toCol - col);
            }
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public RenderedCell resize(int rows, int columns) {
            NPrimitiveText[][] rendered0 = new NPrimitiveText[rows][];
            switch (valign) {
                case FIRST: {
                    for (int i = 0; i < rows; i++) {
                        if (i < rendered.length) {
                            NPrimitiveText[] chars = rendered[i];
                            int ll = NTextBuilder.of().appendAll(chars).length();
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                NTextBuilder s = NTextBuilder.of();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics);
                                rendered0[i] = s.toCharArray();
                            } else {
                                rendered0[i] = rendered[i];
                            }
                        } else {
                            rendered0[i] = NText.ofPlain(" ").repeat(columns).toCharArray();
                        }
                    }
                    break;
                }
                case LAST: {
                    //FIX ME
                    for (int i = 0; i < rows; i++) {
                        if (i < rows - rendered.length) {
                            rendered0[i] = NText.ofPlain(" ").repeat(columns).toCharArray();
                        } else {
                            NPrimitiveText[] chars = rendered[i];
                            int ll = NTextBuilder.of().appendAll(chars).length();
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                NTextBuilder s = NTextBuilder.of();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics);
                                rendered0[i] = s.toCharArray();
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
                            NPrimitiveText[] chars = rendered[i];
                            int ll = NTextBuilder.of().appendAll(chars).length();
                            int min = Math.min(columns, ll);
                            if (min < columns) {
                                int x = columns - min;
                                NTextBuilder s = NTextBuilder.of();
                                s.append(chars);
                                formatAndHorizontalAlign(s, halign, columns, metrics);
                                rendered0[i] = s.toCharArray();
                            } else {
                                rendered0[i] = rendered[i];
                            }
                        } else {
                            rendered0[i] = NText.ofPlain(" ").repeat(columns).toCharArray();
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
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            c.formatter = formatter;
            return c;
        }

        public RenderedCell copy() {
            NPrimitiveText[][] rendered0 = new NPrimitiveText[rendered.length][];
            for (int i = 0; i < rendered0.length; i++) {
                rendered0[i] = new NPrimitiveText[rendered[i].length];
                System.arraycopy(rendered[i], 0, rendered0[i], 0, rendered[i].length);
            }
            RenderedCell c = new RenderedCell(metrics);
            c.rendered = rendered0;
            c.columns = columns;
            c.rows = rows;
            return c;
        }

        public NText toText() {
            NTextBuilder sb = NTextBuilder.of();
            for (int i = 0, renderedLength = rendered.length; i < renderedLength; i++) {
                NPrimitiveText[] chars = rendered[i];
                if (i > 0) {
                    sb.newLine();
                }
                sb.appendAll(Arrays.asList(chars));
            }
            return sb.build();
        }

        @Override
        public String toString() {
            return toText().filteredText();
        }
    }

    private static NPrimitiveText SPACE() {
        return (NPrimitiveText) NText.ofPlain(" ");
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

        NText value;
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
        public NText getValue() {
            return value;
        }

        @Override
        public DefaultCell setValue(NText value) {
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

    public static void formatAndHorizontalAlign(NTextBuilder sb, NPositionType a, int columns, NTexts tf) {
        int length = sb.length();
        switch (a) {
            case FIRST: {
//                if (sb.length() > length) {
//                    sb.delete(length, sb.length());
//                }
                while (length < columns) {
                    sb.append(NText.ofPlain(" "));
                    length++;
                }
                break;
            }
            case LAST: {
//                if (sb.length() > length) {
//                    sb.delete(length, sb.length());
//                }
                while (length < columns) {
                    sb.insert(0, NText.ofPlain(" "));
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
                        sb.append(NText.ofPlain(" "));
                    } else {
                        sb.insert(0, NText.ofPlain(" "));
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
                        sb.append(NText.ofPlain(" "));
                    } else {
                        sb.insert(0, NText.ofPlain(" "));
                        maxBefore--;
                    }
                    after = !after;
                    length++;
                }
                break;
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported position type %s", a));
            }
        }
    }

    private NTableCellFormat getTableCellFormat(DefaultCell dc) {
        return dc.isHeader() ? defaultHeaderFormatter : defaultCellFormatter;
    }

    private List<Row> rebuild(NTableModel model) {
        NTexts metrics = NTexts.of();
        List<Row> rows1 = new ArrayList<>();
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
                NText cvalue = cell.getValue();
                cell.setRendered(new RenderedCell(
                        c0, r0, cvalue,
                        formatter.format(r0, c0, cvalue),
                        formatter,
                        formatter.getVerticalAlign(r0, c0, cvalue),
                        formatter.getHorizontalAlign(r0, c0, cvalue),
                        metrics
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

    public DefaultNTextArtTableRenderer setVisibleColumn(int col, Boolean visible) {
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

    public boolean isVisibleHeader() {
        return visibleHeader;
    }

    public DefaultNTextArtTableRenderer setVisibleHeader(boolean visibleHeader) {
        this.visibleHeader = visibleHeader;
        return this;
    }

    public DefaultNTextArtTableRenderer setHeaderFormat(NTableCellFormat formatter) {
        defaultHeaderFormatter = formatter == null ? DefaultTableHeaderFormat.INSTANCE : formatter;
        return this;
    }

    public DefaultNTextArtTableRenderer setCellFormat(NTableCellFormat formatter) {
        defaultCellFormatter = formatter == null ? DefaultTableCellFormat.INSTANCE : formatter;
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

}
