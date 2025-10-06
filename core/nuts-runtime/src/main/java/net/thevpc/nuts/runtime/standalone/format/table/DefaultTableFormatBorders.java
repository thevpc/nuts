package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.text.NTableBordersFormat;
import net.thevpc.nuts.text.NTableSeparator;
import net.thevpc.nuts.text.NText;

public class DefaultTableFormatBorders implements NTableBordersFormat {

    private final String name;
    private final NText[] config = new NText[NTableSeparator.values().length];

    public DefaultTableFormatBorders(String name, NText... config) {
        this.name = name;
        System.arraycopy(config, 0, this.config, 0, this.config.length);
    }

    public DefaultTableFormatBorders(String name, String... config) {
        this.name = name;
        for (int i = 0; i < config.length; i++) {
            this.config[i] = NText.ofPlain(config[i]);
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public NText format(NTableSeparator s) {
        return config[s.ordinal()];
    }
}
