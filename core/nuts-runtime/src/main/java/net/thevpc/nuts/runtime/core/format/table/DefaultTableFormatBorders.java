package net.thevpc.nuts.runtime.core.format.table;

import net.thevpc.nuts.NutsTableFormat;
import net.thevpc.nuts.NutsTableBordersFormat;

public class DefaultTableFormatBorders implements NutsTableBordersFormat {

    private final String[] config = new String[NutsTableFormat.Separator.values().length];

    public DefaultTableFormatBorders(String... config) {
        System.arraycopy(config, 0, this.config, 0, this.config.length);
    }

    @Override
    public String format(NutsTableFormat.Separator s) {
        return config[s.ordinal()];
    }
}
