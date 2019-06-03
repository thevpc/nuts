package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsPropertiesFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

public class DefaultPropertiesFormat extends DefaultFormatBase<NutsPropertiesFormat> implements NutsPropertiesFormat {

    public static final String OPTION_MULTILINE_PROPERTY = "--multiline-property";
    private boolean sort;
    private boolean compact;
    private boolean javaProps;
    private boolean escapeText = true;
    private String separator = " = ";
    private Map model;
    private Map<String, String> multilineProperties = new HashMap<>();

    public DefaultPropertiesFormat(NutsWorkspace ws) {
        super(ws, "props-format");
    }

    @Override
    public boolean configureFirst(NutsCommand commandLine) {
        NutsArgument a;
        if ((a = commandLine.nextString(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = a.getValue();
            addMultilineProperty(i.getKey().getString(), i.getValue().getString());
            return true;
        } else if ((a = commandLine.nextBoolean("--compact")) != null) {
            this.compact = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.nextBoolean("--props")) != null) {
            this.javaProps = a.getValue().getBoolean();
            return true;
        } else if ((a = commandLine.nextBoolean("--escape-text")) != null) {
            this.escapeText = a.getValue().getBoolean();
            return true;
        }
        return false;
    }

    public DefaultPropertiesFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    public Map getModel() {
        return model;
    }

    @Override
    public NutsPropertiesFormat model(Map model) {
        return setModel(model);
    }

    @Override
    public NutsPropertiesFormat sort() {
        return sort(true);
    }

    @Override
    public NutsPropertiesFormat separator(String separator) {
        return setSeparator(separator);
    }

    @Override
    public NutsPropertiesFormat sort(boolean sort) {
        return setSort(sort);
    }

    public NutsPropertiesFormat setModel(Map model) {
        this.model = model;
        return this;
    }

    public boolean isSort() {
        return sort;
    }

//    @Override
//    public NutsPropertiesFormat table(boolean table) {
//        return setTable(table);
//    }
//    @Override
//    public NutsPropertiesFormat table() {
//        return table(true);
//    }
//    public boolean isTable() {
//        return table;
//    }
//
//    public DefaultPropertiesFormat setTable(boolean table) {
//        this.table = table;
//        return this;
//    }
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

    public void print(PrintStream out) {
        print(new PrintWriter(out));
    }

    @Override
    public void print(Writer w) {
        PrintWriter out = getValidPrintWriter(w);
        Map<Object, Object> mm;
        if (sort) {
            mm = new LinkedHashMap<>();
            List<Object> keys = new ArrayList(getModel().keySet());
            if (sort) {
                keys.sort(null);
            }
            for (Object k : keys) {
                Object v = getModel().get(k);
                mm.put(k, v);
            }
        } else {
            mm = getModel();
        }
        if (javaProps) {
            CoreIOUtils.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(mm), w, sort);
        } else {
            printMap(out, "", mm);
        }
    }

//    private String formatValue(Object value) {
//        if (value == null) {
//            return "";
//        }
//        StringBuilder sb = new StringBuilder();
//        String svalue = String.valueOf(value);
//        for (char c : svalue.toCharArray()) {
//            switch (c) {
//                case '\n': {
//                    sb.append("\\n");
//                    break;
//                }
//                default: {
//                    sb.append(c);
//                    break;
//                }
//            }
//        }
//        return sb.toString();
//    }
    private void printMap(PrintWriter out, String prefix, Map<Object, Object> props) {
        int len = 1;
        for (Object extraKey : props.keySet()) {
            int x = ws.io().getTerminalFormat().textLength(stringValue(extraKey));
            if (x > len) {
                len = x;
            }
        }
        boolean first = true;
        for (Map.Entry<Object, Object> e : props.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.println();
            }
            printKeyValue(out, prefix, len, stringValue(e.getKey()), stringValue(e.getValue()));
        }
        out.flush();
    }

//    private Map<String, String> getMultilineMap(String key, Object value) {
//        String[] a = getMultilineArray(key, value);
//        if (a == null) {
//            return null;
//        }
//        LinkedHashMap<String, String> m = new LinkedHashMap<>();
//        for (int i = 0; i < a.length; i++) {
//            m.put(String.valueOf(i + 1), a[i]);
//        }
//        return m;
//    }

//    private String[] getMultilineArray(String key, Object value) {
//        String sep = getMultilineSeparator(key);
//        if (sep == null) {
//            return null;
//        }
//        String[] vv = stringValue(value).split(sep);
//        if (vv.length == 0 || vv.length == 1) {
//            return null;
//        }
//        return vv;
//    }

    private String getMultilineSeparator(String key) {
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(PrintWriter out, String prefix, int len, String key, String value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(PrintWriter out, String prefix, int len, String fancySep, String key, String value) {
        if (prefix == null) {
            prefix = "";
        }
        String ekey = ws.io().getTerminalFormat().escapeText(key);
        int delta = key.length() - ekey.length();
        String formattedKey = compact ? key : CoreStringUtils.alignLeft(ekey, len - delta);
        if (fancySep != null) {
            String cc = compact ? key : CoreStringUtils.alignLeft("", len + 3);
            String space = prefix + "==%s==%s%s";
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.printf(prefix + "==%N==%s", formattedKey, separator);
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {
                        out.printf(prefix + "==%N==%s%s", formattedKey, separator, s);
                    } else {
                        out.println();
                        out.printf(space, cc, separator, s);
                    }
                    //                    }
                }
            }
        } else {
            out.printf(prefix + "==%N==%s%s", formattedKey, separator, value);
        }
    }

    private String stringValue(Object o) {
        if (escapeText) {
            return CoreCommonUtils.stringValueFormatted(o, ws, getValidSession());
        } else {
            return String.valueOf(o);
        }
    }
}
