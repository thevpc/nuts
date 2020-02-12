package net.vpc.app.nuts.runtime.format.props;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.*;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.fprint.ExtendedFormatAwarePrintWriter;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.format.DefaultFormatBase;
import net.vpc.app.nuts.runtime.format.ObjectOutputFormatWriterHelper;

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
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument a;
        if ((a = commandLine.nextString(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = a.getArgumentValue();
            if(i.isEnabled()) {
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--compact")) != null) {
            if(a.isEnabled()) {
                this.compact = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--props")) != null) {
            if(a.isEnabled()) {
                this.javaProps = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--escape-text")) != null) {
            if(a.isEnabled()) {
                this.escapeText = a.getBooleanValue();
            }
            return true;
        }
        return false;
    }

    public DefaultPropertiesFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    @Override
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

    @Override
    public void print(PrintStream w) {
        PrintStream out = getValidPrintStream(w);
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
    private void printMap(PrintStream out, String prefix, Map<Object, Object> props) {
        int len = 1;
        for (Object extraKey : props.keySet()) {
            int x = getWorkspace().io().getTerminalFormat().textLength(stringValue(extraKey));
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

    private String getMultilineSeparator(String key) {
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(PrintStream out, String prefix, int len, String key, String value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(PrintStream out, String prefix, int len, String fancySep, String key, String value) {
        if (prefix == null) {
            prefix = "";
        }
        String ekey = getWorkspace().io().getTerminalFormat().escapeText(key);
        int delta = key.length() - ekey.length();
        String formattedKey = compact ? key : CoreStringUtils.alignLeft(ekey, len - delta);
        if (fancySep != null) {
            String cc = compact ? key : CoreStringUtils.alignLeft("", len+3);
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(prefix);
                out.print(formattedKey);
                out.print(separator);
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {
                        out.print(prefix);
                        out.print("=="+formattedKey+"==");
                        out.print(separator);
                        out.print( s);
                    } else {
                        out.println();
                        out.print(cc);
                        out.print(s);
                    }
                    //                    }
                }
            }
        } else {
            out.print(prefix);
            out.print("=="+formattedKey+"==");
            out.print(separator);
            out.print(value);
        }
    }

    private String stringValue(Object o) {
        if (escapeText) {
            return CoreCommonUtils.stringValueFormatted(o, escapeText,getValidSession());
        } else {
            return String.valueOf(o);
        }
    }
}
