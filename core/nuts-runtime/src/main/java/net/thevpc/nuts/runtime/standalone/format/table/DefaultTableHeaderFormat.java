package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.format.NTableCellFormat;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

/**
 *
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableHeaderFormat implements NTableCellFormat {

    public static final NTableCellFormat INSTANCE = new DefaultTableHeaderFormat();

    public DefaultTableHeaderFormat() {
    }

    @Override
    public String format(int row, int col, Object value) {
        return NTextBuilder.of().append(String.valueOf(value),
                NTextStyle.primary1()
                ).toString();
    }

    @Override
    public NPositionType getHorizontalAlign(int row, int col, Object value) {
        return NPositionType.HEADER;
    }

}
