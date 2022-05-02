package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.function.Predicate;

import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NutsIterableFormat;
import net.thevpc.nuts.io.*;
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
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsProgressFactory;

public class DefaultNutsElements extends DefaultFormatBase<NutsElements> implements NutsElements {

    //    public static final NutsPrimitiveElement NULL = new DefaultNutsPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false);
    private static Predicate<Class> DEFAULT_INDESTRUCTIBLE_FORMAT = new Predicate<Class>() {
        @Override
        public boolean test(Class x) {
            switch (x.getName()) {
                case "boolean":
                case "byte":
                case "short":
                case "int":
                case "long":
                case "float":
                case "double":
                case "java.lang.String":
                case "java.lang.StringBuilder":
                case "java.lang.Boolean":
                case "java.lang.Byte":
                case "java.lang.Short":
                case "java.lang.Integer":
                case "java.lang.Long":
                case "java.lang.Float":
                case "java.lang.Double":
                case "java.math.BigDecimal":
                case "java.math.BigInteger":
                case "java.util.Date":
                case "java.sql.Time":
                    return true;
            }
            if(Temporal.class.isAssignableFrom(x)){
                return true;
            }
            if(java.util.Date.class.isAssignableFrom(x)){
                return true;
            }
            return (
                    NutsString.class.isAssignableFrom(x)
                            || NutsElement.class.isAssignableFrom(x)
                            || NutsFormattable.class.isAssignableFrom(x)
                            || NutsMessage.class.isAssignableFrom(x)
                            || NutsMessageFormattable.class.isAssignableFrom(x)
            );
        }
    };
    private final DefaultNutsTextManagerModel model;
    private Object value;
    private NutsContentType contentType = NutsContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NutsProgressFactory progressFactory;
    private Predicate<Class> indestructibleObjects;

    public DefaultNutsElements(NutsSession session) {
        super(session, "element-format");
        this.model = NutsWorkspaceExt.of(session).getModel().textModel;
    }


    public boolean isLogProgress() {
        return logProgress;
    }

    public NutsElements setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    public boolean isTraceProgress() {
        return traceProgress;
    }

