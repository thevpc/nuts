package net.thevpc.nuts.runtime.core.format.table;

import net.thevpc.nuts.NutsPositionType;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTableCellFormat;
import net.thevpc.nuts.NutsTextStyle;

/**
 *
 * @author thevpc
 * @since 0.5.5
 */
public class DefaultTableHeaderFormat implements NutsTableCellFormat {

    public static final NutsTableCellFormat INSTANCE = new DefaultTableHeaderFormat();

    public DefaultTableHeaderFormat() {
    }

    @Override
    public String format(int row, int col, Object value, NutsSession session) {
        return session.getWorkspace().text().builder().append(String.valueOf(value),
                NutsTextStyle.primary(1)
                ).toString();
    }

    @Override
    public NutsPositionType getHorizontalAlign(int row, int col, Object value, NutsSession session) {
        return NutsPositionType.HEADER;
    }

}
