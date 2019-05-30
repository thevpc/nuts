package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsTableCellFormat;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public class DefaultTableCellFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableCellFormat();

    public DefaultTableCellFormat() {
    }

    @Override
    public String format(int row, int col, Object value) {
        return String.valueOf(value);
    }

}
