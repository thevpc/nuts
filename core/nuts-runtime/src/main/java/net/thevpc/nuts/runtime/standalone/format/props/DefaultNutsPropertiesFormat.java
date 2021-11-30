package net.thevpc.nuts.runtime.standalone.format.props;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.ObjectOutputFormatWriterHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.text.util.NutsTextUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsPropertiesFormat extends DefaultFormatBase<NutsPropertiesFormat> implements NutsPropertiesFormat {

    public static final String OPTION_MULTILINE_PROPERTY = "--multiline-property";
    private boolean sorted;
    private boolean compact;
    private boolean javaProps;
    private String rootName = "";
    private final boolean omitNull = true;
    private boolean escapeText = true;
    private String separator = " = ";
    private Object value;
    private Map<String, String> multilineProperties = new HashMap<>();

    public DefaultNutsPropertiesFormat(NutsSession session) {
        super(session, "props-format");
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument a;
        if ((a = commandLine.nextString(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = NutsArgument.of(a.getValue().getString(),getSession());
            if (i.isActive()) {
                addMultilineProperty(i.getKey().getString(), i.getValue().getString());
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--compact")) != null) {
            if (a.isActive()) {
                this.compact = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--props")) != null) {
            if (a.isActive()) {
                this.javaProps = a.getBooleanValue();
            }
            return true;
        } else if ((a = commandLine.nextBoolean("--escape-text")) != null) {
            if (a.isActive()) {
                this.escapeText = a.getBooleanValue();
            }
            return true;
        }
        return false;
    }

    public DefaultNutsPropertiesFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    public Map buildModel() {
        Object value = NutsElements.of(getSession()).setIndestructibleFormat().destruct(getValue());
        LinkedHashMap<NutsString, NutsString> map = new LinkedHashMap<>();
        fillMap(NutsString.of((rootName==null?"":rootName),getSession()), value, map);
        return map;
    }

    private void fillMap(NutsString entryKey, Object entryValue, Map<NutsString, NutsString> map) {
        if(entryValue instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) entryValue).entrySet()) {
                Object k = entry.getKey();
                NutsString ns = entryKey.isEmpty() ? stringValue(k) : entryKey.builder().append(".").append(stringValue(k));
                Object v = entry.getValue();
                fillMap(ns, v, map);
            }
        }else if(entryValue instanceof NutsObjectElement){
            for (NutsElementEntry entry : ((NutsObjectElement) entryValue)) {
                Object k = entry.getKey();
                NutsString ns= entryKey.isEmpty()?stringValue(k): entryKey.builder().append(".").append(stringValue(k));
                Object v = entry.getValue();
                fillMap(ns, v,map);
            }
        }else if(entryValue instanceof List){
            List<Object> objects = (List<Object>) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NutsString ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else if(entryValue instanceof NutsArrayElement){
            NutsArrayElement objects = (NutsArrayElement) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NutsString ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else {
            if(entryValue ==null && omitNull){
                //do nothing;
            }else {
                if(!entryKey.isEmpty()) {
                    map.put(entryKey, stringValue(entryValue));
                }else{
                    map.put(NutsString.of("value",getSession()), stringValue(entryValue));
                }
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

    public String getSeparator() {
        return separator;
    }

    public DefaultNutsPropertiesFormat setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public DefaultNutsPropertiesFormat setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public void print(NutsPrintStream w) {
        checkSession();
        NutsPrintStream out = getValidPrintStream(w);
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
            CoreIOUtils.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(mm), w.asPrintStream(), sorted,getSession());
        } else {
            printMap(out, NutsTexts.of(getSession()).ofBlank(), mm);
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
    private void printMap(NutsPrintStream out, NutsString prefix, Map<Object, Object> props) {
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

    private void printKeyValue(NutsPrintStream out, NutsString prefix, int len, NutsString key, NutsString value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(NutsPrintStream out, NutsString prefix, int len, String fancySep, NutsString key, NutsString value) {
        NutsTexts txt = NutsTexts.of(getSession());
        if (prefix == null) {
            prefix = txt.ofBlank();
        }
        NutsString formattedKey = compact ? key
                : txt.builder().append(key).append(CoreStringUtils.fillString(' ', len - key.textLength()));
        if (fancySep != null) {
            NutsString cc = compact ? key : txt.ofPlain(CoreStringUtils.alignLeft("", len + 3));
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
            out.printf("%s", txt.ofStyled(formattedKey, NutsTextStyle.primary3()));
            if (separator.isEmpty() || separator.startsWith("#")) {
                out.print(NutsConstants.Ntf.SILENT);
            }
            out.print(separator);
            out.print(value);
        }
    }

    private NutsString stringValue(Object o) {
        if (escapeText) {
            return NutsTextUtils.stringValueFormatted(o, escapeText, getSession());
        } else {
            return NutsTexts.of(getSession()).toText(o);
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

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
