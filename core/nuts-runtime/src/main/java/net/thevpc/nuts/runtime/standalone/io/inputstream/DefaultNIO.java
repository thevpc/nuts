package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSession;
import net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;

import java.io.*;

public class DefaultNIO implements NIO {
    private final NSession session;
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIO(NSession session) {
        this.session = session;
        this.cmodel = ((DefaultNConfigs) NConfigs.of(session)).getModel();
        bootModel = NWorkspaceExt.of(session.getWorkspace()).getModel().bootModel;
    }

    @Override
    public InputStream ofNullRawInputStream() {
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
        return NConstants.Support.DEFAULT_SUPPORT;
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
    public OutputStream ofNullRawOutputStream() {
        checkSession();
        return getBootModel().nullOutputStream();
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream() {
        return ofInMemoryPrintStream(null);
    }

    @Override
    public NMemoryPrintStream ofInMemoryPrintStream(NTerminalMode mode) {
        checkSession();
        return new NByteArrayPrintStream(mode, getSession());
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
        checkSession();
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
        return ofPrintStream(w, mode, terminal);
    }

    @Override
    public NPrintStream ofPrintStream(Writer out) {
        checkSession();
        return ofPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public NPrintStream ofPrintStream(NPath out) {
        return ofPrintStream(out.getOutputStream());
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
    public NInputStreamBuilder ofInputStreamBuilder(InputStream inputStream) {
        return new DefaultNInputStreamBuilder(session).setBase(inputStream);
    }

    @Override
    public NOutputStreamBuilder ofOutputStreamBuilder(OutputStream base) {
        return new DefaultNOutputStreamBuilder(session).setBase(base);
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

        InputStream inputStreamExt = ofInputStreamBuilder(inputStream).setMetadata(metadata).createInputStream();
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
        return new OutputTargetExt(ofOutputStreamBuilder(outputStream).setMetadata(metadata).createOutputStream(), null, session);
    }

    @Override
    public NIO enableRichTerm() {
        bootModel.enableRichTerm(session);
        return this;
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
    public NSystemTerminal getSystemTerminal() {
        return NWorkspaceExt.of(session).getModel().bootModel.getSystemTerminal();
    }

    @Override
    public NIO setSystemTerminal(NSystemTerminalBase terminal) {
        NWorkspaceExt.of(session).getModel().bootModel.setSystemTerminal(terminal, session);
        return this;
    }

    @Override
    public NSessionTerminal getDefaultTerminal() {
        return cmodel.getTerminal();
    }

    @Override
    public NIO setDefaultTerminal(NSessionTerminal terminal) {
        cmodel.setTerminal(terminal, session);
        return this;
    }
}
