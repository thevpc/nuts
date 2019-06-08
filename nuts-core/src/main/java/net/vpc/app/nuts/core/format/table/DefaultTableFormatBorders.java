package net.vpc.app.nuts.core.format.table;

import net.vpc.app.nuts.NutsTableFormat;
import net.vpc.app.nuts.NutsTableBordersFormat;

public class DefaultTableFormatBorders implements NutsTableBordersFormat{
    private final String[] config = new String[NutsTableFormat.Separator.values().length];

    public DefaultTableFormatBorders(String ... config) {
        System.arraycopy(config,0,this.config,0,this.config.length);
    }

    @Override
    public String format(NutsTableFormat.Separator s) {
        return config[s.ordinal()];
    }
}
