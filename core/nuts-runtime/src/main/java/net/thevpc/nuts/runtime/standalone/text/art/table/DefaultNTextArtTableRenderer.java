package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableCellFormat;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableFormatBorders;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultTableHeaderFormat;
import net.thevpc.nuts.runtime.standalone.text.art.region.NTextRegion;
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
                "none",
                "unicode"
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
            "|", "|", "|",
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
            throw new NIllegalArgumentException(NMsg.ofC("unsupported border '%s'. use one of : %s", borderName, getAvailableTableBorders()));
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
        DefaultTableFormatBorders b = (DefaultTableFormatBorders) border;
        return b == null ? "none" : b.getName();
    }

    public NText render(NText model) {
        return render(NTableModel.of().addRow(model));
    }

    // Replace the existing render method with this version that includes the bottom border
    public NText render(NTableModel model) {
        NTextBuilder out = NTextBuilder.of();

        // --- Compute col widths ---
        TableModelRenderHelper ctx = new TableModelRenderHelper(model);
        ctx.prepare(visibleColumns, visibleHeader, defaultHeaderFormatter, defaultHeaderFormatter);
//        ctx.dump();
        if (ctx.isEmpty()) return NText.of("");
        NTextRegion nTextRegion = ctx.renderTable(border);
        out.append(nTextRegion.toText());
        return out.build();
    }


    private static class CellRender {
        NText text;
        int span;
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


    @Override
    public String toString() {
        return "DefaultNTextArtTableRenderer(" + getName() + ")";
    }
}
