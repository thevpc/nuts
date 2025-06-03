package net.thevpc.nuts.runtime.standalone.format.elem;

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
import java.util.Arrays;
import java.util.Date;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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


    public DefaultNElements() {
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
    public NElements xml() {
        return setContentType(NContentType.XML);
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

    public NElement normalize(NElement e) {
        return resolveStructuredFormat().normalize(e == null ? NElements.ofNull() : e);
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
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(this, reflectRepository);
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
}
