/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultNPropertiesFormat;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.stream.StreamResult;
import java.util.*;

/**
 * @author thevpc
 */
public class NFormatPlain extends DefaultFormatBase<NContentTypeFormat> implements NContentTypeFormat {

    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();
    private Object value;
    private boolean compact;

    public NFormatPlain(NWorkspace workspace) {
        super(workspace, NContentType.PLAIN.id() + "-format");
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NContentTypeFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session=workspace.currentSession();
        NArg n = cmdLine.peek().orNull();
        if (n != null) {
            NArg a;
            boolean enabled = n.isActive();
            if ((a = cmdLine.nextEntry(DefaultNPropertiesFormat.OPTION_MULTILINE_PROPERTY).orNull()) != null) {
                if (enabled) {
                    NArg i = NArg.of(a.getStringValue().get());
                    extraConfig.add(a.asString().get());
                    addMultilineProperty(i.key(), i.getStringValue().get());
                }
            } else {
                a = cmdLine.next().get();
                if (!a.isOption() || a.isActive()) {
                    extraConfig.add(a.asString().get());
                }
            }
            return true;
        }
        return false;
    }

    public NContentTypeFormat addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String getFormattedPrimitiveValue(NElement value) {
        switch (value.type()) {
            default: {
                NSession session=workspace.currentSession();
                throw new NUnsupportedArgumentException(NMsg.ofC("invalid element type: %s", value.type()));
            }
        }
    }

    @Override
    public void print(NPrintStream w) {
        NSession session=workspace.currentSession();
        Object value = getValue();
        if (value instanceof NTableModel) {
            NTableFormat.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NTreeModel) {
            NTreeFormat.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof Properties) {
            NPropertiesFormat.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NElement) {
            NElements.of().setValue(value).setNtf(isNtf())
                    .setCompact(isCompact())
                    .configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof org.w3c.dom.Document) {
            XmlUtils.writeDocument((org.w3c.dom.Document) value, new StreamResult(w.asPrintStream()), false, true);
        } else if (value instanceof org.w3c.dom.Element) {
            Element elem = (org.w3c.dom.Element) value;
            Document doc = XmlUtils.createDocument();
            doc.appendChild(doc.importNode(elem, true));
            XmlUtils.writeDocument(doc, new StreamResult(w.asPrintStream()), false, false);
        } else {
            NElements element = NElements.of();
            Object newVal = element.setNtf(true).setIndestructibleFormat().destruct(value);
            Flags f=new Flags();
            collectFlags(newVal,f,300);
            if(f.map){
                if(f.msg || f.formattable){
                    NTreeFormat.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else if(f.elems){
                    NElements.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else {
                    //defaults to elements
                    NElements.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }
            }else if(f.list){
                if(f.msg || f.formattable){
                    NTableFormat.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                    //table.configure(true, "--no-header", "--border=spaces");
                }else if(f.elems){
                    NElements.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }else {
                    //defaults to elements
                    NElements.of().setValue(value).setNtf(isNtf()).configure(true, extraConfig.toArray(new String[0])).print(w);
                }
            }else{
                NPrintStream out = getValidPrintStream(w);
                out.print(NTexts.of().ofText(value));
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
        } else if (value instanceof NElement) {
            flags.elems = true;
            if(value instanceof NObjectElement){
                flags.map = true;
            }else if(value instanceof NArrayElement){
                flags.list = true;
            }
        } else if (value instanceof NMsg) {
            flags.msg = true;
        } else {
            flags.primitives = true;
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NFormatPlain setNtf(boolean ntf) {
        return (NFormatPlain) super.setNtf(ntf);
    }

    public boolean isCompact() {
        return compact;
    }

    public NFormatPlain setCompact(boolean compact) {
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
