package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.SimpleWriterOutputStream;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNutsBootManager;
import net.thevpc.nuts.runtime.standalone.boot.NutsBootModel;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.OutputStream;
import java.io.Writer;

public class DefaultNutsPrintStreams implements NutsPrintStreams {
    private final NutsSession session;

    public DefaultNutsPrintStreams(NutsSession session) {
        this.session = session;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<Object> context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NutsPrintStream createNull() {
        checkSession();
        return getConfigModel().nullPrintStream();
    }


    @Override
    public NutsPrintStream create(OutputStream out) {
        checkSession();
        return new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings());
    }

    @Override
    public NutsPrintStream create(Writer out) {
        checkSession();
        return create(out, NutsTerminalMode.INHERITED);
    }

    public NutsPrintStream create(Writer out, NutsTerminalMode mode) {
        checkSession();
        if(mode ==null){
            mode =NutsTerminalMode.INHERITED;
        }
        if (out == null) {
            return null;
        }
        if (out instanceof NutsPrintStreamAdapter) {
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().setMode(mode);
        }
        SimpleWriterOutputStream w = new SimpleWriterOutputStream(out, session);
        return create(w, mode);
    }

    @Override
    public NutsPrintStream create(OutputStream out, NutsTerminalMode expectedMode) {
        if (out == null) {
            return null;
        }
        NutsWorkspaceOptions woptions = session.boot().setSession(session).getBootOptions();
        NutsTerminalMode expectedMode0 = woptions.getTerminalMode();
        if (expectedMode0 == null) {
            if (woptions.isBot()) {
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
            return ((NutsPrintStreamAdapter) out).getBaseNutsPrintStream().setMode(expectedMode);
        }
        return
                new NutsPrintStreamRaw(out, null, null, session, new NutsPrintStreamBase.Bindings())
                        .setMode(expectedMode)
                ;
    }

    @Override
    public NutsMemoryPrintStream createInMemory() {
        checkSession();
        return new NutsByteArrayPrintStream(getSession());
    }

    public NutsSession getSession() {
        return session;
    }

    private void checkSession() {
        //NutsWorkspaceUtils.checkSession(model.getWorkspace(), getSession());
    }
//    private DefaultNutsIOModel getIoModel(){
//        return ((DefaultNutsIOManager)session.io()).getModel();
//    }
    private DefaultNutsWorkspaceConfigModel getConfigModel(){
        return ((DefaultNutsWorkspaceConfigManager)session.config()).getModel();
    }
    private NutsBootModel getBootModel(){
        return ((DefaultNutsBootManager)session.boot()).getModel();
    }

    @Override
    public boolean isStdout(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        if (out == getConfigModel().stdout()) {
            return true;
        }
        if (out == getConfigModel().getBootModel().stdout()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStdout(((NutsPrintStreamRendered) out).getBase());
        }
        return false;
    }

    @Override
    public boolean isStderr(NutsPrintStream out) {
        if (out == null) {
            return false;
        }
        if (out == getBootModel().stderr()) {
            return true;
        }
        if (out == getBootModel().stderr()) {
            return true;
        }
        if (out instanceof NutsPrintStreamRendered) {
            return isStdout(((NutsPrintStreamRendered) out).getBase());
        }
        return false;
    }

    @Override
    public NutsPrintStream stdout() {
        return getConfigModel().stdout();
    }

    @Override
    public NutsPrintStream stderr() {
        return getConfigModel().stderr();
    }

}
