package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.text.NTableCellFormat;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;

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
    public NText format(int row, int col, NText value) {
        return NTextBuilder.of().append(value==null?NText.ofBlank() : value,
                NTextStyle.primary1()
                );
    }

    @Override
    public NPositionType getHorizontalAlign(int row, int col, NText value) {
        return NPositionType.HEADER;
    }

}
