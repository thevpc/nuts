package net.vpc.app.nuts.core.util.cmdline.format;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsPropertiesFormat;

public class DefaultPropertiesFormat implements NutsPropertiesFormat{
    private boolean sort;
    private boolean table;
    private String separator=" = ";

    public boolean isSort() {
        return sort;
    }

    public boolean isTable() {
        return table;
    }

    public DefaultPropertiesFormat setTable(boolean table) {
        this.table = table;
        return this;
    }

    public String getSeparator() {
        return separator;
    }

    public DefaultPropertiesFormat setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public DefaultPropertiesFormat setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public void format(Map map, PrintStream out){
        List<Object> keys = new ArrayList(map.keySet());
        if (sort) {
            Collections.sort(keys,null);
        }
        if(table) {
            int maxSize = 0;
            for (Object k : keys) {
                maxSize = Math.max(maxSize, formatValue(k).length());
            }
            for (Object k : keys) {
                Object v1 = map.get(k);
                out.printf("[[%s]]%s%s\n", FormatUtils.alignLeft(formatValue(k), maxSize), separator, formatValue(v1));
            }
        }else{
            for (Object k : keys) {
                Object v1 = map.get(k);
                out.printf("[[%s]]%s%s\n", formatValue(k), separator, formatValue(v1));
            }
        }
    }

    private String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String svalue=String.valueOf(value);
        for (char c : svalue.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }
}
