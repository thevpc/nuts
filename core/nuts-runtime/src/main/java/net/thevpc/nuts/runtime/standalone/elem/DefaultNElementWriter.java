package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NUnsupportedOperationException;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.text.NContentType;
import net.thevpc.nuts.text.NIterableFormat;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
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
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressFactory;
import net.thevpc.nuts.text.NMsg;

import java.lang.reflect.Type;
import java.util.function.Consumer;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNElementWriter extends DefaultObjectWriterBase<NElementWriter> implements NElementWriter {
    private final DefaultNTextManagerModel model;
    private NContentType contentType = NContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private UserElementMapperStore userElementMapperStore;


    public DefaultNElementWriter() {
        super("element-format");
        this.model = NWorkspaceExt.of().getModel().textModel;
        this.userElementMapperStore = new UserElementMapperStore();
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }

    public NElementMapperStore mapperStore() {
        return userElementMapperStore;
    }

    @Override
    public NElementWriter doWithMapperStore(Consumer<NElementMapperStore> doWith) {
        if(doWith != null) {
            doWith.accept(mapperStore());
        }
        return this;
    }

    public boolean isLogProgress() {
        return logProgress;
    }

    public NElementWriter setLogProgress(boolean logProgress) {
        this.logProgress = logProgress;
        return this;
    }

    public boolean isTraceProgress() {
        return traceProgress;
    }

    public NElementWriter setTraceProgress(boolean traceProgress) {
        this.traceProgress = traceProgress;
        return this;
    }

    @Override
    public NContentType getContentType() {
        return contentType;
    }

    @Override
    public NElementWriter setContentType(NContentType contentType) {
        if (contentType == null) {
            this.contentType = NContentType.JSON;
        } else {
            this.contentType = contentType;
        }
        return this;
    }

    @Override
    public NElementWriter json() {
        return setContentType(NContentType.JSON);
    }

    @Override
    public NElementWriter yaml() {
        return setContentType(NContentType.YAML);
    }

    @Override
    public NElementWriter tson() {
        return setContentType(NContentType.TSON);
    }

    @Override
    public NElementWriter xml() {
        return setContentType(NContentType.XML);
    }


    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NElementWriter setCompact(boolean compact) {
        this.compact = compact;
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


    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(isNtf(), reflectRepository, userElementMapperStore);
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

    private void print(Object aValue, NPrintStream out, NElementStreamFormat format) {
        NElement elem = NElements.of().doWithMapperStore(d->d.copyFrom(mapperStore())).toElement(aValue);
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
    public void print(Object aValue, NPrintStream out) {
        if (contentType == NContentType.PLAIN) {
            print(aValue, out, model.getJsonMan());
        } else {
            print(aValue, out, model.getStreamFormat(contentType==null?NContentType.JSON : contentType));
        }
    }

    public Object elementToObject(NElement o, Type type) {
        return createFactoryContext().createObject(o, type);
    }

    public NElementFactoryService getElementFactoryService() {
        return model.getElementFactoryService();
    }

    @Override
    public NProgressFactory getProgressFactory() {
        return progressFactory;
    }

    @Override
    public NElementWriter setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }
}
