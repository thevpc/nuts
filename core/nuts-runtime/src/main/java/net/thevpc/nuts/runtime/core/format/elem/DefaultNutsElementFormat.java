package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.xml.NutsArrayElementXml;
import net.thevpc.nuts.runtime.core.format.xml.NutsObjectElementXml;
import net.thevpc.nuts.runtime.core.format.xml.NutsXmlUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import net.thevpc.nuts.runtime.core.format.json.AdapterHelpers;
import net.thevpc.nuts.runtime.core.format.json.GsonItemSerializeManager;
import net.thevpc.nuts.runtime.core.util.CoreEnumUtils;

public class DefaultNutsElementFormat extends DefaultFormatBase<NutsElementFormat> implements NutsElementFormat, NutsElementFactoryContext {

    private final NutsElementFactoryService nvalueFactory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<>();
    private Object value;
    private NutsElementBuilder builder;
    private NutsContentType contentType = NutsContentType.JSON;
    private boolean compact;
    private static final Pattern NUM_REGEXP = Pattern.compile("-?\\d+(\\.\\d+)?");
    private String defaultName = "value";
    private String attributePrefix = "@";
    private String typeAttribute = "_";
    private boolean ignoreNullValue = true;
    private boolean autoResolveType = true;
    private org.w3c.dom.Document defaultDocument;
    private final NutsElementFactoryContext xmlContext;
    private GsonItemSerializeManager jsonMan;

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        super(ws, "element-format");
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
        builder = new DefaultNutsElementBuilder(ws);
        xmlContext = new DefaultNutsElementFactoryContext(ws) {

            @Override
            public NutsElement toElement(Object o) {
                return convert(o, NutsElement.class);
            }
        };
        jsonMan = new GsonItemSerializeManager(ws, () -> getSession());
        jsonMan.setAdapter(Instant.class, new AdapterHelpers.InstantJsonAdapter());
        jsonMan.setAdapter(NutsId.class, new AdapterHelpers.NutsIdJsonAdapter());
        jsonMan.setAdapter(NutsVersion.class, new AdapterHelpers.NutsVersionJsonAdapter());
        jsonMan.setAdapter(NutsDescriptor.class, new AdapterHelpers.NutsDescriptorJsonAdapter());
        jsonMan.setAdapter(NutsDependency.class, new AdapterHelpers.NutsDependencyJsonAdapter());
        jsonMan.setAdapter(NutsIdLocation.class, new AdapterHelpers.NutsIdLocationJsonAdapter());
        jsonMan.setAdapter(NutsClassifierMapping.class, new AdapterHelpers.NutsClassifierMappingJsonAdapter());
        jsonMan.setAdapter(NutsArtifactCall.class, new AdapterHelpers.NutsArtifactCallElementAdapter());
        jsonMan.setAdapter(Path.class, new AdapterHelpers.PathJsonAdapter());
        jsonMan.setAdapter(File.class, new AdapterHelpers.FileJsonAdapter());
        jsonMan.setAdapter(Date.class, new AdapterHelpers.DateJsonAdapter());
        jsonMan.setAdapter(NutsElement.class, new AdapterHelpers.NutsElementElementAdapter());
        jsonMan.setAdapter(org.w3c.dom.Document.class, new AdapterHelpers.XmlDocumentJsonAdapter());
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
    }

    @Override
    public NutsElementFormat setContentType(NutsContentType contentType) {
        if (contentType == null) {
            this.contentType = NutsContentType.JSON;
        } else {
            switch (contentType) {
                case TREE:
                case TABLE:
                case PLAIN: {
                    throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
                }
            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public NutsElementBuilder builder() {
        return builder;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsElementFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {

        switch (contentType) {
            case JSON:
            case XML: {
                try {
                    try (InputStream is = NutsWorkspaceUtils.of(getWorkspace()).openURL(url)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NutsIOException(getWorkspace(), ex);
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getWorkspace(), "unable to parse url " + url, ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getWorkspace(), "unable to parse url " + url, ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case XML: {
                return parse(new InputStreamReader(inputStream), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case XML: {
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case XML: {
                return parse(new InputStreamReader(new ByteArrayInputStream(bytes)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        switch (contentType) {
            case JSON: {
                return jsonMan.fromJson(reader, clazz);
            }
            case XML: {
                Document doc = null;
                try {
                    doc = NutsXmlUtils.createDocumentBuilder(false, getValidSession()).parse(new InputSource(reader));
                } catch (SAXException | ParserConfigurationException ex) {
                    throw new NutsIOException(getWorkspace(), new IOException(ex));
                } catch (IOException ex) {
                    throw new NutsIOException(getWorkspace(), ex);
                }
                return convert(doc == null ? null : doc.getDocumentElement(), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case XML: {
                try (Reader r = Files.newBufferedReader(file)) {
                    return parse(r, clazz);
                } catch (IOException ex) {
                    throw new NutsIOException(getWorkspace(), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case XML: {
                try (FileReader r = new FileReader(file)) {
                    return parse(r, clazz);
                } catch (IOException ex) {
                    throw new NutsIOException(getWorkspace(), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(), "invalid content type " + contentType + ". Only structured content types re allowed.");
    }

    @Override
    public NutsElementFactory getFallback() {
        return fallback;
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setFallback(NutsElementFactory fallback) {
        this.fallback = fallback;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        return false;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NutsElementFormat setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsElementPath compilePath(String pathExpression) {
        NutsSession session = getSession();
        if (session == null) {
            session = getWorkspace().createSession();
        }
        return NutsElementPathFilter.compile(pathExpression, session);
    }

    @Override
    public Element toXmlElement(Object value, Document document) {
        return toXmlElement(value, null, document, false);
    }

    public org.w3c.dom.Element toXmlElement(Object obj, String name, org.w3c.dom.Document doc, boolean returnNull) {
        if (name == null) {
            name = getDefaulTagName();
        }
        if (obj == null) {
            if (!returnNull || !ignoreNullValue) {
                Element e = doc.createElement(name);
                if (!autoResolveType) {
                    e.setAttribute(getTypeAttributeName(), "null");
                }
                return e;
            }
            return null;
        }
        if (obj instanceof Element) {
            Element elem2 = (Element) obj;
            if (doc == null) {
                return elem2;
            } else {
                return (Element) doc.importNode(elem2, true);
            }
        }
        if (obj instanceof Document) {
            Document doc2 = (Document) obj;
            if (doc == null) {
                return doc2.getDocumentElement();
            } else {
                Element elem3 = doc2.getDocumentElement();
                return (Element) doc.importNode(elem3, true);
            }
        }

        NutsElement elem = getWorkspace().formats().element().convert(obj, NutsElement.class);

        if (doc == null) {
            if (defaultDocument == null) {
                try {
                    defaultDocument = NutsXmlUtils.createDocument(getSession());
                } catch (ParserConfigurationException ex) {
                    throw new NutsException(getWorkspace(), "Unable to create Document", ex);
                }
            }
            doc = defaultDocument;
        }
        switch (elem.type()) {
            case STRING: {
                String v = toXmlAttributeValue(elem.primitive());
                if (v != null || !returnNull || !ignoreNullValue) {
                    Element e = doc.createElement(name);
                    if (autoResolveType && isResolvableNonStringType(v)) {
                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
                    }
                    e.setTextContent(v);
                    return e;
                }
                return null;
            }
            case BOOLEAN:
            case NULL: {
                String v = toXmlAttributeValue(elem.primitive());
                if (v != null || !returnNull || !ignoreNullValue) {
                    Element e = doc.createElement(name);
                    if (!autoResolveType) {
                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
                    }
                    e.setTextContent(v);
                    return e;
                }
                return null;
            }
            case INTEGER:
            case FLOAT: {
                String v = toXmlAttributeValue(elem.primitive());
                if (v != null || !returnNull || !ignoreNullValue) {
                    Element e = doc.createElement(name);
                    if (!autoResolveType || !NUM_REGEXP.matcher(v).matches()) {
                        e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
                    }
                    e.setTextContent(v);
                    return e;
                }
                return null;
            }
            case DATE: {
                String v = toXmlAttributeValue(elem.primitive());
                if (v != null || !returnNull || !ignoreNullValue) {
                    Element e = doc.createElement(name);
                    e.setAttribute(getTypeAttributeName(), elem.type().toString().toLowerCase());
                    e.setTextContent(v);
                    return e;
                }
                return null;
            }
            case ARRAY: {
                Element e = doc.createElement(name);
                int count = 0;
                for (NutsElement attribute : elem.array().children()) {
                    Element c = toXmlElement(attribute, getArrayItemName(), doc, true);
                    if (c != null) {
                        e.appendChild(c);
                        count++;
                    }
                }
                if (!autoResolveType || count <= 1) {
                    e.setAttribute(getTypeAttributeName(), "array");
                }
                return e;
            }
            case OBJECT: {
                Element e = doc.createElement(name);
                Set<String> visited = new HashSet<String>();
                String ap = getAttributePrefix();
                int count = 0;
                boolean distinct = false;
                String lastName = null;
                for (NutsNamedElement attribute : elem.object().children()) {
                    String k = attribute.getName();
                    if (visited.contains(k)) {
                        throw new IllegalArgumentException("Unexpected");
                    }
                    visited.add(k);
                    if (k.equals(getTypeAttributeName())) {
                        //ignore
                    } else if (k.startsWith(ap)) {
                        String vv = toXmlAttributeValue(attribute.getValue().primitive());
                        if (vv != null) {
                            e.setAttribute(k.substring(ap.length()), vv);
                        }
                    } else {
                        if (lastName == null) {
                            lastName = k;
                        } else {
                            if (!lastName.equals(k)) {
                                distinct = true;
                            }
                        }
                        Element b = toXmlElement(attribute.getValue(), k, doc, true);
                        if (b != null) {
                            e.appendChild(b);
                            count++;
                        }
                    }
                }
                if (!autoResolveType || count <= 1 || !distinct) {
                    e.setAttribute(getTypeAttributeName(), "object");
                }
                return e;
            }
            default: {
                throw new IllegalArgumentException("Unsupported");
            }
        }
    }

    private static String getArrayItemName() {
        return "item";
    }

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
    public String getDefaulTagName() {
        return defaultName;
    }

    public boolean isIgnoreNullValue() {
        return ignoreNullValue;
    }

    public boolean isAutoResolveType() {
        return autoResolveType;
    }

    public String getAttributePrefix() {
        return attributePrefix;
    }

    public String getTypeAttributeName() {
        return typeAttribute;
    }

    private boolean isResolvableNonStringType(String s) {
        if (s != null) {
            switch (s) {
                case "true":
                case "false":
                case "null":
                    return true;
            }
            if (NUM_REGEXP.matcher(s).matches()) {
                return true;
            }
        }
        return false;
    }

    public String toXmlAttributeValue(NutsPrimitiveElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return String.valueOf(o.getBoolean());
            }
            case INTEGER:
            case FLOAT: {
                return String.valueOf(o.getNumber());
            }
            case STRING: {
                return o.getString();
            }
            case DATE: {
                return o.getString();
            }
            case NULL: {
                return null;
            }
        }
        throw new NutsException(getWorkspace(), "unsupported " + o.type());
    }

    protected NutsElement nutsElementToXmlElement(org.w3c.dom.Element element) {
        if (element == null) {
            return xmlContext.builder().forNull();
        }
        String ta = getTypeAttributeName();
        String d = element.getAttribute(ta);
        NutsElementType elementType = null;
        if (d != null) {
            elementType = CoreEnumUtils.parseEnumString(d, NutsElementType.class, true);
        }
        if (elementType == null) {
            //will resolve
            int count = 0;
            boolean distinct = false;
            String lastName = null;
            NodeList childNodes = element.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node e = childNodes.item(i);
                if (e instanceof Element) {
                    Element ee = (Element) e;
                    count++;
                    if (lastName == null) {
                        lastName = ee.getTagName();
                    } else if (!lastName.equals(ee.getTagName())) {
                        distinct = true;
                    }
                    if (count > 1 || distinct) {
                        break;
                    }
                }
            }
            if (count == 0) {
                String s = element.getTextContent();
                if (s == null) {
                    return xmlContext.builder().forNull();
                }
                switch (s) {
                    case "true":
                        return xmlContext.builder().forBoolean(true);
                    case "false":
                        return xmlContext.builder().forBoolean(false);
                    case "null":
                        return xmlContext.builder().forNull();
                }
                if (NUM_REGEXP.matcher(s).matches()) {
                    try {
                        return xmlContext.builder().forNumber(s);
                    } catch (Exception ex) {
                        return xmlContext.builder().forString(s);
                    }
                } else {
                    return xmlContext.builder().forString(s);
                }
                //primitive
            } else if (distinct) {
                return new NutsObjectElementXml(element, xmlContext);
            } else if (count == 1) {
                if (lastName.equals(getArrayItemName())) {
                    return new NutsArrayElementXml(element, xmlContext);
                } else {
                    return new NutsObjectElementXml(element, xmlContext);
                }
            } else {
                return new NutsArrayElementXml(element, xmlContext);
            }
        }
        switch (elementType) {
            case BOOLEAN:
                return xmlContext.builder().forBoolean(element.getTextContent());
            case INTEGER:
            case FLOAT:
                return xmlContext.builder().forNumber(element.getTextContent());
            case DATE:
                return xmlContext.builder().forDate(element.getTextContent());
            case STRING:
                return xmlContext.builder().forString(element.getTextContent());
            case ARRAY:
                return new NutsArrayElementXml(element, xmlContext);
            case OBJECT:
                return new NutsObjectElementXml(element, xmlContext);
            case NULL:
                return xmlContext.builder().forNull();
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public org.w3c.dom.Element toXmlElement(NutsElement o) {
        return toXmlElement(o, getDefaulTagName(), null, false);
    }

//    public <T> T parseXml(Reader reader, Class<T> clazz) {
//        Document doc = null;
//        try {
//            doc = NutsXmlUtils.createDocumentBuilder(false,getValidSession()).parse(new InputSource(reader));
//        } catch (SAXException | ParserConfigurationException ex) {
//            throw new NutsIOException(getWorkspace(),new IOException(ex));
//        } catch (IOException ex) {
//            throw new NutsIOException(getWorkspace(),ex);
//        }
//        return convert(doc == null ? null : doc.getDocumentElement(), clazz);
//    }
    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (any == null) {
            return null;
        }
        if (to.isAssignableFrom(NutsElement.class)) {
            return (T) nvalueFactory.create(any, this);
        }
        if (to.equals(Document.class)) {
            if (any instanceof Document) {
                return (T) any;
            }
            Document document;
            try {
                document = NutsXmlUtils.createDocument(getSession());
            } catch (ParserConfigurationException ex) {
                throw new NutsIllegalArgumentException(getWorkspace(), ex);
            }
            Element e = toXmlElement(any, document);
            document.appendChild(e);
            return (T) document;
        }
        if (NutsElement.class.isAssignableFrom(any.getClass())) {
            if (NutsElement.class.isAssignableFrom(to)) {
                return (T) any;
            }
            if (org.w3c.dom.Node.class.isAssignableFrom(to)) {
                if (org.w3c.dom.Document.class.isAssignableFrom(to)) {
                    Document document;
                    try {
                        document = NutsXmlUtils.createDocument(getSession());
                    } catch (ParserConfigurationException ex) {
                        throw new NutsIllegalArgumentException(getWorkspace(), ex);
                    }
                    Element e = toXmlElement(any, document);
                    document.appendChild(e);
                    return (T) document;
                }
                if (org.w3c.dom.Element.class.isAssignableFrom(to)) {
                    return (T) toXmlElement(any, null);
                }
            }
        } else if (Element.class.isAssignableFrom(any.getClass())) {
            if (Element.class.isAssignableFrom(to)) {
                return (T) any;
            }
            if (NutsElement.class.isAssignableFrom(to)) {
                return (T) nutsElementToXmlElement((Element) any);
            }
        }
        return jsonMan.fromObject(any, to);
    }

    @Override
    public void print(PrintStream out) {
        switch (getContentType()) {
            case JSON: {
                if (getWorkspace().io().term().isFormatted(out)) {
                    ByteArrayPrintStream bos = new ByteArrayPrintStream();
                    jsonMan.print(value, bos, compact);
                    out.print(getWorkspace().formats().text().code("json", bos.toString()));
                } else {
                    jsonMan.print(value, out, compact);
                }
                out.flush();
                break;
            }
            case XML: {
                Document doc = convert(value, Document.class);
                if (getWorkspace().io().term().isFormatted(out)) {
                    ByteArrayPrintStream bos = new ByteArrayPrintStream();
                    try {
                        NutsXmlUtils.writeDocument(doc, new StreamResult(bos), compact, true);
                    } catch (TransformerException ex) {
                        throw new NutsException(getWorkspace(), CoreStringUtils.exceptionToString(ex), ex);
                    }
                    out.print(getWorkspace().formats().text().code("xml", bos.toString()));
                } else {
                    try {
                        NutsXmlUtils.writeDocument(doc, new StreamResult(out), compact, true);
                    } catch (TransformerException ex) {
                        throw new NutsException(getWorkspace(), CoreStringUtils.exceptionToString(ex), ex);
                    }
                }
                break;
            }
        }
    }

    @Override
    public NutsElement toElement(Object o) {
        return convert(o, NutsElement.class);
    }
}
