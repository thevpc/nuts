package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.text.NTableCellFormat;
import net.thevpc.nuts.text.NText;

/**
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableCellFormat implements NTableCellFormat {

    public static final NTableCellFormat INSTANCE = new DefaultTableCellFormat();

    public DefaultTableCellFormat() {
    }

    @Override
    public NText format(int row, int col, NText value) {
        return value == null ? NText.ofBlank() : value;
    }

}
