package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.ask.DefaultNAsk;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSession;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.NInputStreamSource;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.reserved.rpi.NIORPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NAsk;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DefaultNIORPI implements NIORPI {
    private final NSession session;
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIORPI(NSession session) {
        this.session = session;
        this.cmodel = ((DefaultNConfigs) NConfigs.of(session)).getModel();
        bootModel = NWorkspaceExt.of(session.getWorkspace()).getModel().bootModel;
    }


    @Override
    public <T> NAsk<T> createQuestion(NSession session) {
        return createQuestion(session.getTerminal());
    }

    @Override
    public <T> NAsk<T> createQuestion(NSessionTerminal terminal) {
        return new DefaultNAsk<>(session.getWorkspace(),terminal,terminal.out());
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream() {
        return ofInMemoryPrintStream(null);
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode) {
        return new NByteArrayPrintStream(mode, session);
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out, NTerminalMode expectedMode, NSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        NWorkspaceOptions woptions = NBootManager.of(session).getBootOptions();
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
        return
                new NPrintStreamRaw(out,expectedMode, null, null, session, new NPrintStreamBase.Bindings(), term)
//                        .setTerminalMode(expectedMode)
                ;
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out) {
//        checkSession();
        if (out instanceof NPrintStreamAdapter) {
            return ((NPrintStreamAdapter) out).getBasePrintStream();
        }
        return new NPrintStreamRaw(out, null, null, session, new NPrintStreamBase.Bindings(), null);
    }

    @Override
    public NPrintStream ofPrintStream(Writer out, NTerminalMode mode) {
        return ofPrintStream(out,mode,null);
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out, NTerminalMode mode) {
        return ofPrintStream(out,mode,null);
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
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal, session);
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
    public NInputSource ofInputSource(InputStream inputStream, NContentMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        if (metadata == null) {
            NString str = null;
            Long contentLength = null;
            try {
                contentLength = (long)inputStream.available();
            } catch (IOException e) {
                //just ignore error
                //throw new UncheckedIOException(e);
            }
            if (inputStream instanceof ByteArrayInputStream) {
                str = NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path());
            } else {
                str = NTexts.of(session).ofStyled(inputStream.toString(), NTextStyle.path());
            }
            metadata = new DefaultNContentMetadata(NMsg.ofNtf(str), contentLength, null, null, null);
        }

        InputStream inputStreamExt = ofInputSourceBuilder(inputStream).setMetadata(metadata).createInputStream();
        return new NInputStreamSource(inputStreamExt, null, session);
    }


    @Override
    public NInputSource ofMultiRead(NInputSource source) {
        if (source.isMultiRead()) {
            return source;
        }
        NPath tf = NPath.ofTempFile(session);
        try (InputStream in = source.getInputStream()) {
            try (OutputStream out = tf.getOutputStream()) {
                CoreIOUtils.copy(in, out, 4096, session);
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
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
        return new OutputTargetExt(NOutputStreamBuilder.of(outputStream,session)
                .setMetadata(metadata).createOutputStream(), null, session);
    }

    @Override
    public NOutputStreamBuilder ofOutputStreamBuilder(OutputStream base) {
        return new DefaultNOutputStreamBuilder(session).setBase(base);
    }

    public NNonBlockingInputStream ofNonBlockingInputStream(InputStream base) {
        return ofInputSourceBuilder(base).createNonBlockingInputStream();
    }

    public NInterruptible<InputStream> ofInterruptible(InputStream base) {
        return ofInputSourceBuilder(base).createInterruptibleInputStream();
    }

    public NInputSourceBuilder ofInputSourceBuilder(InputStream inputStream) {
        return new DefaultNInputSourceBuilder(session).setBase(inputStream);
    }

    @Override
    public NSessionTerminal createTerminal() {
        return cmodel.createTerminal(session);
    }

    @Override
    public NSessionTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
        return cmodel.createTerminal(in, out, err, session);
    }

    @Override
    public NSessionTerminal createTerminal(NSessionTerminal terminal) {
        if (terminal == null) {
            return createTerminal();
        }
        if (terminal instanceof DefaultNSessionTerminalFromSystem) {
            DefaultNSessionTerminalFromSystem t = (DefaultNSessionTerminalFromSystem) terminal;
            return new DefaultNSessionTerminalFromSystem(session, t);
        }
        if (terminal instanceof DefaultNSessionTerminalFromSession) {
            DefaultNSessionTerminalFromSession t = (DefaultNSessionTerminalFromSession) terminal;
            return new DefaultNSessionTerminalFromSession(session, t);
        }
        return new DefaultNSessionTerminalFromSession(session, terminal);
    }

    @Override
    public NSessionTerminal createInMemoryTerminal() {
        return createInMemoryTerminal(false);
    }

    @Override
    public NSessionTerminal createInMemoryTerminal(boolean mergeErr) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NMemoryPrintStream out = NMemoryPrintStream.of(session);
        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of(session);
        return createTerminal(in, out, err);
    }

    @Override
    public void enableRichTerm() {
        bootModel.enableRichTerm(session);
    }


    @Override
    public List<NExecutionEntry> parseExecutionEntries(NPath file) {
        if (file.getName().toLowerCase().endsWith(".jar")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "jar", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
        } else if (file.getName().toLowerCase().endsWith(".class")) {
            try {
                try (InputStream in = file.getInputStream()) {
                    return parseExecutionEntries(in, "class", file.toAbsolute().normalize().toString());
                }
            } catch (IOException ex) {
                throw new NIOException(session, ex);
            }
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<NExecutionEntry> parseExecutionEntries(InputStream inputStream, String type, String sourceName) {
        if ("jar".equals(type)) {
            return JavaJarUtils.parseJarExecutionEntries(inputStream, session);
        } else if ("class".equals(type)) {
            NExecutionEntry u = JavaClassUtils.parseClassExecutionEntry(inputStream, sourceName, session);
            return u == null ? Collections.emptyList() : Arrays.asList(u);
        }
        return Collections.emptyList();
    }
}
