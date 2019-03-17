package net.vpc.app.nuts.app;

import net.vpc.common.commandline.format.TableFormatter;

public class ColoredCellFormatter implements TableFormatter.CellFormatter {
    private final NutsApplicationContext appContext;

    public ColoredCellFormatter(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public int stringLength(String value) {
        return appContext.getWorkspace().getParseManager().filterText(value).length();
    }

    @Override
    public String format(int row, int col, Object value) {
        return String.valueOf(value);
    }
}