    public NutsElements setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NutsContentType getContentType() {
        return contentType;
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
    public NutsElements json() {
        return setContentType(NutsContentType.JSON);
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
        return parse(NutsPath.of(url,getSession()),clazz);
    }

    private InputStream prepareInputStream(InputStream is,Object origin){
        if(isLogProgress() || isTraceProgress()){
            return NutsInputStreamMonitor.of(getSession())
                    .setSource(is)
                    .setOrigin(origin)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return is;
    }
    private InputStream prepareInputStream(NutsPath path){
        if(isLogProgress()){
            return NutsInputStreamMonitor.of(getSession())
                    .setSource(path)
                    .setOrigin(path)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return path.getInputStream();
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
                    try (InputStream is = prepareInputStream(path)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NutsException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NutsIOException(getSession(), ex);
                    } catch (RuntimeException ex) {
                        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse path %s", path), ex);
                    }
                } catch (IOException ex) {
                    throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse path %s", path), ex);
                }
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(inputStream,null)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.", contentType));
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        checkSession();
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(new ByteArrayInputStream(bytes),null)), clazz);
            }
        }
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStructuredFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        checkSession();
        return parse(NutsPath.of(file, getSession()), clazz);
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        checkSession();
        return parse(NutsPath.of(file, getSession()), clazz);
    }

    @Override
    public NutsElement parse(URL url) {
        return parse(url, NutsElement.class);
    }

    @Override
    public NutsElement parse(InputStream inputStream) {
        return parse(inputStream, NutsElement.class);
    }

    @Override
    public NutsElement parse(String string) {
        if (string == null || string.isEmpty()) {
            return ofNull();
        }
        return parse(string, NutsElement.class);
    }

    @Override
    public NutsElement parse(byte[] bytes) {
        return parse(bytes, NutsElement.class);
    }

    @Override
    public NutsElement parse(Reader reader) {
        return parse(reader, NutsElement.class);
    }

    @Override
    public NutsElement parse(Path file) {
        return parse(file, NutsElement.class);
    }

    @Override
    public NutsElement parse(File file) {
        return parse(file, NutsElement.class);
    }

    @Override
    public NutsElement parse(NutsPath file) {
        return parse(file, NutsElement.class);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NutsElement e = toElement(any);
        return (T) elementToObject(e, to);
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

    @Override
    public <T> T fromElement(NutsElement o, Class<T> to) {
        return convert(o,to);
    }

    //    @Override
//    public NutsElementEntryBuilder forEntry() {
//        return new DefaultNutsElementEntryBuilder(getSession());
//    }
    @Override
    public NutsElementEntry ofEntry(NutsElement key, NutsElement value) {
        return new DefaultNutsElementEntry(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value
        );
    }

    //    @Override
//    public NutsPrimitiveElementBuilder forPrimitive() {
//        return new DefaultNutsPrimitiveElementBuilder(getSession());
//    }
    @Override
    public NutsObjectElementBuilder ofObject() {
        return new DefaultNutsObjectElementBuilder(getSession());
    }

    @Override
    public NutsArrayElementBuilder ofArray() {
        return new DefaultNutsArrayElementBuilder(getSession());
    }

    @Override
    public NutsArrayElement ofEmptyArray() {
        return ofArray().build();
    }

    @Override
    public NutsObjectElement ofEmptyObject() {
        return ofObject().build();
    }

    @Override
    public NutsPrimitiveElement ofBoolean(String value) {
        NutsOptional<Boolean> o = NutsValue.of(value).asBoolean();
        if(o.isEmpty()){
            return ofNull();
        }
        return ofBoolean(o.get());
    }

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNutsPrimitiveElementBuilder.NULL : new DefaultNutsPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NutsPrimitiveElement ofBoolean(boolean value) {
        checkSession();
        //TODO: perhaps we can optimize this
        if (value) {
            return new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, true, getSession());
        } else {
            return new DefaultNutsPrimitiveElement(NutsElementType.BOOLEAN, false, getSession());
        }
    }

    public NutsPrimitiveElement ofString(String str) {
        checkSession();
        return str == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.STRING, str, getSession());
    }

    @Override
    public NutsCustomElement ofCustom(Object object) {
        checkSession();
        if(object ==null){
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("custom element cannot be null"));
        }
        return new DefaultNutsCustomElement(object, getSession());
    }

    @Override
    public NutsPrimitiveElement ofTrue() {
        return ofBoolean(true);
    }

    @Override
    public NutsPrimitiveElement ofFalse() {
        return ofBoolean(false);
    }

    @Override
    public NutsPrimitiveElement ofInstant(Instant instant) {
        checkSession();
        return instant == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, instant, getSession());
    }

    @Override
    public NutsPrimitiveElement ofFloat(Float value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofInt(Integer value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofLong(Long value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.LONG, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofNull() {
        checkSession();
        //perhaps we can optimize this?
        return new DefaultNutsPrimitiveElement(NutsElementType.NULL, null, getSession());
    }

    @Override
    public NutsPrimitiveElement ofNumber(String value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        if (value.indexOf('.') >= 0) {
            try {
                return ofNumber(Double.parseDouble(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigDecimal(value));
            } catch (Exception ex) {

            }
        } else {
            try {
                return ofNumber(Integer.parseInt(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(Long.parseLong(value));
            } catch (Exception ex) {

            }
            try {
                return ofNumber(new BigInteger(value));
            } catch (Exception ex) {

            }
        }
        throw new NutsParseException(getSession(), NutsMessage.cstyle("unable to parse number %s", value));
    }

    @Override
    public NutsPrimitiveElement ofInstant(Date value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, value.toInstant(), getSession());
    }

    @Override
    public NutsPrimitiveElement ofInstant(String value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        return new DefaultNutsPrimitiveElement(NutsElementType.INSTANT, DefaultNutsValue.parseInstant(value).get(getSession()), getSession());
    }

    @Override
    public NutsPrimitiveElement ofByte(Byte value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofDouble(Double value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofFloat(Short value) {
        checkSession();
        return value == null ? ofNull() : new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value, getSession());
    }

    @Override
    public NutsPrimitiveElement ofNumber(Number value) {
        checkSession();
        if (value == null) {
            return ofNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNutsPrimitiveElement(NutsElementType.BYTE, value, getSession());
            case "java.lang.Short":
                return new DefaultNutsPrimitiveElement(NutsElementType.SHORT, value, getSession());
            case "java.lang.Integer":
                return new DefaultNutsPrimitiveElement(NutsElementType.INTEGER, value, getSession());
            case "java.lang.Long":
                return new DefaultNutsPrimitiveElement(NutsElementType.LONG, value, getSession());
            case "java.math.BigInteger":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_INTEGER, value, getSession());
            case "java.lang.float":
                return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value, getSession());
            case "java.lang.Double":
                return new DefaultNutsPrimitiveElement(NutsElementType.DOUBLE, value, getSession());
            case "java.math.BigDecimal":
                return new DefaultNutsPrimitiveElement(NutsElementType.BIG_DECIMAL, value, getSession());
        }
        // ???
        return new DefaultNutsPrimitiveElement(NutsElementType.FLOAT, value, getSession());
    }

    public Predicate<Class> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public NutsElements setIndestructibleFormat() {
        return setIndestructibleObjects(DEFAULT_INDESTRUCTIBLE_FORMAT);
    }

    public NutsElements setIndestructibleObjects(Predicate<Class> destructTypeFilter) {
        this.indestructibleObjects = destructTypeFilter;
        return this;
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
    public <T> NutsElements setMapper(Class<T> type, NutsElementMapper<T> mapper) {
        checkSession();
        ((DefaultNutsElementFactoryService) getElementFactoryService())
                .setMapper(type, mapper);
        return this;
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
        throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    private DefaultNutsElementFactoryContext createFactoryContext() {
        DefaultNutsElementFactoryContext c = new DefaultNutsElementFactoryContext(this);
        switch (getContentType()) {
            case XML:
            case JSON:
            case TSON:
            case YAML: {
                c.setNtf(false);
                break;
            }
        }
        return c;
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        return false;
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

    public Object elementToObject(NutsElement o, Type type) {
        return createFactoryContext().elementToObject(o, type);
    }

    public NutsElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService(getSession());
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsProgressFactory getProgressFactory() {
        return progressFactory;
    }

    @Override
    public NutsElements setProgressFactory(NutsProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }
}
