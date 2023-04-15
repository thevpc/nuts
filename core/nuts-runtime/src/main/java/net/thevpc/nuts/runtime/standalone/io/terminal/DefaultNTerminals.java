package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NTerminals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DefaultNTerminals implements NTerminals {

    public DefaultNWorkspaceConfigModel cmodel;
    public NSession session;
    public DefaultNBootModel bootModel;

    public DefaultNTerminals(NSession session) {
        this.session = session;
        this.cmodel = ((DefaultNConfigs) NConfigs.of(session)).getModel();
        bootModel = NWorkspaceExt.of(session.getWorkspace()).getModel().bootModel;
    }

    public DefaultNWorkspaceConfigModel getModel() {
        return cmodel;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(cmodel.getWorkspace(), session);
    }

    @Override
    public NTerminals enableRichTerm() {
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
    public NSessionTerminal createMemTerminal() {
        return createMemTerminal(false);
    }

    @Override
    public NSessionTerminal createMemTerminal(boolean mergeErr) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NMemoryPrintStream out = NMemoryPrintStream.of(session);
        NMemoryPrintStream err = mergeErr ? out : NMemoryPrintStream.of(session);
        return createTerminal(in, out, err);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
