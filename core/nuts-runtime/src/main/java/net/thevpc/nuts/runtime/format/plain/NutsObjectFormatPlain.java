/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format.plain;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;
import net.thevpc.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.format.xml.NutsXmlUtils;
import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vpc
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
                throw new NutsUnsupportedArgumentException(getWorkspace(), value.type().toString());
            }
        }
    }

    @Override
    public void print(PrintStream w) {
        Object value = getValue();
        if (value instanceof NutsTableModel) {
            getWorkspace().formats().table().setModel(((NutsTableModel) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NutsTreeModel) {
            getWorkspace().formats().tree().setValue(value).configure(true, extraConfig.toArray(new String[0])).print(w);
//        } else if (value instanceof Map) {
//            ws.props().setModel(((Map) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof org.w3c.dom.Document) {
            try {
                NutsXmlUtils.writeDocument((org.w3c.dom.Document) value, new StreamResult(w), false, true);
            } catch (TransformerException ex) {
                throw new NutsIOException(getWorkspace(),new IOException(ex));
            }
        } else if (value instanceof org.w3c.dom.Element) {
            try {
                Element elem = (org.w3c.dom.Element) value;
                Document doc = NutsXmlUtils.createDocument(getWorkspace());
                doc.appendChild(doc.importNode(elem, true));
                NutsXmlUtils.writeDocument(doc, new StreamResult(w), false, false);
            } catch (TransformerException | ParserConfigurationException ex) {
                throw new NutsIOException(getWorkspace(),new IOException(ex));
            }
        } else {
            printElement(w, getWorkspace().formats().element().convert(value,NutsElement.class));
        }
    }

    public void printElement(PrintStream w, NutsElement value) {
        PrintStream out = getValidPrintStream(w);
        switch (value.type()) {
            case STRING: {
                out.print(value.primitive().getString());
                out.flush();
                break;
            }
            case BOOLEAN: {
                out.print(value.primitive().getBoolean());
                out.flush();
                break;
            }
            case INTEGER:
            case FLOAT: {
                out.print(value.primitive().getNumber());
                out.flush();
                break;
            }
            case DATE: {
                out.print(getWorkspace().formats().text().escapeText(value.primitive().getDate().toString()));
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
                throw new NutsUnsupportedArgumentException(getWorkspace(), value.type().toString());
            }
        }
    }

    private String formatObject(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false, getValidSession());
    }
}
