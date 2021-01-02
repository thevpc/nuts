package net.thevpc.nuts.runtime.core.format.table;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTableCellFormat;

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
