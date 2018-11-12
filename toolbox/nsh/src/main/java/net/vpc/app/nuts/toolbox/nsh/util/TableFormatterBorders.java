package net.vpc.app.nuts.toolbox.nsh.util;

public class TableFormatterBorders {
    private String[] config = new String[TableFormatter.Separator.values().length];

    public TableFormatterBorders(String ... config) {
        System.arraycopy(config,0,this.config,0,this.config.length);
    }

    public String get(TableFormatter.Separator s) {
        return config[s.ordinal()];
    }
}
