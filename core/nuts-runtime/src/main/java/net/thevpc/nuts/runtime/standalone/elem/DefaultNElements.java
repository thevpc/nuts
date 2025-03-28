package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.io.*;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.Temporal;
import java.util.Date;
import java.util.function.Predicate;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NFormattable;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.tson.DefaultSearchFormatTson;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.standalone.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.reflect.NReflectRepository;

public class DefaultNElements extends DefaultFormatBase<NElements> implements NElements {

    //    public static final NutsPrimitiveElement NULL = new DefaultNPrimitiveElement(NutsElementType.NULL, null);
//    public static final NutsPrimitiveElement TRUE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, true);
//    public static final NutsPrimitiveElement FALSE = new DefaultNPrimitiveElement(NutsElementType.BOOLEAN, false);
    private static Predicate<Class<?>> DEFAULT_INDESTRUCTIBLE_FORMAT = new Predicate<Class<?>>() {
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
            if (Temporal.class.isAssignableFrom(x)) {
                return true;
            }
            if (java.util.Date.class.isAssignableFrom(x)) {
                return true;
            }
            return (
                    NText.class.isAssignableFrom(x)
                            || NElement.class.isAssignableFrom(x)
                            || NFormattable.class.isAssignableFrom(x)
                            || NMsg.class.isAssignableFrom(x)
            );
        }
    };
    private final DefaultNTextManagerModel model;
    private Object value;
    private NContentType contentType = NContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private Predicate<Class<?>> indestructibleObjects;


    public DefaultNElements(NWorkspace workspace) {
        super("element-format");
        this.model = NWorkspaceExt.of().getModel().textModel;
    }


    public boolean isLogProgress() {
        return logProgress;
    }

    public NElements setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    public boolean isTraceProgress() {
        return traceProgress;
    }

    public NElements setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public NElements setContentType(NContentType contentType) {
//        checkSession();
        if (contentType == null) {
            this.contentType = NContentType.JSON;
        } else {
//            switch (contentType) {
//                case TREE:
//                case TABLE:
//                case PLAIN: {
//                    throw new NutsIllegalArgumentException(session, "invalid content type " + contentType + ". Only structured content types are allowed.");
//                }
//            }
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public NElements json() {
        return setContentType(NContentType.JSON);
    }

    @Override
    public NElements yaml() {
        return setContentType(NContentType.YAML);
    }

    @Override
    public NElements tson() {
        return setContentType(NContentType.TSON);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NElements setValue(Object value) {
        this.value = value;
        return this;
    }

    @Override
    public NElementPath compilePath(String pathExpression) {
        return NElementPathFilter.compile(pathExpression);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NElements setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public <T> T parse(URL url, Class<T> clazz) {
        return parse(NPath.of(url), clazz);
    }

    private InputStream prepareInputStream(InputStream is, Object origin) {
        if (isLogProgress() || isTraceProgress()) {
            return NInputStreamMonitor.of()
                    .setSource(is)
                    .setOrigin(origin)
                    .setLogProgress(isLogProgress())
                    .setTraceProgress(isTraceProgress())
                    .setProgressFactory(getProgressFactory())
                    .create();
        }
        return is;
    }

    private InputStream prepareInputStream(NPath path) {
        if (isLogProgress()) {
            return NInputStreamMonitor.of()
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
    public <T> T parse(NPath path, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                try {
                    try (InputStream is = prepareInputStream(path)) {
                        return parse(new InputStreamReader(is), clazz);
                    } catch (NException ex) {
                        throw ex;
                    } catch (UncheckedIOException ex) {
                        throw new NIOException(ex);
                    } catch (RuntimeException ex) {
                        throw new NParseException(NMsg.ofC("unable to parse path %s", path), ex);
                    }
                } catch (IOException ex) {
                    throw new NParseException(NMsg.ofC("unable to parse path %s", path), ex);
                }
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(InputStream inputStream, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(inputStream, null)), clazz);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(String string, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new StringReader(string), clazz);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(byte[] bytes, Class<T> clazz) {
        switch (contentType) {
            case JSON:
            case YAML:
            case XML:
            case TSON: {
                return parse(new InputStreamReader(prepareInputStream(new ByteArrayInputStream(bytes), null)), clazz);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    @Override
    public <T> T parse(Reader reader, Class<T> clazz) {
        return (T) elementToObject(resolveStructuredFormat().parseElement(reader, createFactoryContext()), clazz);
    }

    @Override
    public <T> T parse(Path file, Class<T> clazz) {
        return parse(NPath.of(file), clazz);
    }

    @Override
    public <T> T parse(File file, Class<T> clazz) {
        return parse(NPath.of(file), clazz);
    }

    @Override
    public NElement parse(URL url) {
        return parse(url, NElement.class);
    }

    @Override
    public NElement parse(InputStream inputStream) {
        return parse(inputStream, NElement.class);
    }

    @Override
    public NElement parse(String string) {
        if (string == null || string.isEmpty()) {
            return ofNull();
        }
        return parse(string, NElement.class);
    }

    @Override
    public NElement parse(byte[] bytes) {
        return parse(bytes, NElement.class);
    }

    @Override
    public NElement parse(Reader reader) {
        return parse(reader, NElement.class);
    }

    @Override
    public NElement parse(Path file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(File file) {
        return parse(file, NElement.class);
    }

    @Override
    public NElement parse(NPath file) {
        return parse(file, NElement.class);
    }

    @Override
    public <T> T convert(Object any, Class<T> to) {
        if (to == null || to.isInstance(any)) {
            return (T) any;
        }
        NElement e = toElement(any);
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
    public NElement toElement(Object o) {
        return createFactoryContext().objectToElement(o, null);
    }

    @Override
    public <T> T fromElement(NElement o, Class<T> to) {
        return convert(o, to);
    }

    @Override
    public NPairElement ofPair(NElement key, NElement value) {
        return new DefaultNPairElement(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value,
                new NElementAnnotation[0], null
        );
    }

    @Override
    public NPairElement ofPair(String key, NElement value) {
        return ofPair(ofNameOrString(key), value);
    }

    @Override
    public NPairElementBuilder ofPairBuilder(NElement key, NElement value) {
        return new DefaultNPairElementBuilder(
                key == null ? ofNull() : key,
                value == null ? ofNull() : value
        );
    }

    //    @Override
//    public NutsPrimitiveElementBuilder forPrimitive() {
//        return new DefaultNPrimitiveElementBuilder(session);
//    }
    @Override
    public NObjectElementBuilder ofObjectBuilder() {
        return new DefaultNObjectElementBuilder();
    }

    @Override
    public NArrayElementBuilder ofArrayBuilder() {
        return new DefaultNArrayElementBuilder();
    }

    @Override
    public NArrayElement ofEmptyArray() {
        return ofArrayBuilder().build();
    }

    @Override
    public NArrayElement ofArray(NElement... items) {
        return ofArrayBuilder().addAll(items).build();
    }

    @Override
    public NObjectElement ofEmptyObject() {
        return ofObjectBuilder().build();
    }

    @Override
    public NPrimitiveElement ofBoolean(String value) {
        NOptional<Boolean> o = NLiteral.of(value).asBoolean();
        if (o.isEmpty()) {
            return ofNull();
        }
        return ofBoolean(o.get());
    }

    //    public NutsPrimitiveElement forNutsString(NutsString str) {
//        return str == null ? DefaultNPrimitiveElementBuilder.NULL : new DefaultNPrimitiveElement(NutsElementType.NUTS_STRING, str);
//    }
    @Override
    public NPrimitiveElement ofBoolean(boolean value) {
        //TODO: perhaps we can optimize this
        if (value) {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, true, null, null);
        } else {
            return new DefaultNPrimitiveElement(NElementType.BOOLEAN, false, null, null);
        }
    }

    public NPrimitiveElement ofString(String str) {
        return ofString(str, null);
    }

    public NPrimitiveElement ofString(String str, NStringLayout stringLayout) {
        return str == null ? ofNull() : new DefaultNStringElement(NElementType.STRING, str, stringLayout, null, null);
    }

    public NPrimitiveElement ofRegex(String str) {
        return str == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.REGEX, str, null, null);
    }

    public NPrimitiveElement ofName(String str) {
        return str == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.NAME, str, null, null);
    }

    @Override
    public NPrimitiveElement ofNameOrString(String value) {
        if (value == null) {
            return ofNull();
        }
        return NElements.isValidName(value) ? new DefaultNPrimitiveElement(NElementType.NAME, value, null, null)
                : new DefaultNStringElement(NElementType.STRING, value, null, null, null)
                ;
    }

    @Override
    public NCustomElement ofCustom(Object object) {
        NAssert.requireNonNull(object, "custom element");
        return new DefaultNCustomElement(object, null, null);
    }

    @Override
    public NPrimitiveElement ofTrue() {
        return ofBoolean(true);
    }

    @Override
    public NPrimitiveElement ofFalse() {
        return ofBoolean(false);
    }

    @Override
    public NPrimitiveElement ofInstant(Instant instant) {
        return instant == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.INSTANT, instant, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalDate(LocalDate localDate) {
        return localDate == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDate, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalDateTime(LocalDateTime localDateTime) {
        return localDateTime == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_DATE, localDateTime, null, null);
    }

    @Override
    public NPrimitiveElement ofLocalTime(LocalTime localTime) {
        return localTime == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.LOCAL_TIME, localTime, null, null);
    }

    @Override
    public NPrimitiveElement ofFloat(Float value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.FLOAT, value);
    }

    @Override
    public NPrimitiveElement ofInt(Integer value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.INTEGER, value);
    }

    @Override
    public NElement ofBinaryStream(NInputStreamProvider value) {
        return value == null ? ofNull() : new DefaultNBinaryStreamElement(value, null, null);
    }

    @Override
    public NBinaryStreamElementBuilder ofBinaryStreamBuilder() {
        return new DefaultNBinaryStreamElementBuilder();
    }

    @Override
    public NElement ofCharStream(NReaderProvider value) {
        return value == null ? ofNull() : new DefaultNCharStreamElement(value, null, null);
    }

    @Override
    public NCharStreamElementBuilder ofCharStreamBuilder() {
        return new DefaultNCharStreamElementBuilder();
    }

    @Override
    public NPrimitiveElement ofLong(Long value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.LONG, value);
    }

    @Override
    public NPrimitiveElement ofNull() {
        //perhaps we can optimize this?
        return new DefaultNPrimitiveElement(NElementType.NULL, null, null, null);
    }

    @Override
    public NPrimitiveElement ofNumber(String value) {
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
        throw new NParseException(NMsg.ofC("unable to parse number %s", value));
    }

    @Override
    public NPrimitiveElement ofInstant(Date value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, value.toInstant(), null, null);
    }

    @Override
    public NPrimitiveElement ofInstant(String value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNPrimitiveElement(NElementType.INSTANT, DefaultNLiteral.parseInstant(value).get(), null, null);
    }

    @Override
    public NPrimitiveElement ofByte(Byte value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.BYTE, value);
    }

    @Override
    public NPrimitiveElement ofShort(Short value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value);
    }

    @Override
    public NPrimitiveElement ofChar(Character value) {
        return value == null ? ofNull() : new DefaultNPrimitiveElement(NElementType.CHAR, value, null, null);
    }

    @Override
    public NPrimitiveElement ofDouble(Double value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.DOUBLE, value);
    }

    @Override
    public NPrimitiveElement ofFloat(Short value) {
        return value == null ? ofNull() : new DefaultNNumberElement(NElementType.SHORT, value);
    }

    @Override
    public NPrimitiveElement ofBigDecimal(BigDecimal value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value);
    }

    @Override
    public NPrimitiveElement ofBigInteger(BigInteger value) {
        if (value == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_INTEGER, value);
    }

    @Override
    public NUpletElementBuilder ofUpletBuilder() {
        return new DefaultNUpletElementBuilder();
    }

    @Override
    public NUpletElement ofEmptyUplet() {
        return ofUpletBuilder().build();
    }

    @Override
    public NUpletElement ofUplet(String name, NElement... items) {
        return ofUpletBuilder().name(name).addAll(items).build();
    }

    @Override
    public NPrimitiveElement ofDoubleComplex(double real) {
        return ofDoubleComplex(real, 0);
    }

    @Override
    public NPrimitiveElement ofDoubleComplex(double real, double imag) {
        return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, new NDoubleComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofFloatComplex(float real) {
        return ofFloatComplex(real, 0);
    }

    @Override
    public NPrimitiveElement ofFloatComplex(float real, float imag) {
        return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, new NFloatComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofBigComplex(BigDecimal real) {
        return ofBigComplex(real, BigDecimal.ZERO);
    }

    @Override
    public NPrimitiveElement ofBigComplex(BigDecimal real, BigDecimal imag) {
        if (real == null && imag == null) {
            return ofNull();
        }
        return new DefaultNNumberElement(NElementType.BIG_COMPLEX, new NBigComplex(real, imag));
    }

    @Override
    public NPrimitiveElement ofNumber(Number value) {
        if (value == null) {
            return ofNull();
        }
        switch (value.getClass().getName()) {
            case "java.lang.Byte":
                return new DefaultNNumberElement(NElementType.BYTE, value);
            case "java.lang.Short":
                return new DefaultNNumberElement(NElementType.SHORT, value);
            case "java.lang.Integer":
                return new DefaultNNumberElement(NElementType.INTEGER, value);
            case "java.lang.Long":
                return new DefaultNNumberElement(NElementType.LONG, value);
            case "java.math.BigInteger":
                return new DefaultNNumberElement(NElementType.BIG_INTEGER, value);
            case "java.lang.float":
                return new DefaultNNumberElement(NElementType.FLOAT, value);
            case "java.lang.Double":
                return new DefaultNNumberElement(NElementType.DOUBLE, value);
            case "java.math.BigDecimal":
                return new DefaultNNumberElement(NElementType.BIG_DECIMAL, value);
        }
        if (value instanceof NDoubleComplex) {
            return new DefaultNNumberElement(NElementType.DOUBLE_COMPLEX, value);
        }
        if (value instanceof NBigComplex) {
            return new DefaultNNumberElement(NElementType.BIG_COMPLEX, value);
        }
        if (value instanceof NFloatComplex) {
            return new DefaultNNumberElement(NElementType.FLOAT_COMPLEX, value);
        }
        // ???
        return new DefaultNNumberElement(NElementType.FLOAT, value);
    }

    public Predicate<Class<?>> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public NElements setIndestructibleFormat() {
        return setIndestructibleObjects(DEFAULT_INDESTRUCTIBLE_FORMAT);
    }

    public NElements setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter) {
        this.indestructibleObjects = destructTypeFilter;
        return this;
    }

    @Override
    public NIterableFormat iter(NPrintStream writer) {
        switch (getContentType()) {
            case JSON:
                return new DefaultSearchFormatJson(writer, new NFetchDisplayOptions());
            case TSON:
                return new DefaultSearchFormatTson(writer, new NFetchDisplayOptions());
            case XML:
                return new DefaultSearchFormatXml(writer, new NFetchDisplayOptions());
            case PLAIN:
                return new DefaultSearchFormatPlain(writer, new NFetchDisplayOptions());
            case TABLE:
                return new DefaultSearchFormatTable(writer, new NFetchDisplayOptions());
            case TREE:
                return new DefaultSearchFormatTree(writer, new NFetchDisplayOptions());
            case PROPS:
                return new DefaultSearchFormatProps(writer, new NFetchDisplayOptions());
        }
        throw new NUnsupportedOperationException(NMsg.ofC("unsupported iterator for %s", getContentType()));
    }

    @Override
    public <T> NElements setMapper(Class<T> type, NElementMapper<T> mapper) {
        ((DefaultNElementFactoryService) getElementFactoryService())
                .setMapper(type, mapper);
        return this;
    }

    private NElementStreamFormat resolveStructuredFormat() {
        switch (contentType) {
            case JSON: {
                return model.getJsonMan();
            }
            case YAML: {
                return model.getYamlMan();
            }
            case XML: {
                return model.getXmlMan();
            }
            case TSON: {
                return model.getTsonMan();
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("invalid content type %s. Only structured content types are allowed.", contentType));
    }

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(NWorkspace.of(), this, reflectRepository);
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
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    private void print(NPrintStream out, NElementStreamFormat format) {
        NElement elem = toElement(value);
        if (out.isNtf()) {
            NPrintStream bos = NMemoryPrintStream.of();
            format.printElement(elem, bos, compact, createFactoryContext());
            out.print(NText.ofCode(getContentType().id(), bos.toString()));
        } else {
            format.printElement(elem, out, compact, createFactoryContext());
        }
        out.flush();
    }

    @Override
    public void print(NPrintStream out) {
        if (contentType == NContentType.PLAIN) {
            print(out, model.getJsonMan());
        } else {
            print(out, resolveStructuredFormat());
        }
    }

    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().elementToObject(o, type);
    }

    public NElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    @Override
    public NElements setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }

    @Override
    public NMatrixElementBuilder ofMatrixBuilder() {
        throw new NUnsupportedOperationException(NMsg.ofC("not implemented yet ofMatrixBuilder()"));
    }

    public NElementComments ofMultiLineComments(String a) {
        return new NElementCommentsImpl(new NElementComment[]{ofMultiLineComment(a)}, null);
    }

    public NElementComments ofSingleLineComments(String a) {
        return new NElementCommentsImpl(new NElementComment[]{ofSingleLineComment(a)}, null);
    }

    public NElementComments ofComments(NElementComment[] leading, NElementComment[] trailing) {
        return new NElementCommentsImpl(leading, trailing);
    }


    public NElementComment ofMultiLineComment(String a) {
        return NElementCommentImpl.ofMultiLine(a);
    }

    public NElementComment ofSingleLineComment(String a) {
        return NElementCommentImpl.ofSingleLine(a);
    }


}
