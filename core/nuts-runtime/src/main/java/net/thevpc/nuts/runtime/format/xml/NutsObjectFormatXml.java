///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package net.thevpc.nuts.runtime.format.xml;
//
//import net.thevpc.nuts.*;
//
//import java.io.PrintStream;
//import java.util.*;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.stream.StreamResult;
//import net.thevpc.nuts.runtime.format.props.DefaultPropertiesFormat;
//import net.thevpc.nuts.runtime.format.NutsObjectFormatBase;
//import org.w3c.dom.Document;
//
///**
// *
// * @author thevpc
// */
//public class NutsObjectFormatXml extends NutsObjectFormatBase {
//
//    private final List<String> extraConfig = new ArrayList<>();
//    private final Map<String, String> multilineProperties = new HashMap<>();
//
//    public NutsObjectFormatXml(NutsWorkspace ws) {
//        super(ws, NutsContentType.XML.id() + "-format");
//    }
//
//    @Override
//    public boolean configureFirst(NutsCommandLine commandLine) {
//        NutsArgument n = commandLine.peek();
//        if (n != null) {
//            NutsArgument a;
//            if ((a = commandLine.nextString(DefaultPropertiesFormat.OPTION_MULTILINE_PROPERTY)) != null) {
//                if(a.isEnabled()) {
//                    NutsArgument i = a.getArgumentValue();
//                    extraConfig.add(a.getString());
//                    addMultilineProperty(i.getStringKey(), i.getStringValue());
//                }
//            } else {
//                a = commandLine.next();
//                if(!a.isOption() || a.isEnabled()) {
//                    extraConfig.add(a.getString());
//                }
//            }
//            return true;
//        }
//        return false;
//    }
//
//    @Override
//    public void print(PrintStream w) {
//        try {
//            Document document = NutsXmlUtils.createDocument(getWorkspace());
//            document.appendChild(getWorkspace().formats().element().toXmlElement(getValue(), document));
//            NutsXmlUtils.writeDocument(document, new StreamResult(w), false,true);
//        } catch (TransformerException | ParserConfigurationException ex) {
//            throw new NutsException(getWorkspace(), ex);
//        }
//    }
//
//    public NutsObjectFormatBase addMultilineProperty(String property, String separator) {
//        multilineProperties.put(property, separator);
//        return this;
//    }
//}
