package net.vpc.app.nuts.app.util;

import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.commandline.format.TableFormatter;

public class DefaultWorkspaceCellFormatter implements TableFormatter.CellFormatter {
    private NutsWorkspace ws;

    public DefaultWorkspaceCellFormatter(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public String format(int row, int col, Object value) {
        return String.valueOf(value);
    }

    @Override
    public int stringLength(String value) {
        return ws.parser().filterText(value).length();
    }
}
