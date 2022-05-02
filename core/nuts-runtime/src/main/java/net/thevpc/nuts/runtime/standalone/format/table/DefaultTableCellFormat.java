package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.format.NutsTableCellFormat;

/**
 *
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableCellFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableCellFormat();

    public DefaultTableCellFormat() {
    }

    @Override
    public String format(int row, int col, Object value, NutsSession session) {
        return String.valueOf(value);
    }

}
