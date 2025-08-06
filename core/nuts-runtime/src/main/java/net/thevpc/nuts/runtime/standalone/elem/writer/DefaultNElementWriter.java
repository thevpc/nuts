package net.thevpc.nuts.runtime.standalone.elem.writer;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.runtime.standalone.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.elem.NElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore.UserElementMapperStore;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTextManagerModel;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.time.NProgressFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.function.Consumer;

public class DefaultNElementWriter implements NElementWriter {

    private final DefaultNTextManagerModel model;
    private NContentType contentType = NContentType.JSON;
    private boolean compact;
    private boolean logProgress;
    private boolean traceProgress;
    private NProgressFactory progressFactory;
    private UserElementMapperStore userElementMapperStore;
    private boolean ntf;


    public DefaultNElementWriter() {
        this.model = NWorkspaceExt.of().getModel().textModel;
        this.userElementMapperStore = new UserElementMapperStore();
        this.userElementMapperStore.setReflectRepository(NReflectRepository.of());
    }

    @Override
    public NElementMapperStore mapperStore() {
        return userElementMapperStore;
    }

    @Override
    public NElementWriter doWithMapperStore(Consumer<NElementMapperStore> doWith) {
        if (doWith != null) {
            doWith.accept(mapperStore());
        }
        return this;
    }

    public boolean isNtf() {
        return ntf;
    }

    public NElementWriter setNtf(boolean nft) {
        this.ntf = nft;
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
    public NElementWriter table() {
        return setContentType(NContentType.TABLE);
    }

    @Override
    public NElementWriter tree() {
        return setContentType(NContentType.TREE);
    }

    @Override
    public NElementWriter props() {
        return setContentType(NContentType.PROPS);
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

    private NElementStreamFormat resolveStructuredFormat() {
        return model.getStreamFormat(
                (contentType == null || contentType == NContentType.PLAIN) ? NContentType.TSON : contentType
        );
    }

    private DefaultNElementFactoryContext createFactoryContext() {
        NReflectRepository reflectRepository = NWorkspaceUtils.of().getReflectRepository();
        DefaultNElementFactoryContext c = new DefaultNElementFactoryContext(ntf, reflectRepository, userElementMapperStore);
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

    private void write(Object value, NPrintStream out, NElementStreamFormat format) {
        NElement elem = NElements.of().doWithMapperStore(d->d.copyFrom(userElementMapperStore)).toElement(value);
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
    public void write(Object value, NPrintStream out) {
        write(value, out, resolveStructuredFormat());
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
    public NElementWriter setProgressFactory(NProgressFactory progressFactory) {
        this.progressFactory = progressFactory;
        return this;
    }

    /// ////////


    @Override
    public void write(Object any) {
        NSession session = NSession.of();
        write(any, session.getTerminal());
    }

    @Override
    public void writeln(Object any) {
        NSession session = NSession.of();
        writeln(any,session.getTerminal());
    }

    @Override
    public void write(Object any, Writer out) {
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            write(pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            write(any, pout);
            pout.flush();
        }
    }

    @Override
    public void write(Object any, OutputStream out) {
        NPrintStream p =
                out == null ? getValidPrintStream() :
                        NPrintStream.of(out);
        write(any, p);
        p.flush();
    }

    @Override
    public void write(Object any, Path path) {
        write(any,NPath.of(path));
    }

    @Override
    public void write(Object any, NPath path) {
        path.mkParentDirs();
        try (Writer w = path.getWriter()) {
            write(any, w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void write(Object any, File file) {
        write(NPath.of(file));
    }

    @Override
    public void write(Object any, NTerminal terminal) {
        NSession session = NSession.of();
        write(any, terminal == null ? session.getTerminal().out() : terminal.out());
    }

    @Override
    public void writeln(Object any, Writer w) {
        if (w == null) {
            NPrintStream pout = getValidPrintStream();
            writeln(any, pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(w);
            writeln(any, pout);
            pout.flush();
        }
    }

    @Override
    public void writeln(Object any, NPrintStream out) {
        NPrintStream p = getValidPrintStream(out);
        write(any, out);
        p.println();
        p.flush();
    }

    @Override
    public void writeln(Object any, OutputStream out) {
        if (out == null) {
            NPrintStream pout = getValidPrintStream();
            writeln(any, pout);
            pout.flush();
        } else {
            NPrintStream pout = NPrintStream.of(out);
            writeln(any, pout);
            pout.flush();
        }
    }

    @Override
    public void writeln(Object any, Path path) {
        writeln(any, NPath.of(path));
    }

    @Override
    public void writeln(Object any, NPath out) {
        out.mkParentDirs();
        try (Writer w = out.getWriter()) {
            writeln(any, w);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void writeln(Object any, NTerminal terminal) {
        NSession session = NSession.of();
        writeln(any, terminal == null ? session.getTerminal().out() : terminal.out());
    }

    @Override
    public void writeln(Object any, File file) {
        writeln(any, file.toPath());
    }


    @Override
    public String toString(Object object) {
        StringWriter sw = new StringWriter();
        write(object, sw);
        return sw.toString();
    }

    @Override
    public NText toText(Object object) {
        if (isNtf()) {
            return NText.of(toString(object));
        }
        return NText.ofPlain(toString(object));
    }

    public NPrintStream getValidPrintStream(NPrintStream out) {
        if (out == null) {
            out = NOut.out();
        }
        return out;
    }

    public NPrintStream getValidPrintStream() {
        return NOut.out();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public NElementWriter configure(boolean skipUnsupported, String... args) {
        return NCmdLineConfigurable.configure(this, skipUnsupported, args, "writer");
    }
}
