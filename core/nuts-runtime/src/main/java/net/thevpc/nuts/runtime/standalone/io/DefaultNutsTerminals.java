package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminalFromSession;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.spi.NutsTerminals;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class DefaultNutsTerminals implements NutsTerminals {

    public DefaultNutsWorkspaceConfigModel model;
    public NutsWorkspace ws;

    public DefaultNutsTerminals(NutsSession session) {
        this.ws = session.getWorkspace();
        this.model = ((DefaultNutsWorkspaceConfigManager) session.config()).getModel();
    }

    public DefaultNutsWorkspaceConfigModel getModel() {
        return model;
    }

    private void checkSession(NutsSession session) {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsTerminals enableRichTerm(NutsSession session) {
        checkSession(session);
        model.enableRichTerm(session);
        return this;
    }


    @Override
    public NutsSessionTerminal createTerminal(NutsSession session) {
        checkSession(session);
        return model.createTerminal(session);
    }

    @Override
    public NutsSessionTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err, NutsSession session) {
        checkSession(session);
        return model.createTerminal(in, out, err, session);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsSessionTerminal terminal, NutsSession session) {
        checkSession(session);
        if (terminal == null) {
            return createTerminal(session);
        }
        if (terminal instanceof DefaultNutsSessionTerminalFromSystem) {
            DefaultNutsSessionTerminalFromSystem t = (DefaultNutsSessionTerminalFromSystem) terminal;
            return new DefaultNutsSessionTerminalFromSystem(session, t);
        }
        if (terminal instanceof DefaultNutsSessionTerminalFromSession) {
            DefaultNutsSessionTerminalFromSession t = (DefaultNutsSessionTerminalFromSession) terminal;
            return new DefaultNutsSessionTerminalFromSession(session, t);
        }
        return new DefaultNutsSessionTerminalFromSession(session, terminal);
    }

    @Override
    public NutsSessionTerminal createMemTerminal(NutsSession session) {
        return createMemTerminal(false, session);
    }

    @Override
    public NutsSessionTerminal createMemTerminal(boolean mergeErr, NutsSession session) {
        ByteArrayInputStream in = new ByteArrayInputStream(new byte[0]);
        NutsMemoryPrintStream out = NutsMemoryPrintStream.of(session);
        NutsMemoryPrintStream err = mergeErr ? out : NutsMemoryPrintStream.of(session);
        return createTerminal(in, out, err, session);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
