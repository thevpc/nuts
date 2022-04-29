package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.NutsBootModel;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.OutputStream;
import java.io.Writer;

public class DefaultNutsPrintStreams implements NutsPrintStreams {
    private final NutsSession session;

    public DefaultNutsPrintStreams(NutsSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsPrintStream createNull() {
        checkSession();
        return getBootModel().nullPrintStream();
    }

    @Override
    public NutsMemoryPrintStream createInMemory() {
        checkSession();
        return new NutsByteArrayPrintStream(getSession());
    }

    @Override
    public NutsPrintStream create(OutputStream out, NutsTerminalMode expectedMode, NutsSystemTerminalBase term) {
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
            return ((NutsPrintStreamAdapter) out).getBasePrintStream().setMode(expectedMode);
        }
        return
                new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings(), term)
                        .setMode(expectedMode)
                ;
    }

    @Override
    public NutsPrintStream create(OutputStream out) {
        checkSession();
        return new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings(), null);
    }

    public NutsPrintStream create(Writer out, NutsTerminalMode mode, NutsSystemTerminalBase terminal) {
        checkSession();
        if (mode == null) {
            mode = NutsTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBasePrintStream().setMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, terminal, session);
        return create(w, mode, terminal);
    }

    @Override
    public NutsPrintStream create(Writer out) {
        checkSession();
        return create(out, NutsTerminalMode.INHERITED, null);
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

    private NutsBootModel getBootModel() {
        return ((DefaultNutsBootManager) session.boot()).getModel();
    }

}
