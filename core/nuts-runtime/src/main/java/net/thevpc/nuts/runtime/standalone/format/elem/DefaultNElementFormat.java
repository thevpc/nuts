package net.thevpc.nuts.runtime.standalone.format.elem;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NUnsupportedOperationException;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.elem.NElementFormat;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.format.NIterableFormat;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultSearchFormatJson;
import net.thevpc.nuts.runtime.standalone.format.plain.DefaultSearchFormatPlain;
import net.thevpc.nuts.runtime.standalone.format.props.DefaultSearchFormatProps;
import net.thevpc.nuts.runtime.standalone.format.table.DefaultSearchFormatTable;
import net.thevpc.nuts.runtime.standalone.format.tree.DefaultSearchFormatTree;
import net.thevpc.nuts.runtime.standalone.format.tson.DefaultSearchFormatTson;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultSearchFormatXml;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Type;
import java.util.function.Predicate;

public class DefaultNElementFormat extends DefaultFormatBase<NElementFormat> implements NElementFormat {
    private final DefaultNTextManagerModel model;
    private Object value;
    private NContentType contentType = NContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private Predicate<Class<?>> indestructibleObjects;
    private UserElementMapperStore userElementMapperStore;


    public DefaultNElementFormat() {
        super("element-format");
        this.model = NWorkspaceExt.of().getModel().textModel;
        this.userElementMapperStore = new UserElementMapperStore();
    }

    public NElementMapperStore mappers() {
        return userElementMapperStore;
    }

    public boolean isLogProgress() {
        return logProgress;
    }

    public NElementFormat setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    public boolean isTraceProgress() {
        return traceProgress;
    }

    public NElementFormat setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public NElementFormat setContentType(NContentType contentType) {
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
    public NElementFormat json() {
        return setContentType(NContentType.JSON);
    }

    @Override
    public NElementFormat yaml() {
        return setContentType(NContentType.YAML);
    }

    @Override
    public NElementFormat tson() {
        return setContentType(NContentType.TSON);
    }

    @Override
    public NElementFormat xml() {
        return setContentType(NContentType.XML);
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public NElementFormat setValue(Object value) {
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
    public NElementFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public Predicate<Class<?>> getIndestructibleObjects() {
        return indestructibleObjects;
    }

    @Override
    public NElementFormat setIndestructibleFormat() {
        return setIndestructibleObjects(CoreNElementUtils.DEFAULT_INDESTRUCTIBLE_FORMAT);
    }

    public NElementFormat setIndestructibleObjects(Predicate<Class<?>> destructTypeFilter) {
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
        return resolveStructuredFormat().normalize(e == null ? NElement.ofNull() : e);
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
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(isNtf(), reflectRepository, userElementMapperStore,indestructibleObjects);
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
        NElement elem = NElements.of().setIndestructibleObjects(indestructibleObjects).toElement(value);
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
        return createFactoryContext().createObject(o, type);
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
    public NElementFormat setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }
}
