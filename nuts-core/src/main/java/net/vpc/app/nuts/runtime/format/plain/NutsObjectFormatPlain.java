/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.plain;

import java.io.IOException;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreCommonUtils;

import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.NutsObjectFormatBase;
import net.vpc.app.nuts.runtime.format.props.NutsObjectFormatProps;
import net.vpc.app.nuts.runtime.format.table.NutsObjectFormatTable;
import net.vpc.app.nuts.runtime.format.xml.NutsXmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatPlain extends NutsObjectFormatBase {

    private final NutsWorkspace ws;
    private final String rootName = "";
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatPlain(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.PLAIN.id() + "-format");
        this.ws = ws;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsArgument n = commandLine.peek();
        if (n != null) {
            NutsArgument a;
            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
                NutsArgument i = a.getArgumentValue();
                extraConfig.add(a.getString());
                addMultilineProperty(i.getStringKey(), i.getStringValue());
            } else {
                extraConfig.add(commandLine.next().getString());
            }
            return true;
        }
        return false;
    }

    private String getFormattedPrimitiveValue(NutsElement value) {
        switch (value.type()) {
            default: {
                throw new NutsUnsupportedArgumentException(ws, value.type().toString());
            }
        }
    }

    @Override
    public void print(Writer w) {
        Object value = getValue();
        if (value instanceof NutsTableModel) {
            ws.table().setModel(((NutsTableModel) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof NutsTreeModel) {
            ws.tree().setModel(((NutsTreeModel) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
//        } else if (value instanceof Map) {
//            ws.props().setModel(((Map) value)).configure(true, extraConfig.toArray(new String[0])).print(w);
        } else if (value instanceof org.w3c.dom.Document) {
            try {
                NutsXmlUtils.writeDocument((org.w3c.dom.Document) value, new StreamResult(w), false,true);
            } catch (TransformerException ex) {
                throw new UncheckedIOException(new IOException(ex));
            }
        } else if (value instanceof org.w3c.dom.Element) {
            try {
                Element elem = (org.w3c.dom.Element) value;
                Document doc = NutsXmlUtils.createDocument();
                doc.appendChild(doc.importNode(elem, true));
                NutsXmlUtils.writeDocument(doc, new StreamResult(w), false,false);
            } catch (TransformerException|ParserConfigurationException ex) {
                throw new UncheckedIOException(new IOException(ex));
            }
        } else {
            printElement(w, ws.element().toElement(value));
        }
    }

    public void printElement(Writer w, NutsElement value) {
        PrintWriter out = getValidPrintWriter(w);
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
                out.print(ws.io().getTerminalFormat().escapeText(value.primitive().getDate().toString()));
                out.flush();
                break;
            }
            case NULL: {
                break;
            }
            case ARRAY: {
                NutsObjectFormatTable table = new NutsObjectFormatTable(ws);
                table.configure(true, "--no-header", "--border=spaces");
                table.value(value).print(w);
                break;
            }
            case OBJECT: {
                NutsObjectFormatProps tree = new NutsObjectFormatProps(ws);
                tree.configure(true, extraConfig.toArray(new String[0]));
                tree.value(value).print(w);
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException(ws, value.type().toString());
            }
        }
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    private String formatObject(Object any) {
        return CoreCommonUtils.stringValueFormatted(any, false, getValidSession());
    }
}
