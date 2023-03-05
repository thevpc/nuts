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
    public NPrintStream ofNullPrintStream() {
        checkSession();
        return getBootModel().nullPrintStream();
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream() {
        return ofInMemoryPrintStream(null);
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode) {
        checkSession();
        return new NByteArrayPrintStream(mode,getSession());
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
        if (out instanceof NOutputStreamAdapter) {
            return ((NOutputStreamAdapter) out).getBaseOutputStream().setTerminalMode(expectedMode);
        }
        return
                new NPrintStreamRaw(out, null, null, session, new NPrintStreamBase.Bindings(), term)
                        .setTerminalMode(expectedMode)
                ;
    }

    @Override
    public NPrintStream ofPrintStream(OutputStream out) {
        checkSession();
        return new NPrintStreamRaw(out, null, null, session, new NPrintStreamBase.Bindings(), null);
    }

    public NPrintStream ofPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal) {
        checkSession();
        if (mode == null) {
            mode = NTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NOutputStreamAdapter) {
            return ((NOutputStreamAdapter) out).getBaseOutputStream().setTerminalMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal, session);
        return ofPrintStream(w, mode, terminal);
    }

    @Override
    public NPrintStream ofPrintStream(Writer out) {
        checkSession();
        return ofPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public boolean isStdout(NPrintStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.out()) {
            return true;
        }
        if (out instanceof NPrintStreamRendered) {
            return isStdout(((NPrintStreamRendered) out).getBase());
        }
        return out instanceof NPrintStreamSystem;
    }

    @Override
    public boolean isStderr(NPrintStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.err()) {
            return true;
        }
        if (out instanceof NPrintStreamRendered) {
            return isStderr(((NPrintStreamRendered) out).getBase());
        }
        return out instanceof NPrintStreamSystem;
    }

    @Override
    public NPrintStream stdout() {
        return getBootModel().getSystemTerminal().out();
    }

    @Override
    public NPrintStream stderr() {
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
    public NInputSource ofInputSource(InputStream inputStream) {
        return ofInputSource(inputStream, null);
    }

    @Override
    public NInputSource ofInputSource(InputStream inputStream, NInputSourceMetadata metadata) {
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
    public NInputSource ofMultiRead(NInputSource source) {
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
    public NInputSource ofInputSource(byte[] bytes) {
        return ofInputSource(new ByteArrayInputStream(bytes));
    }


    @Override
    public NInputSource ofInputSource(byte[] inputStream, NInputSourceMetadata metadata) {
        return ofInputSource(new ByteArrayInputStream(inputStream), metadata);
    }

    @Override
    public NOutputTarget ofOutputTarget(OutputStream output) {
        return ofOutputTarget(output, null);
    }

    @Override
    public NOutputTarget ofOutputTarget(OutputStream output, NOutputTargetMetadata metadata) {
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
