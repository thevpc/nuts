package net.thevpc.nuts.runtime.core.format.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;

import net.thevpc.nuts.runtime.bundles.io.ByteArrayPrintStream;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.core.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.core.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.runtime.core.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.core.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.core.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsElementFormat extends DefaultFormatBase<NutsElementFormat> implements NutsElementFormat {
    public static final NutsPrimitiveElement NULL = new DefaultNutsPrimitiveElement(NutsElementType.NULL, null);
    public static final NutsPrimitiveElement TRUE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true);
    public static final NutsPrimitiveElement FALSE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false);

    private Object value;
    private NutsContentType contentType = NutsContentType.JSON;
    private boolean compact;
    private final DefaultNutsTextManagerModel model;
    private Predicate<Type> destructTypeFilter;

    public DefaultNutsElementFormat(DefaultNutsTextManagerModel model) {
        super(model.getWorkspace(), "element-format");
        this.model = model;
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
    }

    @Override
    public NutsElementFormat setContentType(NutsContentType contentType) {
        checkSession();
        if (contentType == null) {
            this.contentType = NutsContentType.JSON;
        } else {
            switch (contentType) {
                case TREE:
                case TABLE:
                case PLAIN: {
                    throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
                }
            }
            this.contentType = contentType;
        }
        return this;
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
        checkSession();

        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = NutsWorkspaceUtils.of(getSession()).openURL(url)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NutsIOException(getSession(), ex);
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getSession(), "unable to parse url " + url, ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(inputStream), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(new ByteArrayInputStream(bytes)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    private NutsElementStreamFormat resolveStucturedFormat() {
        checkSession();
        switch (contentType) {
            case JSON: {
                return model.getJsonMan(getSession());
            }
            case YAML: {
                return model.getYamlMan(getSession());
            }
            case XML: {
                return model.getXmlMan(getSession());
            }
            case TSON: {
                throw new IllegalArgumentException("tson not supported yet");
            }
        }
        throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStucturedFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    private DefaultNutsElementFactoryContext createFactoryContext() {
        return new DefaultNutsElementFactoryContext(this);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        checkSession();
        try (Reader r = Files.newBufferedReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        checkSession();
        try (FileReader r = new FileReader(file)) {
            return parse(r, clazz);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), ex);
        }
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

//    public String getDefaulTagName() {
//        return defaultName;
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
//    public String getAttributePrefix() {
//        return attributePrefix;
//    }
//
//    public String getTypeAttributeName() {
//        return typeAttribute;
//    }
    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NutsElement e = toElement(any);
        return (T) elementToObject(e, to);
    }

    private void print(PrintStream out, NutsElementStreamFormat format) {
        checkSession();
        NutsElement elem = toElement(value);
        if (getSession().getWorkspace().term().setSession(getSession()).isFormatted(out)) {
            ByteArrayPrintStream bos = new ByteArrayPrintStream();
            format.printElement(elem, bos, compact, createFactoryContext());
            out.print(getSession().getWorkspace().formats().text().forCode(getContentType().id(), bos.toString()));
        } else {
            format.printElement(elem, out, compact, createFactoryContext());
        }
        out.flush();
    }

    @Override
    public void print(PrintStream out) {
        print(out, resolveStucturedFormat());
    }

//    @Override
//    public NutsElement objectToElement(Object o) {
//        return convert(o, NutsElement.class);
//    }
    @Override
    public Object destruct(Object any) {
        return createFactoryContext().destruct(any, null);
    }

    @Override
    public NutsElement toElement(Object o) {
        return createFactoryContext().objectToElement(o, null);
    }

    public Object elementToObject(NutsElement o, Type type) {
        return createFactoryContext().elementToObject(o, type);
    }

//    @Override
//    public NutsElementEntryBuilder forEntry() {
//        return new DefaultNutsElementEntryBuilder(getSession());
//    }

    @Override
    public NutsElementEntry forEntry(NutsElement key, NutsElement value) {
        return new DefaultNutsElementEntry(
                key == null ? forNull() : key,
                value == null ? forNull() : value
        );
    }

//    @Override
//    public NutsPrimitiveElementBuilder forPrimitive() {
//        return new DefaultNutsPrimitiveElementBuilder(getSession());
//    }

    @Override
    public NutsObjectElementBuilder forObject() {
        return new DefaultNutsObjectElementBuilder(getSession());
    }

    @Override
    public NutsArrayElementBuilder forArray() {
        return new DefaultNutsArrayElementBuilder(getSession());
    }

    public NutsElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService(getSession());
    }

    public NutsPrimitiveElement forString(String str) {
        return str == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.STRING, str);
    }

