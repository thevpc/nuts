package net.thevpc.nuts.runtime.standalone.format.props;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElementEntry;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.ObjectOutputFormatWriterHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNPropertiesFormat extends DefaultFormatBase<NPropertiesFormat> implements NPropertiesFormat {

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

    public DefaultNPropertiesFormat(NSession session) {
        super(session, "props-format");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a;
        if ((a = cmdLine.nextEntry(OPTION_MULTILINE_PROPERTY).orNull()) != null) {
            NArg i = NArg.of(a.getStringValue().get(getSession()));
            if (i.isActive()) {
                addMultilineProperty(i.getKey().asString().get(getSession()), i.getStringValue().get(getSession()));
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--compact").orNull()) != null) {
            if (a.isActive()) {
                this.compact = a.getBooleanValue().get(getSession());
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--props").orNull()) != null) {
            if (a.isActive()) {
                this.javaProps = a.getBooleanValue().get(getSession());
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--escape-text").orNull()) != null) {
            if (a.isActive()) {
                this.escapeText = a.getBooleanValue().get(getSession());
            }
            return true;
        }
        return false;
    }

    public DefaultNPropertiesFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    public Map buildModel() {
        Object value = NElements.of(getSession()).setIndestructibleFormat().destruct(getValue());
        LinkedHashMap<NString, NString> map = new LinkedHashMap<>();
        fillMap(NString.of((rootName==null?"":rootName),getSession()), value, map);
        return map;
    }

    private void fillMap(NString entryKey, Object entryValue, Map<NString, NString> map) {
        if(entryValue instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) entryValue).entrySet()) {
                Object k = entry.getKey();
                NString ns = entryKey.isEmpty() ? stringValue(k) : entryKey.builder().append(".").append(stringValue(k));
                Object v = entry.getValue();
                fillMap(ns, v, map);
            }
        }else if(entryValue instanceof NObjectElement){
            for (NElementEntry entry : ((NObjectElement) entryValue)) {
                Object k = entry.getKey();
                NString ns= entryKey.isEmpty()?stringValue(k): entryKey.builder().append(".").append(stringValue(k));
                Object v = entry.getValue();
                fillMap(ns, v,map);
            }
        }else if(entryValue instanceof List){
            List<Object> objects = (List<Object>) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NString ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else if(entryValue instanceof NArrayElement){
            NArrayElement objects = (NArrayElement) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NString ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else {
            if(entryValue ==null && omitNull){
                //do nothing;
            }else {
                if(!entryKey.isEmpty()) {
                    map.put(entryKey, stringValue(entryValue));
                }else{
                    map.put(NString.of("value",getSession()), stringValue(entryValue));
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

    public DefaultNPropertiesFormat setSeparator(String separator) {
        this.separator = separator;
        return this;
    }

    public DefaultNPropertiesFormat setSorted(boolean sort) {
        this.sorted = sort;
        return this;
    }

    @Override
    public void print(NPrintStream w) {
        checkSession();
        NPrintStream out = getValidPrintStream(w);
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
            printMap(out, NTexts.of(getSession()).ofBlank(), mm);
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
    private void printMap(NPrintStream out, NString prefix, Map<Object, Object> props) {
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

    private String getMultilineSeparator(NString key) {
        String sep = multilineProperties.get(key.filteredText());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(NPrintStream out, NString prefix, int len, NString key, NString value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(NPrintStream out, NString prefix, int len, String fancySep, NString key, NString value) {
        NTexts txt = NTexts.of(getSession());
        if (prefix == null) {
            prefix = txt.ofBlank();
        }
        NString formattedKey = compact ? key
                : txt.ofBuilder().append(key).append(CoreStringUtils.fillString(' ', len - key.textLength()));
        if (fancySep != null) {
            NString cc = compact ? key : txt.ofPlain(NStringUtils.formatAlign("", len + 3, NPositionType.FIRST));
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
                            if(out.isNtf()) {
                                out.print(NConstants.Ntf.SILENT);
                            }
                        }
                        out.print(formattedKey);
                        if (separator.isEmpty() || separator.startsWith("#")) {
                            if(out.isNtf()) {
                                out.print(NConstants.Ntf.SILENT);
                            }
                        }
                        out.print(separator);
                        out.print(s);
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
            if (prefix.isEmpty() || prefix.toString().endsWith("#")) {
                if(out.isNtf()) {
                    out.print(NConstants.Ntf.SILENT);
                }
            }
            out.print(txt.ofStyled(formattedKey, NTextStyle.primary3()));
            if (separator.isEmpty() || separator.startsWith("#")) {
                if(out.isNtf()) {
                    out.print(NConstants.Ntf.SILENT);
                }
            }
            out.print(separator);
            out.print(value);
        }
    }

    private NString stringValue(Object o) {
        if (escapeText) {
            return NTextUtils.stringValueFormatted(o, escapeText, getSession());
        } else {
            return NTexts.of(getSession()).ofText(o);
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NPropertiesFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NSupported.DEFAULT_SUPPORT;
    }
}
