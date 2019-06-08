/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format.xml;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.vpc.app.nuts.NutsException;
import net.vpc.app.nuts.NutsWorkspace;

import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author vpc
 */
public class NutsXmlUtils {

    public static void print(String name, Object object, Writer out, boolean compact, NutsWorkspace ws) {
        print(name, object, (Object) out, compact, ws);
    }

    public static void print(String name, Object object, PrintStream out, boolean compact, NutsWorkspace ws) {
        print(name, object, (Object) out, compact, ws);
    }

    private static void print(String name, Object object, Object out, boolean compact, NutsWorkspace ws) {
        try {
            Document document = NutsXmlUtils.createDocument();
            String rootName = name;
            document.appendChild(createElement(CoreStringUtils.isBlank(rootName) ? "root" : rootName, object, document, ws));
            StreamResult streamResult = null;
            if (out instanceof PrintStream) {
                streamResult = new StreamResult((PrintStream) out);
            } else {
                streamResult = new StreamResult((Writer) out);
            }
            NutsXmlUtils.writeDocument(document, streamResult, compact);
            if (out instanceof PrintStream) {
                ((PrintStream) out).flush();
            } else {
                ((Writer) out).flush();
            }

        } catch (ParserConfigurationException | TransformerException ex) {
            throw new UncheckedIOException(new IOException(ex));
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static Document createDocument(String name, Object object, NutsWorkspace ws) {
        try {
            Document document = createDocument();
            document.appendChild(createElement(CoreStringUtils.isBlank(name) ? "root" : name, object, document, ws));
            return document;
        } catch (ParserConfigurationException ex) {
            throw new NutsException(null, ex);
        }
    }

    public static Element createElement(String name, Object o, Document document, NutsWorkspace ws) {
        // root element
        Element elem = document.createElement(createElementName(name));
        if (o == null) {
            elem.setAttribute("type", "null");
        } else if (o instanceof JsonElement) {
            JsonElement je = (JsonElement) o;
            if (je.isJsonNull()) {
                elem.setAttribute("type", "null");
            } else if (je.isJsonPrimitive()) {
                JsonPrimitive jr = je.getAsJsonPrimitive();
                if (jr.isString()) {
                    elem.setAttribute("type", "string");
                } else if (jr.isNumber()) {
                    elem.setAttribute("type", "number");
                } else if (jr.isBoolean()) {
                    elem.setAttribute("type", "boolean");
                }
                elem.setTextContent(je.getAsString());
            } else if (je.isJsonArray()) {
                elem.setAttribute("type", "array");
                JsonArray arr = je.getAsJsonArray();
                int index = 0;
                for (int i = 0; i < arr.size(); i++) {
                    Element item = createElement("item", arr.get(i), document, ws);
                    item.setAttribute("index", String.valueOf(index));
                    elem.appendChild(item);
                    index++;
                }
            } else if (je.isJsonObject()) {
                elem.setAttribute("type", "object");
                JsonObject arr = je.getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : arr.entrySet()) {
                    elem.appendChild(createElement(entry.getKey(), entry.getValue(), document, ws));
                }
            }
        } else if (o instanceof Map) {
            elem.setAttribute("type", "object");
            Map<Object, Object> m = (Map) o;
            for (Map.Entry<Object, Object> entry : m.entrySet()) {
                elem.appendChild(createElement(CoreCommonUtils.stringValue(entry.getKey()), entry.getValue(), document, ws));
            }
        } else if (o instanceof Collection) {
            elem.setAttribute("type", "array");
            Collection m = (Collection) o;
            int index = 0;
            for (Object entry : m) {
                Element item = createElement("item", entry, document, ws);
                item.setAttribute("index", String.valueOf(index));
                elem.appendChild(item);
                index++;
            }
        } else if (o instanceof String) {
            elem.setAttribute("type", "string");
            elem.setTextContent(CoreCommonUtils.stringValue(o));
        } else if (o instanceof Number) {
            elem.setAttribute("type", "number");
            elem.setTextContent(CoreCommonUtils.stringValue(o));
        } else if (o instanceof Boolean) {
            elem.setAttribute("type", "boolean");
            elem.setTextContent(CoreCommonUtils.stringValue(o));
        } else if (o instanceof Date) {
            elem.setAttribute("type", "date");
            elem.setTextContent(CoreCommonUtils.stringValue(o));
        } else {
            elem.setTextContent(CoreCommonUtils.stringValue("string"));
            elem.setTextContent(CoreCommonUtils.stringValue(o));
        }
        return elem;
    }

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

    public static Document createDocument() throws ParserConfigurationException {
        return createDocumentBuilder(false).newDocument();
    }

    public static DocumentBuilder createDocumentBuilder(boolean safe) throws ParserConfigurationException {
        DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
        if (safe) {
            documentFactory.setExpandEntityReferences(false);
            // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
            // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
            setLenientFeature(documentFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);

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
        return documentFactory.newDocumentBuilder();
    }

    private static void setLenientFeature(DocumentBuilderFactory dbFactory, String s, boolean b) {
        try {
            dbFactory.setFeature(s, b);
        } catch (Exception ex) {
            //
        }
    }

    public static String documentToString(Document document) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            writeDocument(document, new StreamResult(b), true);
            return new String(b.toByteArray());
        } catch (TransformerException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String elementToString(Element elem) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            Document d = createDocument();
            elem = (Element) d.importNode(elem, true);
            d.appendChild(elem);
            writeDocument(d, new StreamResult(b), true);
            return new String(b.toByteArray());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static void writeDocument(Document document, StreamResult writer, boolean compact) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        if (!compact) {
            document.setXmlStandalone(true);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.STANDALONE, "false");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        }
        DOMSource domSource = new DOMSource(document);
        transformer.transform(domSource, writer);
    }
}
