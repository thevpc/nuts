/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNutsPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.stream.StreamResult;
import java.util.*;

/**
 * @author thevpc
 */
public class NutsFormatPlain extends DefaultFormatBase<NutsContentTypeFormat> implements NutsContentTypeFormat {

    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();
    private Object value;
    private boolean compact;

    public NutsFormatPlain(NutsSession session) {
        super(session, NutsContentType.PLAIN.id() + "-format");
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsContentTypeFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            boolean enabled = n.isActive();
            if ((a = commandLine.nextString(DefaultNutsPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                if (enabled) {
                    NutsArgument i = NutsArgument.of(a.getValue().getString(), getSession());
                    extraConfig.add(a.getString());
                    addMultilineProperty(i.getKey().getString(), i.getValue().getString());
                }
            } else {
                a = commandLine.next();
                if (!a.isOption() || a.isActive()) {
                    extraConfig.add(a.getString());
                }
            }
            return true;
        }
        return false;
    }

    public NutsContentTypeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String getFormattedPrimitiveValue(NutsElement value) {
        switch (value.type()) {
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), NutsMessage.cstyle("invalid element type: %s", value.type()));
            }
        }
    }

    @Override
    public void print(NutsPrintStream w) {
        checkSession();
        Object value = getValue();
        NutsSession session = getSession();
        if (value instanceof NutsTableModel) {
            NutsTableFormat.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NutsTreeModel) {
            NutsTreeFormat.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof Properties) {
            NutsPropertiesFormat.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NutsElement) {
            NutsElements.of(session).setValue(value).setNtf(isNtf())
                    .setCompact(isCompact())
                    .configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof org.w3c.dom.Document) {
            XmlUtils.writeDocument((org.w3c.dom.Document) value, new StreamResult(w.asPrintStream()), false, true, getSession());
        } else if (value instanceof org.w3c.dom.Element) {
            Element elem = (org.w3c.dom.Element) value;
            Document doc = XmlUtils.createDocument(getSession());
            doc.appendChild(doc.importNode(elem, true));
            XmlUtils.writeDocument(doc, new StreamResult(w.asPrintStream()), false, false, getSession());
        } else {
            NutsElements element = NutsElements.of(session);
            Object newVal = element.setNtf(true).setIndestructibleFormat().destruct(value);
            Flags f=new Flags();
            collectFlags(newVal,f,300);
            if(f.map){
                if(f.msg || f.formattable){
                    NutsTreeFormat.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else if(f.elems){
                    NutsElements.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else {
                    //defaults to elements
                    NutsElements.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }
            }else if(f.list){
                if(f.msg || f.formattable){
                    NutsTableFormat.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                    //table.configure(true, "--no-header", "--border=spaces");
                }else if(f.elems){
                    NutsElements.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else {
                    //defaults to elements
                    NutsElements.of(session).setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }
            }else{
                NutsPrintStream out = getValidPrintStream(w);
                out.printf("%s", value);
                out.flush();
            }
        }
    }

    public void collectFlags(Object value, Flags flags, int depth) {
        if (depth < 0) {
            return;
        }
        if (value instanceof Map) {
            flags.map = true;
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
                collectFlags(entry.getKey(), flags, depth - 1);
                collectFlags(entry.getValue(), flags, depth - 1);
            }
        } else if (value instanceof List) {
            flags.list = true;
            Flags f2=new Flags();
            for (Object entry : ((List) value)) {
                collectFlags(entry, f2, depth - 1);
            }
            if(f2.list || f2.map){
                flags.map=true;
            }
            flags.elems|=f2.elems;
            flags.msg|=f2.msg;
            flags.primitives|=f2.primitives;
            flags.formattable|=f2.formattable;
        } else if (value instanceof Formattable) {
            flags.formattable = true;
        } else if (value instanceof NutsElement) {
            flags.elems = true;
            if(value instanceof NutsObjectElement){
                flags.map = true;
            }else if(value instanceof NutsArrayElement){
                flags.list = true;
            }
        } else if (value instanceof NutsMessage) {
            flags.msg = true;
        } else {
            flags.primitives = true;
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsFormatPlain setNtf(boolean ntf) {
        return (NutsFormatPlain) super.setNtf(ntf);
    }

    public boolean isCompact() {
        return compact;
    }

    public NutsFormatPlain setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    private static class Flags {
        boolean elems;
        boolean list;
        boolean map;
        boolean primitives;
        boolean msg;
        boolean formattable;
    }
}
