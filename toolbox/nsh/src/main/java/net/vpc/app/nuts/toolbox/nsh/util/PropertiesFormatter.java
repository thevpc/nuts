package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.common.strings.StringUtils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PropertiesFormatter {
    private boolean sort;
    private boolean table;
    private String separator=" = ";

    public boolean isSort() {
        return sort;
    }

    public boolean isTable() {
        return table;
    }

    public PropertiesFormatter setTable(boolean table) {
        this.table = table;
        return this;
    }

    public String getSeparator() {
        return separator;
    }

    public PropertiesFormatter setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public PropertiesFormatter setSort(boolean sort) {
        this.sort = sort;
        return this;
    }

    public void format(Map map, PrintStream out){
        List<Object> keys = new ArrayList(map.keySet());
        if (sort) {
            keys.sort(null);
        }
        if(table) {
            int maxSize = 0;
            for (Object k : keys) {
                maxSize = Math.max(maxSize, formatValue(k).length());
            }
            for (Object k : keys) {
                Object v1 = map.get(k);
                if(table) {
                    out.printf("[[%s]]%s%s\n", StringUtils.alignLeft(formatValue(k), maxSize), separator, formatValue(v1));
                }else{

                }
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
