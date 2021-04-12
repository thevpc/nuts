/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.NutsObjectFormatBase;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.xml.NutsXmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.transform.stream.StreamResult;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class NutsObjectFormatPlain extends NutsObjectFormatBase {

    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatPlain(NutsWorkspace ws) {
        super(ws, NutsContentType.PLAIN.id() + "-format");
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            boolean enabled = n.isEnabled();
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                if (enabled) {
                    NutsArgument i = a.getArgumentValue();
                    extraConfig.add(a.getString());
                    addMultilineProperty(i.getStringKey(), i.getStringValue());
                }
            } else {
                a = commandLine.next();
                if (!a.isOption() || a.isEnabled()) {
                    extraConfig.add(a.getString());
                }
            }
            return true;
        }
        return false;
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String getFormattedPrimitiveValue(NutsElement value) {
        switch (value.type()) {
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), value.type().toString());
            }
        }
    }

    @Override
    public void print(PrintStream w) {
        checkSession();
        Object value = getValue();
        if (value instanceof NutsTableModel) {
            getWorkspace().formats().table().setModel(((NutsTableModel) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NutsTreeModel) {
            getWorkspace().formats().tree().setValue(value).configure(true, extraConfig.toArray(new String[0])).print(w);
//        } else if (value instanceof Map) {
//            ws.props().setModel(((Map) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof org.w3c.dom.Document) {
            NutsXmlUtils.writeDocument((org.w3c.dom.Document) value, new StreamResult(w), false, true, getSession());
        } else if (value instanceof org.w3c.dom.Element) {
            Element elem = (org.w3c.dom.Element) value;
            Document doc = NutsXmlUtils.createDocument(getSession());
            doc.appendChild(doc.importNode(elem, true));
            NutsXmlUtils.writeDocument(doc, new StreamResult(w), false, false, getSession());
        } else {
            printElement(w, getWorkspace().formats().element()
                    .setSession(getSession())
                    .convert(value, NutsElement.class));
        }
    }

    public void printElement(PrintStream w, NutsElement value) {
        PrintStream out = getValidPrintStream(w);
        switch (value.type()) {
            case STRING: {
                out.print(value.asPrimitive().getString());
                out.flush();
                break;
            }
            case BOOLEAN: {
                out.print(value.asPrimitive().getBoolean());
                out.flush();
                break;
            }
            case INTEGER:
            case FLOAT: {
                out.print(value.asPrimitive().getNumber());
                out.flush();
                break;
            }
            case INSTANT: {
                out.print(getWorkspace().formats().text().plain(value.asPrimitive().getInstant().toString()).toString());
                out.flush();
                break;
            }
            case NULL: {
                break;
            }
            case ARRAY: {
                NutsTableFormat table = getWorkspace().formats().table();
                table.configure(true, "--no-header", "--border=spaces");
                table.setValue(value).print(w);
                break;
            }
            case OBJECT: {
                NutsTreeFormat tree = getWorkspace().formats().tree();
                tree.configure(true, extraConfig.toArray(new String[0]));
                tree.setValue(value).print(w);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(getSession(), value.type().toString());
            }
        }
    }

//    private String formatObject(Object any) {
//        return CoreCommonUtils.stringValueFormatted(any, false, getValidSession());
//    }
}
