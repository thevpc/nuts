package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.function.Predicate;

import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.standalone.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTextManagerModel;
import net.thevpc.nuts.runtime.standalone.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsElements extends DefaultFormatBase<NutsElements> implements NutsElements {

//    public static final NutsPrimitiveElement NULL = new DefaultNutsPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false);

    private Object value;
    private NutsContentType contentType = NutsContentType.JSON;
    private boolean compact;
    private final DefaultNutsTextManagerModel model;
    private Predicate<Type> destructTypeFilter;

    public DefaultNutsElements(NutsSession session) {
        super(session, "element-format");
        this.model = NutsWorkspaceExt.of(session).getModel().textModel;
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
    }

    @Override
    public NutsElements json() {
        return setContentType(NutsContentType.JSON);
    }

    @Override
    public NutsElements setContentType(NutsContentType contentType) {
//        checkSession();
        if (contentType == null) {
            this.contentType = NutsContentType.JSON;
        } else {
//            switch (contentType) {
//                case TREE:
//                case TABLE:
//                case PLAIN: {
//                    throw new NutsIllegalArgumentException(getSession(), "invalid content type " + contentType + ". Only structured content types are allowed.");
//                }
//            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NutsElements setCompact(boolean compact) {
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
                        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s" , url), ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse url %s", url), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
    }

    @Override
    public <T> T parse(NutsPath path, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = path.getInputStream()) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NutsIOException(getSession(), ex);
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse path %s" , path), ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse path %s", path), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
    }

    private NutsElementStreamFormat resolveStructuredFormat() {
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
                throw new NutsUnsupportedEnumException(getSession(), contentType);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.",contentType));
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStructuredFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    private DefaultNutsElementFactoryContext createFactoryContext() {
        DefaultNutsElementFactoryContext c = new DefaultNutsElementFactoryContext(this);
        switch (getContentType()){
            case XML:
            case JSON:
            case TSON:
            case YAML:{
                c.setNtf(false);
                break;
            }
        }
        return c;
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        checkSession();
        return parse(NutsPath.of(file,getSession()),clazz);
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        checkSession();
        return parse(NutsPath.of(file,getSession()),clazz);
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
    public NutsElements setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public NutsElementPath compilePath(String pathExpression) {
        checkSession();
        return NutsElementPathFilter.compile(pathExpression, getSession());
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

    private void print(NutsPrintStream out, NutsElementStreamFormat format) {
        checkSession();
        NutsElement elem = toElement(value);
        if (out.isNtf()) {
            NutsPrintStream bos = NutsMemoryPrintStream.of(getSession());
            format.printElement(elem, bos, compact, createFactoryContext());
            out.print(NutsTexts.of(getSession()).ofCode(getContentType().id(), bos.toString()));
        } else {
            format.printElement(elem, out, compact, createFactoryContext());
        }
        out.flush();
    }

    @Override
    public void print(NutsPrintStream out) {
        print(out, resolveStructuredFormat());
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

    @Override
    public void setMapper(Class type, NutsElementMapper mapper) {
        checkSession();
        ((DefaultNutsElementFactoryService) getElementFactoryService())
                .setMapper(type, mapper);
    }

    public NutsElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService(getSession());
    }

    public NutsPrimitiveElement forString(String str) {
        checkSession();
        return str == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.STRING, str,getSession());
    }

//    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNutsPrimitiveElementBuilder.NULL : new DefaultNutsPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NutsPrimitiveElement forBoolean(boolean value) {
        checkSession();
        //TODO: perhaps we can optimize this
        if(value) {
            return new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true,getSession());
        }else{
            return new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false,getSession());
        }
    }

    @Override
    public NutsPrimitiveElement forBoolean(String value) {
        return forBoolean(NutsUtilStrings.parseBoolean(value, false, false));
    }

    @Override
    public NutsPrimitiveElement forTrue() {
        return forBoolean(true);
    }

    @Override
    public NutsPrimitiveElement forNull() {
        checkSession();
        //perhaps we can optimize this?
        return new DefaultNutsPrimitiveElement(NutsElementType.NULL, null,getSession());
    }

    @Override
    public NutsPrimitiveElement forInstant(Instant instant) {
        checkSession();
        return instant == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, instant,getSession());
    }

    @Override
    public NutsPrimitiveElement forByte(Byte value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forInt(Integer value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forLong(Long value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.LONG, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forDouble(Double value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forFloat(Float value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forFloat(Short value) {
        checkSession();
        return value == null ? forNull() : new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forNumber(Number value) {
        checkSession();
        if (value == null) {
            return forNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value,getSession());
            case "java.lang.Short":
                return new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value,getSession());
            case "java.lang.Integer":
                return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value,getSession());
            case "java.lang.Long":
                return new DefaultNutsPrimitiveElement(NutsElementType.LONG, value,getSession());
            case "java.math.BigInteger":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_INTEGER, value,getSession());
            case "java.lang.float":
                return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value,getSession());
            case "java.lang.Double":
                return new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value,getSession());
            case "java.math.BigDecimal":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_DECIMAL, value,getSession());
        }
        // ???
        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value,getSession());
    }

    @Override
    public NutsPrimitiveElement forFalse() {
        return forBoolean(false);
    }

    public Predicate<Type> getDestructTypeFilter() {
        return destructTypeFilter;
    }

    public NutsElements setDestructTypeFilter(Predicate<Type> destructTypeFilter) {
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
        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse number %s", value));
    }

    @Override
    public NutsPrimitiveElement forInstant(Date value) {
        checkSession();
        if (value == null) {
            return forNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, value.toInstant(),getSession());
    }

    @Override
    public NutsPrimitiveElement forInstant(String value) {
        checkSession();
        if (value == null) {
            return forNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, DefaultNutsPrimitiveElement.parseDate(value),getSession());
    }

    @Override
    public NutsIterableFormat iter(NutsPrintStream writer) {
        switch (getContentType()) {
            case JSON:
                return new DefaultSearchFormatJson(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
            case XML:
                return new DefaultSearchFormatXml(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
            case PLAIN:
                return new DefaultSearchFormatPlain(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
            case TABLE:
                return new DefaultSearchFormatTable(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
            case TREE:
                return new DefaultSearchFormatTree(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
            case PROPS:
                return new DefaultSearchFormatProps(getSession(), writer, new NutsFetchDisplayOptions(getSession()));
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unsupported iterator for " + getContentType()));
    }

    @Override
    public NutsElement parse(URL url) {
        return parse(url,NutsElement.class);
    }

    @Override
    public NutsElement parse(InputStream inputStream) {
        return parse(inputStream,NutsElement.class);
    }

    @Override
    public NutsElement parse(String string) {
        if(string==null || string.isEmpty()){
            return forNull();
        }
        return parse(string,NutsElement.class);
    }

    @Override
    public NutsElement parse(byte[] bytes) {
        return parse(bytes,NutsElement.class);
    }

    @Override
    public NutsElement parse(Reader reader) {
        return parse(reader,NutsElement.class);
    }

    @Override
    public NutsElement parse(Path file) {
        return parse(file,NutsElement.class);
    }

    @Override
    public NutsElement parse(File file) {
        return parse(file,NutsElement.class);
    }

    @Override
    public NutsElement parse(NutsPath file) {
        return parse(file,NutsElement.class);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
