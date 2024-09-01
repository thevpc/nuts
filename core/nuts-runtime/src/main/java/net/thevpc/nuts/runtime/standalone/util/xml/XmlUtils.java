/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.util.xml;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.function.Predicate;
import java.util.logging.Level;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.thevpc.nuts.*;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NMsg;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 *
 * @author thevpc
 */
public class XmlUtils {

//    public static void print(String name, Object object, long elemIndex, Writer out, boolean compact, boolean headerDeclaration, NutsSession session) {
//        print(name, object, elemIndex, (Object) out, compact, headerDeclaration, session);
//    }
//
//    public static void print(String name, Object object, long elemIndex, PrintStream out, boolean compact, boolean headerDeclaration, NutsSession session) {
//        print(name, object, elemIndex, (Object) out, compact, headerDeclaration, session);
//    }
//
//    private static void print(String name, Object object, long elemIndex, Object out, boolean compact, boolean headerDeclaration, NutsSession session) {
//        try {
//            Document document = XmlUtils.createDocument(session);
//            String rootName = name;
//            document.appendChild(createElement(NutsBlankable.isBlank(rootName) ? "root" : rootName, object, elemIndex,document, session));
//            StreamResult streamResult = null;
//            if (out instanceof PrintStream) {
//                streamResult = new StreamResult((PrintStream) out);
//            } else {
//                streamResult = new StreamResult((Writer) out);
//            }
//            XmlUtils.writeDocument(document, streamResult, compact,headerDeclaration,session);
//            if (out instanceof PrintStream) {
//                ((PrintStream) out).flush();
//            } else {
//                ((Writer) out).flush();
//            }
//
//        } catch (IOException ex) {
//            throw new NutsIOException(session.getWorkspace(),ex);
//        }
//    }

//    public static Document createDocument(String name, Object object, NutsSession session) {
//            Document document = createDocument(session);
//            document.appendChild(createElement(NutsBlankable.isBlank(name) ? "root" : name, object, -1,document, session));
//            return document;
//    }

//    public static Element createElement(String name, Object o, long elemIndex, Document document, NutsSession session) {
//        // root element
//        Element elem = document.createElement(createElementName(name));
//        if(elemIndex>=0){
//            elem.setAttribute("index",NutsTextUtils.stringValue(elemIndex));
//        }
//        NutsElement elem2 = NutsElements.of(session).convert(o,NutsElement.class);
//        switch (elem2.type()){
//            case STRING:{
//                elem.setAttribute("type", "string");
//                elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getString()));
//                break;
//            }
//            case FLOAT:{
//                Number n = elem2.primitive().getNumber();
//                if(n instanceof Double){
//                    elem.setAttribute("type", "double");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getDouble()));
//                }else if(n instanceof Float){
//                    elem.setAttribute("type", "float");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getFloat()));
//                }else{
//                    elem.setAttribute("type", "double");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getDouble()));
//                }
//                break;
//            }
//            case INTEGER:{
//                Number n = elem2.primitive().getNumber();
//                if(n instanceof Integer){
//                    elem.setAttribute("type", "int");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getInt()));
//                }else if(n instanceof Long){
//                    elem.setAttribute("type", "long");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getLong()));
//                }else{
//                    elem.setAttribute("type", "int");
//                    elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getInt()));
//                }
//                break;
//            }
//            case BOOLEAN:{
//                elem.setAttribute("type", "boolean");
//                elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getBoolean()));
//                break;
//            }
//            case DATE:{
//                elem.setAttribute("type", "date");
//                elem.setTextContent(NutsTextUtils.stringValue(elem2.primitive().getDate()));
//                break;
//            }
//            case NULL:{
//                elem.setAttribute("type", "null");
//                break;
//            }
//            case OBJECT:{
//                elem.setAttribute("type", "object");
//                for (NutsElementEntry child : elem2.object().children()) {
//                    elem.appendChild(createElement(child.getKey(), child.getValue(),-1, document, session));
//                }
//                break;
//            }
//            case ARRAY:{
//                elem.setAttribute("type", "array");
//                int index=0;
//                for (NutsElement child : elem2.array().children()) {
//                    Element item = createElement("item", child, (long)index,document, session);
//                    elem.appendChild(item);
//                    index++;
//                }
//                break;
//            }
//            default:{
//                throw new IllegalArgumentException("Unsupported type"+elem2.type());
//            }
//        }
//        return elem;
//    }

    public static String createElementName(String name) {
        if (name == null) {
            name = "";
        }
        name = name.trim();
        if (name.isEmpty()) {
            name = "node";
        }
        if (Character.isDigit(name.charAt(0))) {
            name = "_" + name;
        }
        if (name.toLowerCase().startsWith("xml")) {
            name = "_" + name;
        }
        char[] r = name.toCharArray();
        for (int i = 0; i < r.length; i++) {
            char c = r[i];
            if (Character.isDigit(c)
                    || Character.isLetter(c)
                    || c == '_'
                    || c == '-'
                    || c == '.') {
                //ok
            } else {
                r[i] = '_';
            }
        }
        return new String(r);
    }

