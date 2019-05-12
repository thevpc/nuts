package net.vpc.app.nuts.core.app;

import net.vpc.app.nuts.NutsTableCellFormat;
import net.vpc.app.nuts.NutsWorkspace;

/**
 * 
 * @author vpc
 * @since 0.5.5
 */
public class NutsDefaultWorkspaceCellFormat implements NutsTableCellFormat {
    NutsWorkspace ws;

    public NutsDefaultWorkspaceCellFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public int stringLength(String value) {
        return ws.parser().filterText(value).length();
    }

    @Override
    public String format(int row, int col, Object value) {
        return String.valueOf(value);
    }

}
