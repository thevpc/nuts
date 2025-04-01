package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;

import net.thevpc.nuts.NBootOptions;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.ask.DefaultNAsk;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFrom;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractNInputSource;
import net.thevpc.nuts.runtime.standalone.io.util.NInputStreamSource;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultNIORPI implements NIORPI {
    private final NWorkspace workspace;
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIORPI(NWorkspace workspace) {
        this.workspace = workspace;
        this.cmodel = NWorkspaceExt.of().getConfigModel();
        bootModel = NWorkspaceExt.of().getModel().bootModel;
    }


    @Override
    public <T> NAsk<T> createQuestion() {
        return createQuestion(workspace.currentSession().getTerminal());
    }

    @Override
    public <T> NAsk<T> createQuestion(NTerminal terminal) {
        return new DefaultNAsk<>(terminal, terminal.out());
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream() {
        return ofInMemoryPrintStream(null);
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode) {
        return new NByteArrayPrintStream(mode, workspace);
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out, NTerminalMode expectedMode, NSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        NBootOptions woptions = NWorkspace.of().getBootOptions();
        NTerminalMode expectedMode0 = woptions.getTerminalMode().orElse(NTerminalMode.DEFAULT);
        if (expectedMode0 == NTerminalMode.DEFAULT) {
            if (woptions.getBot().orElse(false)) {
                expectedMode0 = NTerminalMode.FILTERED;
            } else {
                expectedMode0 = NTerminalMode.FORMATTED;
            }
        }
        if (expectedMode == null) {
            expectedMode = expectedMode0;
        }
        if (expectedMode == NTerminalMode.FORMATTED) {
            if (expectedMode0 == NTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                expectedMode = NTerminalMode.FILTERED;
            }
        }
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).getBasePrintStream().setTerminalMode(expectedMode);
        }
        switch (expectedMode) {
            case DEFAULT:
            case ANSI:
            case INHERITED: {
                return new NPrintStreamRaw(out, expectedMode,
                        null, null, workspace,
                        new NPrintStreamBase.Bindings(), term
                );
            }
            case FILTERED:
            case FORMATTED: {
                return new NPrintStreamRaw(out, NTerminalMode.INHERITED,
                        null, null, workspace,
                        new NPrintStreamBase.Bindings(), term
                ).setTerminalMode(expectedMode);
            }
        }
        throw new NIllegalArgumentException(NMsg.ofC("unsupported mode %s", expectedMode));
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out) {
//        checkSession();
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).getBasePrintStream();
        }
        return new NPrintStreamRaw(out, null, null, workspace, new NPrintStreamBase.Bindings(), null);
    }

    @Override
    public NPrintStream ofPrintStream(Writer out, NTerminalMode mode) {
        return ofPrintStream(out, mode, null);
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode) {
        return ofPrintStream(out, mode, null);
    }

    public NPrintStream ofPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal) {
//        checkSession();
        if (mode == null) {
            mode = NTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).getBasePrintStream().setTerminalMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal, workspace);
        return ofPrintStream(w, mode, terminal);
    }

    @Override
    public NPrintStream ofPrintStream(Writer out) {
//        checkSession();
        return ofPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public NPrintStream ofPrintStream(NPath out) {
        return ofPrintStream(out.getOutputStream());
    }

    @Override
    public NPrintStream ofNullPrintStream() {
        return bootModel.nullPrintStream();
    }

    @Override
    public NInputSource ofInputSource(InputStream inputStream) {
        return ofInputSource(inputStream, null);
    }

    @Override
    public NInputSource ofInputSource(Reader inputStream, NContentMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        return ofInputSource(new ReaderInputStream(inputStream,null), metadata);
    }

    @Override
    public NInputSource ofInputSource(Reader inputStream) {
        return ofInputSource(inputStream, null);
    }

    @Override
    public NInputSource ofInputSource(NInputStreamProvider inputStream) {
        return ofInputSource(inputStream, null);
    }

    @Override
    public NInputSource ofInputSource(NInputStreamProvider inputStreamProvider, NContentMetadata metadata) {
        if (inputStreamProvider == null) {
            return null;
        }
        if (inputStreamProvider instanceof NInputSource) {
            if(metadata==null){
                return (NInputSource) inputStreamProvider;
            }
            NInputSource o = (NInputSource) inputStreamProvider;
            return new AbstractNInputSource() {
                @Override
                public boolean isMultiRead() {
                    return o.isMultiRead();
                }

                @Override
                public boolean isKnownContentLength() {
                    return o.isKnownContentLength();
                }

                @Override
                public long contentLength() {
                    return o.contentLength();
                }

                @Override
                public NContentMetadata getMetaData() {
                    return metadata;
                }

                @Override
                public InputStream getInputStream() {
                    return o.getInputStream();
                }
            };
        }
        if(metadata==null){
            DefaultNContentMetadata metadata2 = new DefaultNContentMetadata(NMsg.ofPlain("Provider"), null, null, null, null);
            return new inputStreamProviderToNInputSourceAdapter(metadata2, inputStreamProvider);
        }
        return new inputStreamProviderToNInputSourceAdapter(metadata, inputStreamProvider);
    }

    @Override
    public NInputSource ofInputSource(InputStream inputStream, NContentMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        if (metadata == null) {
            NText str = null;
            Long contentLength = null;
            try {
                contentLength = (long) inputStream.available();
            } catch (IOException e) {
                //just ignore error
                //throw new UncheckedIOException(e);
            }
            if (inputStream instanceof ByteArrayInputStream) {
                str = NText.ofStyled("<memory-buffer>", NTextStyle.path());
            } else {
                str = NText.ofStyled(inputStream.toString(), NTextStyle.path());
            }
            metadata = new DefaultNContentMetadata(NMsg.ofNtf(str), contentLength, null, null, null);
        }

        InputStream inputStreamExt = ofInputSourceBuilder(inputStream).setMetadata(metadata).createInputStream();
        return new NInputStreamSource(inputStreamExt, null, workspace);
    }


    @Override
    public NInputSource ofMultiRead(NInputSource source) {
        if (source.isMultiRead()) {
            return source;
        }
        NPath tf = NPath.ofTempFile();
        try (InputStream in = source.getInputStream()) {
            try (OutputStream out = tf.getOutputStream()) {
                NIOUtils.copy(in, out, 4096);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return tf;
    }

    @Override
    public NInputSource ofInputSource(byte[] bytes) {
        return ofInputSource(new ByteArrayInputStream(bytes));
    }


    @Override
    public NInputSource ofInputSource(byte[] inputStream, NContentMetadata metadata) {
        return ofInputSource(new ByteArrayInputStream(inputStream), metadata);
    }

    @Override
    public NOutputTarget ofOutputTarget(OutputStream outputStream) {
        return ofOutputTarget(outputStream, null);
    }

    @Override
    public NOutputTarget ofOutputTarget(OutputStream outputStream, NContentMetadata metadata) {
        return new OutputTargetExt(NOutputStreamBuilder.of(outputStream)
                .setMetadata(metadata).createOutputStream(), null, workspace);
    }

    @Override
    public NOutputTarget ofOutputTarget(Writer writer, NContentMetadata metadata) {
        if(writer==null){
            return null;
        }
        if(writer instanceof NOutputTarget){
            return (NOutputTarget) writer;
        }
        return ofOutputTarget(new WriterOutputStream(writer), metadata);
    }

    @Override
    public NOutputTarget ofOutputTarget(Writer writer) {
        return ofOutputTarget(writer, null);
    }

    @Override
    public NOutputStreamBuilder ofOutputStreamBuilder(OutputStream base) {
        return new DefaultNOutputStreamBuilder(workspace).setBase(base);
    }

    public NNonBlockingInputStream ofNonBlockingInputStream(InputStream base) {
        return ofInputSourceBuilder(base).createNonBlockingInputStream();
    }

    public NInterruptible<InputStream> ofInterruptible(InputStream base) {
        return ofInputSourceBuilder(base).createInterruptibleInputStream();
    }

    public NInputSourceBuilder ofInputSourceBuilder(InputStream inputStream) {
        return new DefaultNInputSourceBuilder(workspace).setBase(inputStream);
    }

    @Override
    public NTerminal createTerminal() {
        return cmodel.createTerminal();
    }

    @Override
    public NTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
        return cmodel.createTerminal(in, out, err);
    }

    @Override
    public NTerminal createTerminal(NTerminal terminal) {
        if (terminal == null) {
            return createTerminal();
        }
        if (terminal instanceof DefaultNTerminalFromSystem) {
            DefaultNTerminalFromSystem t = (DefaultNTerminalFromSystem) terminal;
            return new DefaultNTerminalFromSystem(t);
        }
        if (terminal instanceof DefaultNSessionTerminalFrom) {
            DefaultNSessionTerminalFrom t = (DefaultNSessionTerminalFrom) terminal;
            return new DefaultNSessionTerminalFrom(t);
        }
        return new DefaultNSessionTerminalFrom(terminal);
    }

    @Override
    public NTerminal createInMemoryTerminal() {
        return createInMemoryTerminal(false);
    }

    @Override
    public NTerminal createInMemoryTerminal(boolean mergeErr) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NMemoryPrintStream out = NMemoryPrintStream.of();
        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of();
        return createTerminal(in, out, err);
    }

    @Override
    public void enableRichTerm() {
        bootModel.enableRichTerm();
    }


    @Override
    public List<NExecutionEntry> parseExecutionEntries(NPath file) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "jar", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else if (file.getName().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "class", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName) {
        if ("jar".equals(type)) {
            return JavaJarUtils.parseJarExecutionEntries(inputStream);
        } else if ("class".equals(type)) {
            NExecutionEntry u = JavaClassUtils.parseClassExecutionEntry(inputStream, sourceName);
            return u == null ? Collections.emptyList() : Arrays.asList(u);
        }
        return Collections.emptyList();
    }

    private class inputStreamProviderToNInputSourceAdapter extends AbstractNInputSource {
        private final NContentMetadata metadata2;
        private final NInputStreamProvider inputStreamProvider;

        public inputStreamProviderToNInputSourceAdapter(NContentMetadata metadata2, NInputStreamProvider inputStreamProvider) {
            super();
            this.metadata2 = metadata2;
            this.inputStreamProvider = inputStreamProvider;
        }

        @Override
        public boolean isMultiRead() {
            return false;
        }

        @Override
        public boolean isKnownContentLength() {
            return false;
        }

        @Override
        public long contentLength() {
            return -1;
        }

        @Override
        public NContentMetadata getMetaData() {
            return metadata2;
        }

        @Override
        public InputStream getInputStream() {
            return inputStreamProvider.getInputStream();
        }
    }
}
