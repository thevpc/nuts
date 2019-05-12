package net.vpc.app.nuts.core.app.format;

import net.vpc.app.nuts.NutsTableFormatterBorders;
import net.vpc.app.nuts.NutsTableFormat;

public class DefaultTableFormatterBorders implements NutsTableFormatterBorders{
    private String[] config = new String[NutsTableFormat.Separator.values().length];

    public DefaultTableFormatterBorders(String ... config) {
        System.arraycopy(config,0,this.config,0,this.config.length);
    }

    public String get(NutsTableFormat.Separator s) {
        return config[s.ordinal()];
    }
}
