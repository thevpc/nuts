package net.vpc.app.nuts.core.util.app;

import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsTableCellFormat;

/**
 * 
 * @author vpc
 * @since 0.5.5
 */
public class NutsColoredCellFormatter implements NutsTableCellFormat {
    final NutsApplicationContext appContext;

    public NutsColoredCellFormatter(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public int stringLength(String value) {
        return appContext.getWorkspace().parser().filterText(value).length();
    }

    @Override
    public String format(int row, int col, Object value) {
        return String.valueOf(value);
    }
}
