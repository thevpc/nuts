package net.thevpc.nuts.runtime.standalone.format.props;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.format.NPropertiesFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.ObjectOutputFormatWriterHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.util.NPropsTransformer;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
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

    public DefaultNPropertiesFormat(NWorkspace workspace) {
        super("props-format");
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a;
        if ((a = cmdLine.nextEntry(OPTION_MULTILINE_PROPERTY).orNull()) != null) {
            NArg i = NArg.of(a.getStringValue().get());
            if (i.isUncommented()) {
                addMultilineProperty(i.getKey().asString().get(), i.getStringValue().get());
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--compact").orNull()) != null) {
            if (a.isUncommented()) {
                this.compact = a.getBooleanValue().get();
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--props").orNull()) != null) {
            if (a.isUncommented()) {
                this.javaProps = a.getBooleanValue().get();
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--escape-text").orNull()) != null) {
            if (a.isUncommented()) {
                this.escapeText = a.getBooleanValue().get();
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
        NElements e = NElements.of();
        Object value = e.destruct(getValue());
        LinkedHashMap<NText, NText> map = new LinkedHashMap<>();
        fillMap(NText.of((rootName==null?"":rootName)), value, map);
        return map;
    }

    private void fillMap(NText entryKey, Object entryValue, Map<NText, NText> map) {
        if(entryValue instanceof Map) {
            for (Map.Entry<Object, Object> entry : ((Map<Object, Object>) entryValue).entrySet()) {
                Object k = entry.getKey();
                NText ns = entryKey.isEmpty() ? stringValue(k) : entryKey.builder().append(".").append(stringValue(k));
                Object v = entry.getValue();
                fillMap(ns, v, map);
            }
        }else if(entryValue instanceof NObjectElement){
            int i=0;
            for (NElement item : ((NObjectElement) entryValue)) {
                if(item instanceof NPairElement){
                    NPairElement entry=(NPairElement) item;
                    Object k = entry.key();
                    NText ns= entryKey.isEmpty()?stringValue(k): entryKey.builder().append(".").append(stringValue(k));
                    Object v = entry.value();
                    fillMap(ns, v,map);
                }else {
                    NText ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                    fillMap(ns, item, map);
                    i++;
                }
            }
        }else if(entryValue instanceof NArrayElement){
            NArrayElement objects = (NArrayElement) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NText ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else if(entryValue instanceof List){
            List<Object> objects = (List<Object>) entryValue;
            for (int i = 0; i < objects.size(); i++) {
                NText ns = entryKey.builder().append("[").append(stringValue(i+1)).append("]");
                fillMap(ns, objects.get(i), map);
            }
        }else {
            if(entryValue ==null && omitNull){
                //do nothing;
            }else {
                if(!entryKey.isEmpty()) {
                    map.put(entryKey, stringValue(entryValue));
                }else{
                    map.put(NText.ofPlain("value"), stringValue(entryValue));
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
            NPropsTransformer.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(mm), w.asPrintStream(), sorted);
        } else {
            printMap(out, NText.ofBlank(), mm);
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
    private void printMap(NPrintStream out, NText prefix, Map<Object, Object> props) {
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

    private String getMultilineSeparator(NText key) {
        String sep = multilineProperties.get(key.filteredText());
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        return sep;
    }

    private void printKeyValue(NPrintStream out, NText prefix, int len, NText key, NText value) {
        printKeyValue(out, prefix, len, getMultilineSeparator(key), key, value);
    }

    private void printKeyValue(NPrintStream out, NText prefix, int len, String fancySep, NText key, NText value) {
        NTexts txt = NTexts.of();
        if (prefix == null) {
            prefix = txt.ofBlank();
        }
        NText formattedKey = compact ? key
                : txt.ofBuilder().append(key).append(CoreStringUtils.fillString(' ', len - key.textLength()));
        if (fancySep != null) {
            NText cc = compact ? key : txt.ofPlain(NStringUtils.formatAlign("", len + 3, NPositionType.FIRST));
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

    private NText stringValue(Object o) {
        if (escapeText) {
            return NTextUtils.stringValueFormatted(o, escapeText);
        } else {
            return NText.of(o);
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
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