//    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNutsPrimitiveElementBuilder.NULL : new DefaultNutsPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NutsPrimitiveElement forBoolean(boolean value) {
        return value ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forBoolean(String value) {
        return CoreBooleanUtils.parseBoolean(value, false, false) ? TRUE : FALSE;
    }

    @Override
    public NutsPrimitiveElement forTrue() {
        return TRUE;
    }

    @Override
    public NutsPrimitiveElement forNull() {
        return NULL;
    }

    @Override
    public NutsPrimitiveElement forInstant(Instant instant) {
        return instant == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, instant);
    }

    @Override
    public NutsPrimitiveElement forByte(Byte value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value);
    }

    @Override
    public NutsPrimitiveElement forInt(Integer value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
    }

    @Override
    public NutsPrimitiveElement forLong(Long value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.LONG, value);
    }

    @Override
    public NutsPrimitiveElement forDouble(Double value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value);
    }

    @Override
    public NutsPrimitiveElement forFloat(Float value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
    }

    @Override
    public NutsPrimitiveElement forFloat(Short value) {
        return value == null ? NULL : new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value);
    }

    @Override
    public NutsPrimitiveElement forNumber(Number value) {
        if (value == null) {
            return forNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value);
            case "java.lang.Short":
                return new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value);
            case "java.lang.Integer":
                return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value);
            case "java.lang.Long":
                return new DefaultNutsPrimitiveElement(NutsElementType.LONG, value);
            case "java.math.BigInteger":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_INTEGER, value);
            case "java.lang.float":
                return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
            case "java.lang.Double":
                return new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value);
            case "java.math.BigDecimal":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_DECIMAL, value);
        }
        // ???
        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value);
    }

    @Override
    public NutsPrimitiveElement forFalse() {
        return FALSE;
    }

    public Predicate<Type> getDestructTypeFilter() {
        return destructTypeFilter;
    }

    public NutsElementFormat setDestructTypeFilter(Predicate<Type> destructTypeFilter) {
        this.destructTypeFilter = destructTypeFilter;
        return this;
    }

    @Override
    public NutsPrimitiveElement forNumber(String value) {
        checkSession();
        if (value == null) {
            return forNull();
        }
        if (value.indexOf('.') >= 0) {
            try {
                return forNumber(Double.parseDouble(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigDecimal(value));
            } catch (Exception ex) {

            }
        } else {
            try {
                return forNumber(Integer.parseInt(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(Long.parseLong(value));
            } catch (Exception ex) {

            }
            try {
                return forNumber(new BigInteger(value));
            } catch (Exception ex) {

            }
        }
        throw new NutsParseException(getSession(), "unable to parse number " + value);
    }

    @Override
    public NutsPrimitiveElement forInstant(Date value) {
        if (value == null) {
            return forNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, value.toInstant());
    }

    @Override
    public NutsPrimitiveElement forInstant(String value) {
        if (value == null) {
            return forNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, DefaultNutsPrimitiveElement.parseDate(value));
    }

    @Override
    public NutsIterableFormat iter(PrintStream writer) {
        switch (getContentType()){
            case JSON:return new DefaultSearchFormatJson(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
            case XML:return new DefaultSearchFormatXml(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
            case PLAIN:return new DefaultSearchFormatPlain(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
            case TABLE:return new DefaultSearchFormatTable(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
            case TREE:return new DefaultSearchFormatTree(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
            case PROPS:return new DefaultSearchFormatProps(getSession(),writer,new NutsFetchDisplayOptions(getSession().getWorkspace()));
        }
        throw new NutsIllegalArgumentException(getSession(),"unsupported iterator for "+getContentType());
    }
}
