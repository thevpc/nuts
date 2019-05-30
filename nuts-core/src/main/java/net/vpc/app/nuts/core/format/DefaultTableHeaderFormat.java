package net.vpc.app.nuts.core.format;

import net.vpc.app.nuts.NutsAlignFormat;
import net.vpc.app.nuts.NutsTableCellFormat;

/**
 *
 * @author vpc
 * @since 0.5.5
 */
public class DefaultTableHeaderFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableHeaderFormat();

    public DefaultTableHeaderFormat() {
    }

    @Override
    public String format(int row, int col, Object value) {
        return "==" + String.valueOf(value) + "==";
    }

    @Override
    public NutsAlignFormat getHorizontalAlign(int row, int col, Object value) {
        return NutsAlignFormat.CENTER;
    }

}
