package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootManager;
import net.thevpc.nuts.runtime.standalone.boot.NBootModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.OutputStream;
import java.io.Writer;

public class DefaultNPrintStreams implements NPrintStreams {
    private final NSession session;

    public DefaultNPrintStreams(NSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NStream createNullPrintStream() {
        checkSession();
        return getBootModel().nullPrintStream();
    }

    @Override
    public NMemoryStream createInMemoryPrintStream() {
        checkSession();
        return new NByteArrayStream(getSession());
    }

    @Override
    public NStream createPrintStream(OutputStream out, NTerminalMode expectedMode, NSystemTerminalBase term) {
        if (out == null) {
            return null;
        }
        NWorkspaceOptions woptions = session.boot().setSession(session).getBootOptions();
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
                new NStreamRaw(out, null, null, session, new NStreamBase.Bindings(), term)
                        .setTerminalMode(expectedMode)
                ;
    }

    @Override
    public NStream createPrintStream(OutputStream out) {
        checkSession();
        return new NStreamRaw(out, null, null, session, new NStreamBase.Bindings(), null);
    }

    public NStream createPrintStream(Writer out, NTerminalMode mode, NSystemTerminalBase terminal) {
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
    public NStream createPrintStream(Writer out) {
        checkSession();
        return createPrintStream(out, NTerminalMode.INHERITED, null);
    }

    @Override
    public boolean isStdout(NStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.out()) {
            return true;
        }
        if (out instanceof NStreamRendered) {
            return isStdout(((NStreamRendered) out).getBase());
        }
        return out instanceof NStreamSystem;
    }

    @Override
    public boolean isStderr(NStream out) {
        if (out == null) {
            return false;
        }
        NSystemTerminal st = getBootModel().getSystemTerminal();
        if (out == st.err()) {
            return true;
        }
        if (out instanceof NStreamRendered) {
            return isStderr(((NStreamRendered) out).getBase());
        }
        return out instanceof NStreamSystem;
    }

    @Override
    public NStream stdout() {
        return getBootModel().getSystemTerminal().out();
    }

    @Override
    public NStream stderr() {
        return getBootModel().getSystemTerminal().err();
    }

    public NSession getSession() {
        return session;
    }

    private void checkSession() {
        //NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }

    private DefaultNWorkspaceConfigModel getConfigModel() {
        return ((DefaultNWorkspaceConfigManager) session.config()).getModel();
    }

    private NBootModel getBootModel() {
        return ((DefaultNBootManager) session.boot()).getModel();
    }

}
