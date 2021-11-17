///**
// * ====================================================================
// *            Nuts : Network Updatable Things Service
// *                  (universal package manager)
// * <br>
// * is a new Open Source Package Manager to help install packages
// * and libraries for runtime execution. Nuts is the ultimate companion for
// * maven (and other build managers) as it helps installing all package
// * dependencies at runtime. Nuts is not tied to java and is a good choice
// * to share shell scripts and other 'things' . Its based on an extensible
// * architecture to help supporting a large range of sub managers / repositories.
// * <br>
// *
// * Copyright [2020] [thevpc]
// * Licensed under the Apache License, Version 2.0 (the "License"); you may
// * not use this file except in compliance with the License. You may obtain a
// * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
// * either express or implied. See the License for the specific language
// * governing permissions and limitations under the LietOrSetcense.
// * <br>
// * ====================================================================
//*/
//package net.thevpc.nuts.runtime.core.format.xml;
//
//import java.io.*;
//import java.net.URL;
//import java.nio.file.Path;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Set;
//import java.util.regex.Pattern;
//import javax.xml.parsers.ParserConfigurationException;
//import javax.xml.transform.TransformerException;
//import javax.xml.transform.stream.StreamResult;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
//import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
//import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryContext;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryContext;
//import net.thevpc.nuts.runtime.core.format.json.DefaultNutsJsonFormat;
//import net.thevpc.nuts.runtime.standalone.util.CoreCommonUtils;
//import org.w3c.dom.Node;
//import org.w3c.dom.NodeList;
//import org.xml.sax.InputSource;
//import org.xml.sax.SAXException;
//
///**
// *
// * @author thevpc
// */
//public class DefaultNutsXmlFormat extends DefaultFormatBase<NutsXmlFormat> implements NutsXmlFormat {
//
//    private static final Pattern NUM_REGEXP = Pattern.compile("-?\\d+(\\.\\d+)?");
//    private String defaulName = "value";
//    private String attributePrefix = "@";
//    private String typeAttribute = "_";
//    private boolean ignoreNullValue = true;
//    private boolean autoResolveType = true;
//    private boolean compact = false;
//    private org.w3c.dom.Document defaulDocument;
//    private final NutsElementFactoryContext xmlContext;
//    private Object value;
//
//    public DefaultNutsXmlFormat(NutsWorkspace ws) {
//        super(ws, "xml-format");
//        xmlContext = new DefaultNutsElementFactoryContext(ws) {
//
//            @Override
//            public NutsElement toElement(Object o) {
//                return fromXmlElement((org.w3c.dom.Element) o, NutsElement.class);
//            }
//        };
//    }
//
//    @Override
//    public <T> T parse(URL url, Class<T> clazz) {
//        try {
//            try (InputStream is = url.openStream()) {
//                return parse(is, clazz);
//            } catch (NutsException ex) {
//                throw ex;
//            } catch (RuntimeException ex) {
//                throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
//            }
//        } catch (IOException ex) {
//            throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
//        }
//    }
//
//    @Override
//    public <T> T parse(InputStream inputStream, Class<T> clazz) {
//        return parse(new InputStreamReader(inputStream), clazz);
//    }
//
//    @Override
//    public <T> T parse(byte[] bytes, Class<T> clazz) {
//        return parse(new ByteArrayInputStream(bytes), clazz);
//    }
//
//    @Override
//    public <T> T parse(Path path, Class<T> cls) {
//        File file = path.toFile();
//        try (FileReader r = new FileReader(file)) {
//            return parse(r, cls);
//        } catch (IOException ex) {
//            throw new NutsIOException(getWorkspace(),ex);
//        }
//    }
//
//    @Override
//    public <T> T parse(File file, Class<T> cls) {
//        try (FileReader r = new FileReader(file)) {
//            return parse(r, cls);
//        } catch (IOException ex) {
//            throw new NutsIOException(getWorkspace(),ex);
//        }
//    }
//
//    @Override
//    public <T> T parse(Reader reader, Class<T> clazz) {
//        Document doc = null;
//        try {
//            doc = NutsXmlUtils.createDocumentBuilder(false,getWorkspace()).parse(new InputSource(reader));
//        } catch (SAXException | ParserConfigurationException ex) {
//            throw new NutsIOException(getWorkspace(),new IOException(ex));
//        } catch (IOException ex) {
//            throw new NutsIOException(getWorkspace(),ex);
//        }
//        return fromXmlElement(doc == null ? null : doc.getDocumentElement(), clazz);
//    }
//
//    public String getDefaulTagName() {
//        return defaulName;
//    }
//
//    public boolean isIgnoreNullValue() {
//        return ignoreNullValue;
//    }
//
//    public boolean isAutoResolveType() {
//        return autoResolveType;
//    }
//
//    public org.w3c.dom.Document toXmlDocument(NutsElement o) {
//        try {
//            Document document = NutsXmlUtils.createDocument(getWorkspace());
//            document.appendChild(toXmlElement(o, getDefaulTagName(), document, false));
//            return document;
//        } catch (ParserConfigurationException ex) {
//            throw new NutsException(null, ex);
//        }
//    }
//
//    public String getAttributePrefix() {
//        return attributePrefix;
//    }
//
//    public String getTypeAttributeName() {
//        return typeAttribute;
//    }
//
//    @Override
//    public <T> T fromXmlElement(Element element, Class<T> cls) {
//        if (Element.class.isAssignableFrom(cls)) {
//            return (T) element;
//        }
//        if (NutsElement.class.isAssignableFrom(cls)) {
//            return (T) fromXmlElement(element);
//        }
//        DefaultNutsJsonFormat json = (DefaultNutsJsonFormat) getWorkspace().formats().json();
//        return json.convert(element, cls);
//    }
//
//    protected NutsElement fromXmlElement(org.w3c.dom.Element element) {
//        if (element == null) {
//            return xmlContext.builder().forNull();
//        }
//        String ta = getTypeAttributeName();
//        String d = element.getAttribute(ta);
//        NutsElementType elementType = null;
//        if (d != null) {
//            elementType = CoreCommonUtils.parseEnumString(d, NutsElementType.class, true);
//        }
//        if (elementType == null) {
//            //will resolve
//            int count = 0;
//            boolean distinct = false;
//            String lastName = null;
//            NodeList childNodes = element.getChildNodes();
//            for (int i = 0; i < childNodes.getLength(); i++) {
//                Node e = childNodes.item(i);
//                if (e instanceof Element) {
//                    Element ee = (Element) e;
//                    count++;
//                    if (lastName == null) {
//                        lastName = ee.getTagName();
//                    } else if (!lastName.equals(ee.getTagName())) {
//                        distinct = true;
//                    }
//                    if (count > 1 || distinct) {
//                        break;
//                    }
//                }
//            }
//            if (count == 0) {
//                String s = element.getTextContent();
//                if (s == null) {
//                    return xmlContext.builder().forNull();
//                }
//                switch (s) {
//                    case "true":
//                        return xmlContext.builder().forBoolean(true);
//                    case "false":
//                        return xmlContext.builder().forBoolean(false);
//                    case "null":
//                        return xmlContext.builder().forNull();
//                }
//                if (NUM_REGEXP.matcher(s).matches()) {
//                    try {
//                        return xmlContext.builder().forNumber(s);
//                    } catch (Exception ex) {
//                        return xmlContext.builder().forString(s);
//                    }
//                } else {
//                    return xmlContext.builder().forString(s);
//                }
//                //primitive
//            } else if (distinct) {
//                return new NutsObjectElementXml(element, xmlContext);
//            } else if (count == 1) {
//                if (lastName.equals(getArrayItemName())) {
//                    return new NutsArrayElementXml(element, xmlContext);
//                } else {
//                    return new NutsObjectElementXml(element, xmlContext);
//                }
//            } else {
//                return new NutsArrayElementXml(element, xmlContext);
//            }
//        }
//        switch (elementType) {
//            case BOOLEAN:
//                return xmlContext.builder().forBoolean(element.getTextContent());
//            case INTEGER:
//            case FLOAT:
//                return xmlContext.builder().forNumber(element.getTextContent());
//            case DATE:
//                return xmlContext.builder().forDate(element.getTextContent());
//            case STRING:
//                return xmlContext.builder().forString(element.getTextContent());
//            case ARRAY:
//                return new NutsArrayElementXml(element, xmlContext);
//            case OBJECT:
//                return new NutsObjectElementXml(element, xmlContext);
//            case NULL:
//                return xmlContext.builder().forNull();
//        }
//        throw new IllegalArgumentException("Unsupported");
//    }
//
//    public org.w3c.dom.Element toXmlElement(NutsElement o) {
//        return toXmlElement(o, getDefaulTagName(), null, false);
//    }
//
//    public String toXmlAttributeValue(NutsPrimitiveElement o) {
//        switch (o.type()) {
//            case BOOLEAN: {
//                return String.valueOf(((NutsPrimitiveElement) o).getBoolean());
//            }
//            case INTEGER:
//            case FLOAT: {
//                return String.valueOf(((NutsPrimitiveElement) o).getNumber());
//            }
//            case STRING: {
//                return ((NutsPrimitiveElement) o).getString();
//            }
//            case DATE: {
//                return ((NutsPrimitiveElement) o).getString();
//            }
//            case NULL: {
//                return null;
//            }
//        }
//        throw new NutsException(null, "Unsupported " + o.type());
//    }
//
//    private boolean isResolvableNonStringType(String s) {
//        if (s != null) {
//            switch (s) {
//                case "true":
//                case "false":
//                case "null":
//                    return true;
//            }
//            if (NUM_REGEXP.matcher(s).matches()) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    @Override
//    public Document toXmlDocument(Object value) {
//        if (value instanceof Document) {
//            return (Document) value;
//        }
//        Document document;
//        try {
//            document = NutsXmlUtils.createDocument(getWorkspace());
//        } catch (ParserConfigurationException ex) {
//            throw new NutsIllegalArgumentException(getWorkspace(), ex);
//        }
//        Element e = toXmlElement(value, document);
//        document.appendChild(e);
//        return document;
//    }
//
//    @Override
//    public Element toXmlElement(Object value, Document document) {
//        return toXmlElement(value, null, document, false);
//    }
//
//    public org.w3c.dom.Element toXmlElement(Object obj, String name, org.w3c.dom.Document doc, boolean returnNull) {
//        if (name == null) {
//            name = getDefaulTagName();
//        }
//        if (obj == null) {
//            if (!returnNull || !ignoreNullValue) {
//                Element e = doc.createElement(name);
//                if (!autoResolveType) {
//                    e.setAttribute(getTypeAttributeName(), "null");
//                }
//                return e;
//            }
//            return null;
//        }
//        if (obj instanceof Element) {
//            Element elem2 = (Element) obj;
//            if (doc == null) {
//                return elem2;
//            } else {
//                return (Element) doc.importNode(elem2, true);
//            }
//        }
//        if (obj instanceof Document) {
//            Document doc2 = (Document) obj;
//            if (doc == null) {
//                return ((Document) doc2).getDocumentElement();
//            } else {
//                Element elem3 = ((Document) doc2).getDocumentElement();
//                return (Element) doc.importNode(elem3, true);
//            }
//        }
//
//        NutsElement elem = getWorkspace().elem().toElement(obj);
//
//        if (doc == null) {
//            if (defaulDocument == null) {
//                try {
//                    defaulDocument = NutsXmlUtils.createDocument(getWorkspace());
//                } catch (ParserConfigurationException ex) {
//                    throw new NutsException(null, "Unable to create Document", ex);
//                }
//            }
//            doc = defaulDocument;
//        }
//        switch (elem.type()) {
//            case STRING: {
//                String v = toXmlAttributeValue(elem.primitive());
//                if (v != null || !returnNull || !ignoreNullValue) {
//                    Element e = doc.createElement(name);
//                    if (autoResolveType && isResolvableNonStringType(v)) {
//                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
//                    }
//                    e.setTextContent(v);
//                    return e;
//                }
//                return null;
//            }
//            case BOOLEAN:
//            case NULL: {
//                String v = toXmlAttributeValue(elem.primitive());
//                if (v != null || !returnNull || !ignoreNullValue) {
//                    Element e = doc.createElement(name);
//                    if (!autoResolveType) {
//                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
//                    }
//                    e.setTextContent(v);
//                    return e;
//                }
//                return null;
//            }
//            case INTEGER:
//            case FLOAT: {
//                String v = toXmlAttributeValue(elem.primitive());
//                if (v != null || !returnNull || !ignoreNullValue) {
//                    Element e = doc.createElement(name);
//                    if (!autoResolveType || !NUM_REGEXP.matcher(v).matches()) {
//                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
//                    }
//                    e.setTextContent(v);
//                    return e;
//                }
//                return null;
//            }
//            case DATE: {
//                String v = toXmlAttributeValue(elem.primitive());
//                if (v != null || !returnNull || !ignoreNullValue) {
//                    Element e = doc.createElement(name);
//                    e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
//                    e.setTextContent(v);
//                    return e;
//                }
//                return null;
//            }
//            case ARRAY: {
//                Element e = doc.createElement(name);
//                int count = 0;
//                for (NutsElement attribute : elem.array().children()) {
//                    Element c = toXmlElement(attribute, getArrayItemName(), doc, true);
//                    if (c != null) {
//                        e.appendChild(c);
//                        count++;
//                    }
//                }
//                if (!autoResolveType || count <= 1) {
//                    e.setAttribute(getTypeAttributeName(), "array");
//                }
//                return e;
//            }
//            case OBJECT: {
//                Element e = doc.createElement(name);
//                Set<String> visited = new HashSet<String>();
//                String ap = getAttributePrefix();
//                int count = 0;
//                boolean distinct = false;
//                String lastName = null;
//                for (NutsNamedElement attribute : elem.object().children()) {
//                    String k = attribute.getName();
//                    if (visited.contains(k)) {
//                        throw new IllegalArgumentException("Unexpected");
//                    }
//                    visited.add(k);
//                    if (k.equals(getTypeAttributeName())) {
//                        //ignore
//                    } else if (k.startsWith(ap)) {
//                        String vv = toXmlAttributeValue(attribute.getValue().primitive());
//                        if (vv != null) {
//                            e.setAttribute(k.substring(ap.length()), vv);
//                        }
//                    } else {
//                        if (lastName == null) {
//                            lastName = k;
//                        } else {
//                            if (!lastName.equals(k)) {
//                                distinct = true;
//                            }
//                        }
//                        Element b = toXmlElement(attribute.getValue(), k, doc, true);
//                        if (b != null) {
//                            e.appendChild(b);
//                            count++;
//                        }
//                    }
//                }
//                if (!autoResolveType || count <= 1 || !distinct) {
//                    e.setAttribute(getTypeAttributeName(), "object");
//                }
//                return e;
//            }
//            default: {
//                throw new IllegalArgumentException("Unsupported");
//            }
//        }
//    }
//
//    private static String getArrayItemName() {
//        return "item";
//    }
//
//    public static DefaultNutsXmlFormat getOrSet(NutsElementFactoryContext context) {
//        Map<String, Object> p = context.getProperties();
//        String key = DefaultNutsXmlFormat.class.getName();
//        DefaultNutsXmlFormat xml = (DefaultNutsXmlFormat) p.get(key);
//        if (xml != null) {
//            return xml;
//        }
//        xml = new DefaultNutsXmlFormat(context.getWorkspace());
//        p.put(key, xml);
//        return xml;
//    }
//
//    @Override
//    public boolean configureFirst(NutsCommandLine cmdLine) {
//        return false;
//    }
//
//    @Override
//    public Object getValue() {
//        return value;
//    }
//
//    @Override
//    public NutsXmlFormat value(Object value) {
//        return setValue(value);
//    }
//
//    @Override
//    public NutsXmlFormat setValue(Object value) {
//        this.value = value;
//        return this;
//    }
//
//    @Override
//    public void print(PrintStream out) {
//        Document doc = toXmlDocument(value);
//        try {
//            NutsXmlUtils.writeDocument(doc, new StreamResult(out), compact,true);
//        } catch (TransformerException ex) {
//            throw new NutsException(getWorkspace(), CoreStringUtils.exceptionToString(ex), ex);
//        }
//    }
//
//    @Override
//    public boolean isCompact() {
//        return compact;
//    }
//
//    @Override
//    public NutsXmlFormat setCompact(boolean compact) {
//        this.compact = compact;
//        return this;
//    }
//
//}
