package net.thevpc.nuts.runtime.core.format.props;

import java.io.PrintStream;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.ObjectOutputFormatWriterHelper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

public class DefaultPropertiesFormat extends DefaultFormatBase<NutsPropertiesFormat> implements NutsPropertiesFormat {

    public static final String OPTION_MULTILINE_PROPERTY = "--multiline-property";
    private boolean sorted;
    private boolean compact;
    private boolean javaProps;
    private final String rootName = "";
    private final boolean omitNull = true;
    private boolean escapeText = true;
    private String separator = " = ";
    private Object value;
    private Map<String, String> multilineProperties = new HashMap<>();

    public DefaultPropertiesFormat(NutsWorkspace ws) {
        super(ws, "props-format");
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument a;
        if ((a = commandLine.nextString(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = a.getArgumentValue();
            if (i.isEnabled()) {
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--compact")) != null) {
            if (a.isEnabled()) {
                this.compact = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--props")) != null) {
            if (a.isEnabled()) {
                this.javaProps = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--escape-text")) != null) {
            if (a.isEnabled()) {
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

    public Map buildModel() {
        Object value = getValue();
        if (value instanceof Map) {
            return (Map) value;
        }
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        fillMap(getSession().getWorkspace().elem()
                .toElement(value), map, rootName);
        return map;
    }

    private void fillMap(NutsElement e, Map<String, Object> map, String prefix) {
        switch (e.type()) {
            case NULL: {
                if (omitNull) {
                    //do nothing;
                } else {
                    String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
                    map.put(k, stringValue(e.asPrimitive().getValue()));
                }
                break;
            }
            case BOOLEAN:
            case INSTANT:
            case INTEGER:
            case FLOAT:
            case STRING: {
                String k = (CoreStringUtils.isBlank(prefix)) ? "value" : prefix;
                map.put(k, stringValue(e.asPrimitive().getValue()));
                break;
            }
            case ARRAY: {
                int index = 1;
                for (NutsElement datum : e.asArray().children()) {
                    String k = (CoreStringUtils.isBlank(prefix)) ? String.valueOf(index) : (prefix + "." + String.valueOf(index));
                    fillMap(datum, map, k);
                    index++;
                }
                break;
            }
            case OBJECT: {
                for (NutsElementEntry datum : e.asObject().children()) {
                    NutsElement k = datum.getKey();
                    if (!k.isString()) {
                        k = getSession().getWorkspace().elem()
                                .setSession(getSession())
                                .forString(
                                k.toString()
                        );
                    }
                    String ks=k.asPrimitive().getString();
                    String k2 = (CoreStringUtils.isBlank(prefix)) ? ks : (prefix + "." + ks);
                    fillMap(datum.getValue(), map, k2);
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), e.type().name());
            }
        }
    }

    @Override
    public Map getModel() {
        return buildModel();
    }

    public boolean isSorted() {
        return sorted;
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

    public DefaultPropertiesFormat setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public void print(PrintStream w) {
        checkSession();
        PrintStream out = getValidPrintStream(w);
        Map<Object, Object> mm;
        Map model = buildModel();
        if (sorted) {
            mm = new LinkedHashMap<>();
            List<Object> keys = new ArrayList(model.keySet());
            if (sorted) {
                keys.sort(null);
            }
            for (Object k : keys) {
                Object v = model.get(k);
                mm.put(k, v);
            }
        } else {
            mm = model;
        }
        if (javaProps) {
            CoreIOUtils.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(mm), w, sorted);
        } else {
            printMap(out, getSession().getWorkspace().text().forBlank(), mm);
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
    private void printMap(PrintStream out, NutsString prefix, Map<Object, Object> props) {
        int len = 1;
        for (Object extraKey : props.keySet()) {
            int x = stringValue(extraKey).textLength();
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

    private String getMultilineSeparator(NutsString key) {
        String sep = multilineProperties.get(key.filteredText());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(PrintStream out, NutsString prefix, int len, NutsString key, NutsString value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(PrintStream out, NutsString prefix, int len, String fancySep, NutsString key, NutsString value) {
        NutsTextManager txt = getSession().getWorkspace().text();
        if (prefix == null) {
            prefix = txt.forBlank();
        }
        NutsString formattedKey = compact ? key
                : txt.builder().append(key).append(CoreStringUtils.fillString(' ', len - key.textLength()));
        if (fancySep != null) {
            NutsString cc = compact ? key : txt.forPlain(CoreStringUtils.alignLeft("", len + 3));
            String[] split = value.toString().split(fancySep);
            if (split.length == 0) {
                out.print(prefix);
                out.print(formattedKey);
                out.print(separator);
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {
                        out.print(prefix);
                        if (prefix.isEmpty() || prefix.toString().endsWith("#")) {
                            out.print(NutsConstants.Ntf.SILENT);
                        }
                        out.printf("%s", formattedKey);
                        if (separator.isEmpty() || separator.startsWith("#")) {
                            out.print(NutsConstants.Ntf.SILENT);
                        }
                        out.print(separator);
                        out.print(s);
                    } else {
                        out.println();
                        out.printf("%s", cc);
                        out.print(s);
                    }
                    //                    }
                }
            }
        } else {
            out.print(prefix);
            if (prefix.isEmpty() || prefix.toString().endsWith("#")) {
                out.print(NutsConstants.Ntf.SILENT);
            }
            out.printf("%s", txt.forStyled(formattedKey, NutsTextStyle.primary(3)));
            if (separator.isEmpty() || separator.startsWith("#")) {
                out.print(NutsConstants.Ntf.SILENT);
            }
            out.print(separator);
            out.print(value);
        }
    }

    private NutsString stringValue(Object o) {
        if (escapeText) {
            return CoreCommonUtils.stringValueFormatted(o, escapeText, getSession());
        } else {
            return getSession().getWorkspace().text().forPlain(String.valueOf(o));
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsPropertiesFormat setValue(Object value) {
        this.value = value;
        return this;
    }
}
