package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.NutsTableBordersFormat;
import net.thevpc.nuts.NutsTableSeparator;

public class DefaultTableFormatBorders implements NutsTableBordersFormat {

    private final String[] config = new String[NutsTableSeparator.values().length];

    public DefaultTableFormatBorders(String... config) {
        System.arraycopy(config, 0, this.config, 0, this.config.length);
    }

    @Override
    public String format(NutsTableSeparator s) {
        return config[s.ordinal()];
    }
}
