package net.thevpc.nuts.runtime.standalone.format.elem;

import com.google.gson.*;
import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.standalone.DefaultNutsClassifierMappingBuilder;
import net.thevpc.nuts.runtime.standalone.DefaultNutsVersion;
import net.thevpc.nuts.runtime.standalone.MutableNutsDependencyTreeNode;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsArtifactCallBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsDescriptorBuilder;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsIdLocationBuilder;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.json.NutsArrayElementJson;
import net.thevpc.nuts.runtime.standalone.format.json.NutsObjectElementJson;
import net.thevpc.nuts.runtime.standalone.format.xml.NutsArrayElementXml;
import net.thevpc.nuts.runtime.standalone.format.xml.NutsObjectElementXml;
import net.thevpc.nuts.runtime.standalone.format.xml.NutsXmlUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

public class DefaultNutsElementFormat extends DefaultFormatBase<NutsElementFormat> implements NutsElementFormat, NutsElementFactoryContext {

    private final NutsElementFactoryService nvalueFactory;
    private NutsElementFactory fallback;
    private final Map<String, Object> properties = new HashMap<>();
    private Object value;
    private NutsElementBuilder builder;
    private NutsContentType contentType= NutsContentType.JSON;
    private boolean compact;
    private static final Pattern NUM_REGEXP = Pattern.compile("-?\\d+(\\.\\d+)?");
    private String defaultName = "value";
    private String attributePrefix = "@";
    private String typeAttribute = "_";
    private boolean ignoreNullValue = true;
    private boolean autoResolveType = true;
    private org.w3c.dom.Document defaultDocument;
    private final NutsElementFactoryContext xmlContext;
    private final NutsElementFactoryContext dummyContext;
    private Gson GSON_COMPACT;
    private Gson GSON_PRETTY;

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        super(ws, "element-format");
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
        builder = new DefaultNutsElementBuilder();
        xmlContext = new DefaultNutsElementFactoryContext(ws) {

            @Override
            public NutsElement toElement(Object o) {
                return convert(o, NutsElement.class);
            }
        };
        dummyContext = new DefaultNutsElementFactoryContext(ws) {
            @Override
            public NutsElement toElement(Object o) {
                return fromJsonElement((JsonElement) o);
            }

        };
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
    }

    @Override
    public NutsElementFormat setContentType(NutsContentType contentType) {
        if(contentType==null){
            this.contentType= NutsContentType.JSON;
        }else{
            switch (contentType){
                case TREE:
                case TABLE:
                case PLAIN:{
                    throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
                }
            }
            this.contentType=contentType;
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
        this.compact=compact;
        return this;
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {

        switch (contentType){
            case JSON:
            case XML:{
                try {
                    try (InputStream is = url.openStream()) {
                        return parseXml(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getWorkspace(), "Unable to parse url " + url, ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        switch (contentType){
            case JSON:
            case XML:{
                return parseXml(new InputStreamReader(inputStream), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        switch (contentType){
            case JSON:
            case XML:{
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        switch (contentType){
            case JSON:
            case XML:{
                return parse(new InputStreamReader(new ByteArrayInputStream(bytes)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        switch (contentType){
            case JSON:{
                return parseJson(reader,clazz);
            }
            case XML:{
                return parseXml(reader, clazz);
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        switch (contentType){
            case JSON:
            case XML:{
                try (Reader r = Files.newBufferedReader(file)) {
                    return parse(r, clazz);
                } catch (IOException ex) {
                    throw new NutsIOException(getWorkspace(),ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        switch (contentType){
            case JSON:
            case XML:{
                try (FileReader r = new FileReader(file)) {
                    return parse(r, clazz);
                } catch (IOException ex) {
                    throw new NutsIOException(getWorkspace(),ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getWorkspace(),"invalid content type "+contentType+". Only structured content types re allowed.");
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

        NutsElement elem = getWorkspace().formats().element().convert(obj,NutsElement.class);

        if (doc == null) {
            if (defaultDocument == null) {
                try {
                    defaultDocument = NutsXmlUtils.createDocument(getSession());
                } catch (ParserConfigurationException ex) {
                    throw new NutsException(null, "Unable to create Document", ex);
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
        throw new NutsException(null, "Unsupported " + o.type());
    }

    protected NutsElement nutsElementToXmlElement(org.w3c.dom.Element element) {
        if (element == null) {
            return xmlContext.builder().forNull();
        }
        String ta = getTypeAttributeName();
        String d = element.getAttribute(ta);
        NutsElementType elementType = null;
        if (d != null) {
            elementType = CoreCommonUtils.parseEnumString(d, NutsElementType.class, true);
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


    public <T> T parseXml(Reader reader, Class<T> clazz) {
        Document doc = null;
        try {
            doc = NutsXmlUtils.createDocumentBuilder(false,getSession()).parse(new InputSource(reader));
        } catch (SAXException | ParserConfigurationException ex) {
            throw new NutsIOException(getWorkspace(),new IOException(ex));
        } catch (IOException ex) {
            throw new NutsIOException(getWorkspace(),ex);
        }
        return convert(doc == null ? null : doc.getDocumentElement(), clazz);
    }

    public <T> T parseJson(Reader reader, Class<T> clazz) {
        return getGson(true).fromJson(reader, clazz);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if(any==null){
            return null;
        }
        if(to.isAssignableFrom(NutsElement.class)){
            return (T) nvalueFactory.create(any, this);
        }
        if(to.equals(Document.class)){
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
            return (T)document;
        }
        if(NutsElement.class.isAssignableFrom(any.getClass())){
            if(NutsElement.class.isAssignableFrom(to)) {
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
                    return (T)document;
                }
                if (org.w3c.dom.Element.class.isAssignableFrom(to)) {
                    return (T) toXmlElement(any, null);
                }
            }
        }else if(Element.class.isAssignableFrom(any.getClass())){
            if (Element.class.isAssignableFrom(to)) {
                return (T) any;
            }
            if (NutsElement.class.isAssignableFrom(to)) {
                return (T) nutsElementToXmlElement((Element) any);
            }
        }
        Gson gson = getGson(true);
        JsonElement t = gson.toJsonTree(any);
        print(System.out);
        return gson.fromJson(t, to);
    }

    @Override
    public void print(PrintStream out) {
        switch (getContentType()){
            case JSON:{
                getGson(compact).toJson(value, out);
                out.flush();
                break;
            }
            case XML:{
                Document doc = convert(value,Document.class);
                try {
                    NutsXmlUtils.writeDocument(doc, new StreamResult(out), compact,true);
                } catch (TransformerException ex) {
                    throw new NutsException(getWorkspace(), CoreStringUtils.exceptionToString(ex), ex);
                }
                break;
            }
        }
    }

    public Gson getGson(boolean compact) {
        if (compact) {
            if (GSON_COMPACT == null) {
                GSON_COMPACT = prepareBuilder().create();
            }
            return GSON_COMPACT;
        } else {
            if (GSON_PRETTY == null) {
                GSON_PRETTY = prepareBuilder().setPrettyPrinting().create();
            }
            return GSON_PRETTY;
        }
    }

    public GsonBuilder prepareBuilder() {
        return new GsonBuilder()
                .registerTypeHierarchyAdapter(NutsId.class, new NutsIdJsonAdapter())
                .registerTypeHierarchyAdapter(NutsVersion.class, new NutsVersionJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDescriptor.class, new NutsDescriptorJsonAdapter())
                .registerTypeHierarchyAdapter(NutsDependency.class, new NutsDependencyJsonAdapter())
                .registerTypeHierarchyAdapter(NutsIdLocation.class, new NutsIdLocationJsonAdapter())
                .registerTypeHierarchyAdapter(NutsClassifierMapping.class, new NutsClassifierMappingJsonAdapter())
                .registerTypeHierarchyAdapter(NutsArtifactCall.class, new NutsExecutorDescriptorAdapter())
                .registerTypeHierarchyAdapter(NutsDependencyTreeNode.class, new NutsDependencyTreeNodeJsonAdapter())
                .registerTypeHierarchyAdapter(NutsElement.class, new NutsElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Element.class, new XmlElementJsonAdapter())
                .registerTypeHierarchyAdapter(org.w3c.dom.Document.class, new XmlDocumentJsonAdapter())
                .registerTypeHierarchyAdapter(Path.class, new PathJsonAdapter())
                .registerTypeHierarchyAdapter(File.class, new FileJsonAdapter())
                .registerTypeHierarchyAdapter(Date.class, new DateJsonAdapter())
                .registerTypeHierarchyAdapter(Instant.class, new InstantJsonAdapter());
    }

    private static class NutsIdJsonAdapter implements
            com.google.gson.JsonSerializer<NutsId>,
            com.google.gson.JsonDeserializer<NutsId> {

        @Override
        public NutsId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return NutsWorkspaceUtils.parseRequiredNutsId0(s);
        }

        @Override
        public JsonElement serialize(NutsId src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsVersionJsonAdapter implements
            com.google.gson.JsonSerializer<NutsVersion>,
            com.google.gson.JsonDeserializer<NutsVersion> {

        @Override
        public NutsVersion deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String s = context.deserialize(json, String.class);
            if (s == null) {
                return null;
            }
            return DefaultNutsVersion.valueOf(s);
        }

        @Override
        public JsonElement serialize(NutsVersion src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src == null ? null : src.toString());
        }
    }

    private static class NutsDescriptorJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDescriptor>,
            com.google.gson.JsonDeserializer<NutsDescriptor> {

        @Override
        public NutsDescriptor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            DefaultNutsDescriptorBuilder b = context.deserialize(json, DefaultNutsDescriptorBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsDescriptor src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsDescriptorBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsDependencyTreeNodeJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependencyTreeNode>,
            com.google.gson.JsonDeserializer<NutsDependencyTreeNode> {

        @Override
        public NutsDependencyTreeNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return context.deserialize(json, MutableNutsDependencyTreeNode.class);
        }

        @Override
        public JsonElement serialize(NutsDependencyTreeNode src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new MutableNutsDependencyTreeNode(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsDependencyJsonAdapter implements
            com.google.gson.JsonSerializer<NutsDependency>,
            com.google.gson.JsonDeserializer<NutsDependency> {

        @Override
        public NutsDependency deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String b = context.deserialize(json, String.class);
            return CoreNutsUtils.parseNutsDependency(null, b);
        }

        @Override
        public JsonElement serialize(NutsDependency src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(src.toString());
            }
            return context.serialize(src);
        }
    }

    private static class NutsIdLocationJsonAdapter implements
            com.google.gson.JsonSerializer<NutsIdLocation>,
            com.google.gson.JsonDeserializer<NutsIdLocation> {

        @Override
        public NutsIdLocation deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsIdLocationBuilder b = context.deserialize(json, DefaultNutsIdLocationBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsIdLocation src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsIdLocationBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsClassifierMappingJsonAdapter implements
            com.google.gson.JsonSerializer<NutsClassifierMapping>,
            com.google.gson.JsonDeserializer<NutsClassifierMapping> {

        @Override
        public NutsClassifierMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsClassifierMappingBuilder b = context.deserialize(json, DefaultNutsClassifierMappingBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsClassifierMapping src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsClassifierMappingBuilder().set(src));
            }
            return context.serialize(src);
        }
    }

    private static class NutsExecutorDescriptorAdapter implements
            com.google.gson.JsonSerializer<NutsArtifactCall>,
            com.google.gson.JsonDeserializer<NutsArtifactCall> {

        @Override
        public NutsArtifactCall deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            NutsArtifactCallBuilder b = context.deserialize(json, DefaultNutsArtifactCallBuilder.class);
            return b.build();
        }

        @Override
        public JsonElement serialize(NutsArtifactCall src, Type typeOfSrc, JsonSerializationContext context) {
            if (src != null) {
                return context.serialize(new DefaultNutsArtifactCallBuilder(src));
            }
            return context.serialize(src);
        }
    }

    private class NutsElementJsonAdapter implements
            com.google.gson.JsonSerializer<NutsElement>,
            com.google.gson.JsonDeserializer<NutsElement> {

        @Override
        public NutsElement deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return fromJsonElement(json);
        }

        @Override
        public JsonElement serialize(NutsElement src, Type typeOfSrc, JsonSerializationContext context) {
            return toJsonElement(src);
        }
    }

    private class XmlElementJsonAdapter implements
            com.google.gson.JsonSerializer<org.w3c.dom.Element>,
            com.google.gson.JsonDeserializer<org.w3c.dom.Element> {

        @Override
        public org.w3c.dom.Element deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return getWorkspace().formats().element().toXmlElement(fromJsonElement(json), null);
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Element src, Type typeOfSrc, JsonSerializationContext context) {
            return toJsonElement(convert(src, NutsElement.class));
        }
    }

    private class XmlDocumentJsonAdapter implements
            com.google.gson.JsonSerializer<org.w3c.dom.Document>,
            com.google.gson.JsonDeserializer<org.w3c.dom.Document> {

        @Override
        public org.w3c.dom.Document deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Document doc;
            try {
                doc = NutsXmlUtils.createDocument(getSession());
            } catch (ParserConfigurationException ex) {
                throw new JsonParseException(CoreStringUtils.exceptionToString(ex), ex);
            }
            Element ee = getWorkspace().formats().element().toXmlElement(fromJsonElement(json), doc);
            ee = (Element) doc.importNode(ee, true);
            doc.appendChild(ee);
            return doc;
        }

        @Override
        public JsonElement serialize(org.w3c.dom.Document src, Type typeOfSrc, JsonSerializationContext context) {
            NutsElement element = convert(src.getDocumentElement(), NutsElement.class);
            return toJsonElement(element);
        }
    }

    private class PathJsonAdapter implements
            com.google.gson.JsonSerializer<Path>,
            com.google.gson.JsonDeserializer<Path> {

        @Override
        public Path deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Paths.get(json.getAsString());
        }

        @Override
        public JsonElement serialize(Path src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    private class FileJsonAdapter implements
            com.google.gson.JsonSerializer<File>,
            com.google.gson.JsonDeserializer<File> {

        @Override
        public File deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new File(json.getAsString());
        }

        @Override
        public JsonElement serialize(File src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.getPath());
        }
    }

    private class DateJsonAdapter implements
            com.google.gson.JsonSerializer<Date>,
            com.google.gson.JsonDeserializer<Date> {

        @Override
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return new Date(dummyContext.builder().forDate(json.getAsString()).primitive().getDate().toEpochMilli());
        }

        @Override
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toInstant().toString());
        }
    }

    private class InstantJsonAdapter implements
            com.google.gson.JsonSerializer<Instant>,
            com.google.gson.JsonDeserializer<Instant> {

        @Override
        public Instant deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return dummyContext.builder().forDate(json.getAsString()).primitive().getDate();
        }

        @Override
        public JsonElement serialize(Instant src, Type typeOfSrc, JsonSerializationContext context) {
            return context.serialize(src.toString());
        }
    }

    public NutsElement fromJsonElement(JsonElement o) {
        JsonElement je = (JsonElement) o;
        if (je.isJsonNull()) {
            return dummyContext.builder().forNull();
        } else if (je.isJsonPrimitive()) {
            JsonPrimitive jr = je.getAsJsonPrimitive();
            if (jr.isString()) {
                return dummyContext.builder().forString(jr.getAsString());
            } else if (jr.isNumber()) {
                return dummyContext.builder().forNumber(jr.getAsNumber());
            } else if (jr.isBoolean()) {
                return dummyContext.builder().forBoolean(jr.getAsBoolean());
            } else {
                throw new IllegalArgumentException("Unsupported");
            }
        } else if (je.isJsonArray()) {
            return new NutsArrayElementJson(je.getAsJsonArray(), dummyContext);
        } else if (je.isJsonObject()) {
            return new NutsObjectElementJson((je.getAsJsonObject()), dummyContext);
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public JsonElement toJsonElement(NutsElement o) {
        switch (o.type()) {
            case BOOLEAN: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getBoolean());
            }
            case INTEGER:
            case FLOAT: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getNumber());
            }
            case STRING: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case DATE: {
                return new JsonPrimitive(((NutsPrimitiveElement) o).getString());
            }
            case NULL: {
                return JsonNull.INSTANCE;
            }
            case ARRAY: {
                JsonArray a = new JsonArray();
                for (NutsElement attribute : o.array().children()) {
                    a.add(toJsonElement(attribute));
                }
                return a;
            }
            case OBJECT: {
                JsonObject a = new JsonObject();
                Set<String> visited = new HashSet<String>();
                for (NutsNamedElement attribute : o.object().children()) {
                    String k = attribute.getName();
                    if (visited.contains(k)) {
                        throw new IllegalArgumentException("Unexpected");
                    }
                    visited.add(k);
                    a.add(k, toJsonElement(attribute.getValue()));
                }
                return a;
            }
            default: {
                throw new IllegalArgumentException("Unsupported " + o.type());
            }
        }
    }

    @Override
    public NutsElement toElement(Object o) {
        return convert(o,NutsElement.class);
    }
}