    public static Document createDocument(NSession session) {
        return createDocumentBuilder(false, session).newDocument();
    }

    public static DocumentBuilder createDocumentBuilder(boolean safe, NSession session) {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        if (safe) {
            documentFactory.setExpandEntityReferences(false);
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            //commented because some pom.xml contains <!DOCTYPE xml>
            //setLenientFeature(documentFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);

            // If you can't completely disable DTDs, then at least do the following:
            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
            // JDK7+ - http://xml.org/sax/features/external-general-entities
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-general-entities", false);
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-general-entities", false);
            setLenientFeature(documentFactory, "http://xml.org/sax/features/external-general-entities", false);

            // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
            // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
            // JDK7+ - http://xml.org/sax/features/external-parameter-entities
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-parameter-entities", false);
            setLenientFeature(documentFactory, "http://xml.org/sax/features/external-parameter-entities", false);
            setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities", false);

            // Disable external DTDs as well
            setLenientFeature(documentFactory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
            documentFactory.setXIncludeAware(false);
            documentFactory.setValidating(false);
        }
        DocumentBuilder b;
        try {
            b = documentFactory.newDocumentBuilder();
        } catch (ParserConfigurationException ex) {
            throw new NIOException(session,ex);
        }

        b.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                NLogOp.of(XmlUtils.class,session)
                        .level(Level.FINEST).verb(NLogVerb.WARNING)
                        .log(NMsg.ofC("%s",exception));
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                NLogOp.of(XmlUtils.class,session)
                        .level(Level.FINEST).verb(NLogVerb.WARNING)
                        .log(NMsg.ofC("%s",exception));
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                NLogOp.of(XmlUtils.class,session)
                        .level(Level.FINEST).verb(NLogVerb.WARNING)
                        .log(NMsg.ofC("%s",exception));
            }
        });
        return b;
    }

    private static void setLenientFeature(DocumentBuilderFactory dbFactory, String s, boolean b) {
        try {
            dbFactory.setFeature(s, b);
        } catch (Throwable ex) {
            //
        }
    }

    public static String documentToString(Document document, NSession session) {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            writeDocument(document, new StreamResult(b), true,true,session);
            return new String(b.toByteArray());
    }

    public static String elementToString(Element elem, NSession session) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            Document d = createDocument(session);
            elem = (Element) d.importNode(elem, true);
            d.appendChild(elem);
            writeDocument(d, new StreamResult(b), true,false,session);
            return new String(b.toByteArray());
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Element asElement(Node n){
        if(n instanceof Element){
            return (Element)n;
        }
        return null;
    }

    public static Iterable<Node> iterable(Node n){
        return new Iterable<Node>() {
            @Override
            public Iterator<Node> iterator() {
                NodeList nl = n.getChildNodes();
                return new Iterator<Node>() {
                    int i=0;
                    @Override
                    public boolean hasNext() {
                        return (i<nl.getLength());
                    }

                    @Override
                    public Node next() {
                        int i0=i;
                        i++;
                        return nl.item(i0);
                    }
                };
            }
        };
    }
    public static void writeDocument(Document document, StreamResult writer, boolean compact, boolean headerDeclaration, NSession session){
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException ex) {
            throw new NIOException(session,ex);
        }
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (!compact) {
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        document.setXmlStandalone(false);
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        if(!headerDeclaration) {
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        }
        DOMSource domSource = new DOMSource(document);
        try {
            transformer.transform(domSource, writer);
        } catch (TransformerException ex) {
            throw new NIOException(session,ex);
        }
    }

    public static boolean visitNode(Node n, Predicate<Node> tst) {
        if (!tst.test(n)) {
            return false;
        }
        if (n instanceof Element) {
            Element e = (Element) n;
            final NodeList nl = e.getChildNodes();
            final int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                if (!visitNode(nl.item(i), tst)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isNode(Element n, String... names) {
        Node nn = n;
        for (int i = names.length - 1; i >= 0; i--) {
            if (nn == null) {
                return false;
            }
            if (nn instanceof Element) {
                Element e = (Element) nn;
                String s = names[i];
                if (s.length() > 0) {
                    if (!e.getNodeName().equals(s)) {
                        return false;
                    }
                }
            } else {
                return false;
            }
            nn = nn.getParentNode();
        }
        return true;
    }

    public static boolean testNode(Node n, Predicate<Node> tst) {
        if (tst.test(n)) {
            return true;
        }
        if (n instanceof Element) {
            Element e = (Element) n;
            final NodeList nl = e.getChildNodes();
            final int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                if (testNode(nl.item(i), tst)) {
                    return true;
                }
            }
        }
        return false;
    }
}
