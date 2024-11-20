package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.io.printstream.*;
import net.thevpc.nuts.runtime.standalone.io.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NSystemTerminalBase;

import java.io.*;

public class DefaultNIO implements NIO {
    private final NSession session;
    public DefaultNWorkspaceConfigModel cmodel;
    public DefaultNBootModel bootModel;

    public DefaultNIO(NSession session) {
        this.session = session;
        this.cmodel = ((DefaultNConfigs) NConfigs.of()).getModel();
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
    public OutputStream ofNullRawOutputStream() {
        checkSession();
        return getBootModel().nullOutputStream();
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
        return ((DefaultNConfigs) NConfigs.of()).getModel();
    }


//    @Override
//    public NSessionTerminal createTerminal() {
//        return cmodel.createTerminal(session);
//    }
//
//    @Override
//    public NSessionTerminal createTerminal(InputStream in, NPrintStream out, NPrintStream err) {
//        return cmodel.createTerminal(in, out, err, session);
//    }
//
//    @Override
//    public NSessionTerminal createTerminal(NSessionTerminal terminal) {
//        if (terminal == null) {
//            return createTerminal();
//        }
//        if (terminal instanceof DefaultNSessionTerminalFromSystem) {
//            DefaultNSessionTerminalFromSystem t = (DefaultNSessionTerminalFromSystem) terminal;
//            return new DefaultNSessionTerminalFromSystem(session, t);
//        }
//        if (terminal instanceof DefaultNSessionTerminalFromSession) {
//            DefaultNSessionTerminalFromSession t = (DefaultNSessionTerminalFromSession) terminal;
//            return new DefaultNSessionTerminalFromSession(session, t);
//        }
//        return new DefaultNSessionTerminalFromSession(session, terminal);
//    }
//
//    @Override
//    public NSessionTerminal createInMemoryTerminal() {
//        return createInMemoryTerminal(false);
//    }
//
//    @Override
//    public NSessionTerminal createInMemoryTerminal(boolean mergeErr) {
//        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
//        NMemoryPrintStream out = NMemoryPrintStream.of(session);
//        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of(session);
//        return createTerminal(in, out, err);
//    }

    @Override
    public NSystemTerminal getSystemTerminal() {
        return NWorkspaceExt.of(session).getModel().bootModel.getSystemTerminal();
    }

    @Override
    public NIO setSystemTerminal(NSystemTerminalBase terminal) {
        NWorkspaceExt.of(session).getModel().bootModel.setSystemTerminal(terminal);
        return this;
    }

    @Override
    public NSessionTerminal getDefaultTerminal() {
        return cmodel.getTerminal();
    }

    @Override
    public NIO setDefaultTerminal(NSessionTerminal terminal) {
        cmodel.setTerminal(terminal);
        return this;
    }
}
