package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.json.MinimalJson;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.optional.gson.GsonItemSerializeManager;
import net.thevpc.nuts.runtime.core.format.xml.DefaultXmlNutsElementStreamFormat;
import net.thevpc.nuts.runtime.optional.gson.OptionalGson;

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
    private NutsElementStreamFormat jsonMan;
    private NutsElementStreamFormat xmlMan;

    public DefaultNutsElementFormat(NutsWorkspace ws) {
        super(ws, "element-format");
        nvalueFactory = new DefaultNutsElementFactoryService(ws);
        builder = new DefaultNutsElementBuilder(ws);
        if (false && OptionalGson.isAvailable()) {
            jsonMan = new GsonItemSerializeManager(this);
        } else {
            jsonMan=new MinimalJson(ws);
        }
        xmlMan = new DefaultXmlNutsElementStreamFormat(this);
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
    public NutsElementBuilder elements() {
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
                return (T) elementToObject(jsonMan.parseElement(reader, getValidSession()), clazz);
            }
            case XML: {
                return (T) elementToObject(xmlMan.parseElement(reader, getValidSession()), clazz);
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

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NutsElement e = objectToElement(any, any.getClass());
        return (T) elementToObject(e, to);
    }

    private void print(PrintStream out, NutsElementStreamFormat format, String contentType) {
        NutsElement elem = objectToElement(value, null);
        if (getWorkspace().io().term().isFormatted(out)) {
            ByteArrayPrintStream bos = new ByteArrayPrintStream();
            format.printElement(elem, bos, compact, getValidSession());
            out.print(getWorkspace().formats().text().code(contentType, bos.toString()));
        } else {
            format.printElement(elem, out, compact, getValidSession());
        }
        out.flush();
    }

    @Override
    public void print(PrintStream out) {
        switch (getContentType()) {
            case JSON: {
                print(out, jsonMan, "json");
                break;
            }
            case XML: {
                print(out, xmlMan, "xml");
                break;
            }
        }
    }

//    @Override
//    public NutsElement objectToElement(Object o) {
//        return convert(o, NutsElement.class);
//    }
    @Override
    public NutsElement objectToElement(Object o, Type expectedType) {
        return nvalueFactory.createElement(o, expectedType, this);
    }

    @Override
    public Object elementToObject(NutsElement o, Type type) {
        return nvalueFactory.createObject(o, type, this);
    }

    @Override
    public NutsElement defaultObjectToElement(Object o, Type expectedType) {
        return nvalueFactory.defaultCreateElement(o, expectedType, this);
    }

    @Override
    public Object defaultElementToObject(NutsElement o, Type type) {
        return nvalueFactory.defaultCreateObject(o, type, this);
    }

}
