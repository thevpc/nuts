package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NOutMemoryStream;
import net.thevpc.nuts.io.NOutStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.boot.DefaultNBootModel;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.spi.NTerminals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DefaultNTerminals implements NTerminals {

    public DefaultNWorkspaceConfigModel cmodel;
    public NWorkspace ws;
    public DefaultNBootModel bootModel;

    public DefaultNTerminals(NSession session) {
        this.ws = session.getWorkspace();
        this.cmodel = ((DefaultNWorkspaceConfigManager) session.config()).getModel();
        bootModel = NWorkspaceExt.of(session.getWorkspace()).getModel().bootModel;
    }

    public DefaultNWorkspaceConfigModel getModel() {
        return cmodel;
    }

    private void checkSession(NSession session) {
        NSessionUtils.checkSession(cmodel.getWorkspace(), session);
    }

    @Override
    public NTerminals enableRichTerm(NSession session) {
        checkSession(session);
        bootModel.enableRichTerm(session);
        return this;
    }


    @Override
    public NSessionTerminal createTerminal(NSession session) {
        checkSession(session);
        return cmodel.createTerminal(session);
    }

    @Override
    public NSessionTerminal createTerminal(InputStream in, NOutStream out, NOutStream err, NSession session) {
        checkSession(session);
        return cmodel.createTerminal(in, out, err, session);
    }

    @Override
    public NSessionTerminal createTerminal(NSessionTerminal terminal, NSession session) {
        checkSession(session);
        if (terminal == null) {
            return createTerminal(session);
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
    public NSessionTerminal createMemTerminal(NSession session) {
        return createMemTerminal(false, session);
    }

    @Override
    public NSessionTerminal createMemTerminal(boolean mergeErr, NSession session) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NOutMemoryStream out = NOutMemoryStream.of(session);
        NOutMemoryStream err = mergeErr ? out : NOutMemoryStream.of(session);
        return createTerminal(in, out, err, session);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
