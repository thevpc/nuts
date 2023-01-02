package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.util.NullInputStream;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NPaths;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;

import java.io.*;

public class DefaultNIO implements NIO {
    private final NSession session;

    public DefaultNIO(NSession session) {
        this.session = session;
    }

    @Override
    public InputStream ofNullInputStream() {
        return NullInputStream.INSTANCE;
    }

    @Override
    public boolean isStdin(InputStream in) {
        return in == stdin();
    }

    @Override
    public InputStream stdin() {
        return getBootModel().getSystemTerminal().in();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    private DefaultNBootModel getBootModel() {
        return NWorkspaceExt.of(session).getModel().bootModel;
    }


    @Override
    public NOutStream createNullPrintStream() {
        checkSession();
        return getBootModel().nullPrintStream();
    }

    @Override
    public NOutMemoryStream createInMemoryPrintStream() {
        checkSession();
        return new NOutByteArrayStream(getSession());
    }

    @Override
    public NOutStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NSystemTerminalBase term) {
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
                new NOutStreamRaw(out, null, null, session, new NOutStreamBase.Bindings(), term)
                        .setTerminalMode(expectedMode)
                ;
    }

    @Override
    public NOutStream createPrintStream(OutputStream out) {
        checkSession();
        return new NOutStreamRaw(out, null, null, session, new NOutStreamBase.Bindings(), null);
    }

    public NOutStream createPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal) {
        checkSession();
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
        return createPrintStream(w, mode, terminal);
    }

    @Override
    public NOutStream createPrintStream(Writer out) {
        checkSession();
        return createPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public boolean isStdout(NOutStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.out()) {
            return true;
        }
        if (out instanceof NOutStreamRendered) {
            return isStdout(((NOutStreamRendered) out).getBase());
        }
        return out instanceof NOutStreamSystem;
    }

    @Override
    public boolean isStderr(NOutStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.err()) {
            return true;
        }
        if (out instanceof NOutStreamRendered) {
            return isStderr(((NOutStreamRendered) out).getBase());
        }
        return out instanceof NOutStreamSystem;
    }

    @Override
    public NOutStream stdout() {
        return getBootModel().getSystemTerminal().out();
    }

    @Override
    public NOutStream stderr() {
        return getBootModel().getSystemTerminal().err();
    }

    public NSession getSession() {
        return session;
    }

    private void checkSession() {
        //NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    private DefaultNWorkspaceConfigModel getConfigModel() {
        return ((DefaultNConfigs) NConfigs.of(session)).getModel();
    }

    @Override
    public NInputSource createInputSource(InputStream inputStream) {
        return createInputSource(inputStream, null);
    }

    @Override
    public NInputSource createInputSource(InputStream inputStream, NInputSourceMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NInputSource) {
            return (NInputSource) inputStream;
        }
        if (metadata == null) {
            NString str = null;
            int contentLength = -1;
            try {
                contentLength = inputStream.available();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (inputStream instanceof ByteArrayInputStream) {
                str = NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path());
            } else {
                str = NTexts.of(session).ofStyled(inputStream.toString(), NTextStyle.path());
            }
            metadata = new DefaultNInputSourceMetadata(NMsg.ofNtf(str), contentLength, null, null);
        }
        return new InputStreamExt(inputStream, metadata, null);
    }

    @Override
    public NInputSource createMultiRead(NInputSource source) {
        if (source.isMultiRead()) {
            return source;
        }
        NPath tf = NPaths.of(session).createTempFile();
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
    public NInputSource createInputSource(byte[] bytes) {
        return createInputSource(new ByteArrayInputStream(bytes));
    }


    @Override
    public NInputSource createInputSource(byte[] inputStream, NInputSourceMetadata metadata) {
        return createInputSource(new ByteArrayInputStream(inputStream), metadata);
    }

    @Override
    public NOutputTarget createOutputTarget(OutputStream output) {
        return createOutputTarget(output, null);
    }

    @Override
    public NOutputTarget createOutputTarget(OutputStream output, NOutputTargetMetadata metadata) {
        if (output == null) {
            return null;
        }
        if (output instanceof NOutputTarget) {
            return (NOutputTarget) output;
        }
        if (metadata == null) {
            NString str = null;
            if (output instanceof ByteArrayOutputStream) {
                str = NTexts.of(session).ofStyled("<memory-buffer>", NTextStyle.path());
            } else {
                str = NTexts.of(session).ofStyled(output.toString(), NTextStyle.path());
            }
            metadata = new DefaultNOutputTargetMetadata(NMsg.ofNtf(str), str.filteredText());
        }
        return new OutputStreamExt(output, metadata);
    }
}
