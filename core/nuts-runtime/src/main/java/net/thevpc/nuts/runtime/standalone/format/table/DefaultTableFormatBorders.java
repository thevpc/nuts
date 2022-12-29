package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.format.NTableBordersFormat;
import net.thevpc.nuts.format.NTableSeparator;

public class DefaultTableFormatBorders implements NTableBordersFormat {

    private final String[] config = new String[NTableSeparator.values().length];

    public DefaultTableFormatBorders(String... config) {
        System.arraycopy(config, 0, this.config, 0, this.config.length);
    }

    @Override
    public String format(NTableSeparator s) {
        return config[s.ordinal()];
    }
}
