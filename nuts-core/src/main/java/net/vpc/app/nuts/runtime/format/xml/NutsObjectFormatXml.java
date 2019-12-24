/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.runtime.format.xml;

import net.vpc.app.nuts.*;
import java.io.Writer;
import java.util.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.vpc.app.nuts.runtime.format.props.DefaultPropertiesFormat;
import net.vpc.app.nuts.runtime.format.NutsObjectFormatBase;
import org.w3c.dom.Document;

/**
 *
 * @author vpc
 */
public class NutsObjectFormatXml extends NutsObjectFormatBase {

    private final NutsWorkspace ws;
    private final List<String> extraConfig = new ArrayList<>();
    private final Map<String, String> multilineProperties = new HashMap<>();

    public NutsObjectFormatXml(NutsWorkspace ws) {
        super(ws, NutsOutputFormat.XML.id() + "-format");
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

    @Override
    public void print(Writer w) {
        try {
            Document document = NutsXmlUtils.createDocument();
            document.appendChild(ws.xml().toXmlElement(getValue(), document));
            NutsXmlUtils.writeDocument(document, new StreamResult(w), false,true);
        } catch (TransformerException | ParserConfigurationException ex) {
            throw new NutsException(ws, ex);
        }
    }

    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }
}
