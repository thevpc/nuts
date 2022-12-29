package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.format.NTableCellFormat;

/**
 *
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableCellFormat implements NTableCellFormat {

    public static final NTableCellFormat INSTANCE = new DefaultTableCellFormat();

    public DefaultTableCellFormat() {
    }

    @Override
    public String format(int row, int col, Object value, NSession session) {
        return String.valueOf(value);
    }

}
