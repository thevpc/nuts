package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsWorkspaceOptions;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.util.NullInputStream;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;

import java.io.*;

public class DefaultNutsIO implements NutsIO {
    private final NutsSession session;

    public DefaultNutsIO(NutsSession session) {
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
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    private DefaultNutsBootModel getBootModel() {
        return NutsWorkspaceExt.of(session).getModel().bootModel;
    }


    @Override
    public NutsPrintStream createNullPrintStream() {
        checkSession();
        return getBootModel().nullPrintStream();
    }

    @Override
    public NutsMemoryPrintStream createInMemoryPrintStream() {
        checkSession();
        return new NutsByteArrayPrintStream(getSession());
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out, NutsTerminalMode expectedMode, NutsSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        NutsWorkspaceOptions woptions = session.boot().setSession(session).getBootOptions();
        NutsTerminalMode expectedMode0 = woptions.getTerminalMode().orElse(NutsTerminalMode.DEFAULT);
        if (expectedMode0 == NutsTerminalMode.DEFAULT) {
            if (woptions.getBot().orElse(false)) {
                expectedMode0 = NutsTerminalMode.FILTERED;
            } else {
                expectedMode0 = NutsTerminalMode.FORMATTED;
            }
        }
        if (expectedMode == null) {
            expectedMode = expectedMode0;
        }
        if (expectedMode == NutsTerminalMode.FORMATTED) {
            if (expectedMode0 == NutsTerminalMode.FILTERED) {
                //if nuts started with --no-color modifier, will disable FORMATTED terminal mode each time
                expectedMode = NutsTerminalMode.FILTERED;
            }
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBasePrintStream().setTerminalMode(expectedMode);
        }
        return
                new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings(), term)
                        .setTerminalMode(expectedMode)
                ;
    }

    @Override
    public NutsPrintStream createPrintStream(OutputStream out) {
        checkSession();
        return new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings(), null);
    }

    public NutsPrintStream createPrintStream(Writer out, NutsTerminalMode mode, NutsSystemTerminalBase terminal) {
        checkSession();
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBasePrintStream().setTerminalMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal, session);
        return createPrintStream(w, mode, terminal);
    }

    @Override
    public NutsPrintStream createPrintStream(Writer out) {
        checkSession();
        return createPrintStream(out, NutsTerminalMode.INHERITED, null);
    }

    @Override
    public boolean isStdout(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        NutsSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.out()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStdout(((NutsPrintStreamRendered) out).getBase());
        }
        return out instanceof NutsPrintStreamSystem;
    }

    @Override
    public boolean isStderr(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        NutsSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.err()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStderr(((NutsPrintStreamRendered) out).getBase());
        }
        return out instanceof NutsPrintStreamSystem;
    }

    @Override
    public NutsPrintStream stdout() {
        return getBootModel().getSystemTerminal().out();
    }

    @Override
    public NutsPrintStream stderr() {
        return getBootModel().getSystemTerminal().err();
    }

    public NutsSession getSession() {
        return session;
    }

    private void checkSession() {
        //NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    private DefaultNutsWorkspaceConfigModel getConfigModel() {
        return ((DefaultNutsWorkspaceConfigManager) session.config()).getModel();
    }

    @Override
    public NutsInputSource createInputSource(InputStream inputStream) {
        return createInputSource(inputStream, null);
    }

    @Override
    public NutsInputSource createInputSource(InputStream inputStream, NutsInputSourceMetadata metadata) {
        if (inputStream == null) {
            return null;
        }
        if (inputStream instanceof NutsInputSource) {
            return (NutsInputSource) inputStream;
        }
        if (metadata == null) {
            NutsString str = null;
            int contentLength = -1;
            try {
                contentLength = inputStream.available();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            if (inputStream instanceof ByteArrayInputStream) {
                str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
            } else {
                str = NutsTexts.of(session).ofStyled(inputStream.toString(), NutsTextStyle.path());
            }
            metadata = new DefaultNutsInputSourceMetadata(NutsMessage.ofNtf(str), contentLength, null, null);
        }
        return new InputStreamExt(inputStream, metadata, null);
    }

    @Override
    public NutsInputSource createMultiRead(NutsInputSource source) {
        if (source.isMultiRead()) {
            return source;
        }
        NutsPath tf = NutsPaths.of(session).createTempFile(session);
        try (InputStream in = source.getInputStream()) {
            try (OutputStream out = tf.getOutputStream()) {
                CoreIOUtils.copy(in, out, 4096, session);
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
        return tf;
    }

    @Override
    public NutsInputSource createInputSource(byte[] bytes) {
        return createInputSource(new ByteArrayInputStream(bytes));
    }


    @Override
    public NutsInputSource createInputSource(byte[] inputStream, NutsInputSourceMetadata metadata) {
        return createInputSource(new ByteArrayInputStream(inputStream), metadata);
    }

    @Override
    public NutsOutputTarget createOutputTarget(OutputStream output) {
        return createOutputTarget(output, null);
    }

    @Override
    public NutsOutputTarget createOutputTarget(OutputStream output, NutsOutputTargetMetadata metadata) {
        if (output == null) {
            return null;
        }
        if (output instanceof NutsOutputTarget) {
            return (NutsOutputTarget) output;
        }
        if (metadata == null) {
            NutsString str = null;
            if (output instanceof ByteArrayOutputStream) {
                str = NutsTexts.of(session).ofStyled("<memory-buffer>", NutsTextStyle.path());
            } else {
                str = NutsTexts.of(session).ofStyled(output.toString(), NutsTextStyle.path());
            }
            metadata = new DefaultNutsOutputTargetMetadata(NutsMessage.ofNtf(str), str.filteredText());
        }
        return new OutputStreamExt(output, metadata);
    }
}
