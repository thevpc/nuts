package net.thevpc.nuts.runtime.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.log.NutsLogVerb;
import net.thevpc.nuts.runtime.terminals.*;
import net.thevpc.nuts.runtime.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSessionTerminalBase;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.util.logging.Level;

public class DefaultNutsTerminalManager implements NutsTerminalManager {
    private NutsWorkspace ws;
    private NutsTerminalFormat terminalMetrics = new DefaultNutsTerminalFormat();
    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;
    public NutsLogger LOG;

    public DefaultNutsTerminalManager(NutsWorkspace ws) {
        this.ws = ws;
        this.LOG = ws.log().of(DefaultNutsTerminalManager.class);
        NutsWorkspaceUtils.of(ws).setWorkspace(terminalMetrics);
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);
    }

    @Override
    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec) {
        NutsSystemTerminalBase termb = ws.extensions().createSupported(NutsSystemTerminalBase.class, spec, spec.getSession());
        if (termb == null) {
            throw new NutsExtensionNotFoundException(ws, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb);
    }

    @Override
    public NutsSystemTerminal systemTerminal() {
        return getSystemTerminal();
    }

    @Override
    public NutsTerminalManager enableRichTerm(NutsSession session) {
        NutsSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NutsId extId = ws.id().parser().parse("net.thevpc.nuts.ext:next-term#" + getWorkspace().getApiVersion());
            if(!getWorkspace().config().options().isExcludedExtension(extId.toString())) {
                NutsWorkspaceExtensionManager extensions = getWorkspace().extensions();
                extensions.loadExtension(extId, session);
                NutsSystemTerminal systemTerminal = createSystemTerminal(new NutsDefaultTerminalSpec()
                        .setAutoComplete(true)
                        .setSession(session)
                );
                setSystemTerminal(systemTerminal, session);
                if(getSystemTerminal().isAutoCompleteSupported()) {
                    LOG.with().level(Level.FINE).verb(NutsLogVerb.SUCCESS).log("enable rich terminal");
                }else{
                    LOG.with().level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to enable rich terminal");
                }
            }else{
                LOG.with().level(Level.FINE).verb(NutsLogVerb.WARNING).log("enableRichTerm discarded; next-term is excluded.");
            }
        }
        return this;
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    @Override
    public NutsSessionTerminal terminal() {
        return getTerminal();
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsTerminalManager setTerminal(NutsSessionTerminal terminal, NutsSession session) {
        if (terminal == null) {
            terminal = createTerminal(session);
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal);
        }
        this.terminal = terminal;
        return this;
    }

    @Override
    public NutsTerminalFormat getTerminalFormat() {
        return terminalMetrics;
    }


    @Override
    public NutsSessionTerminal createTerminal(NutsSession session) {
        return createTerminal(null, session);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent, NutsSession session) {
        if (parent == null) {
            parent = workspaceSystemTerminalAdapter;
        }
        NutsSessionTerminalBase termb = ws.extensions().createSupported(NutsSessionTerminalBase.class, null, session);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(ws, NutsSessionTerminal.class, "SessionTerminalBase");
        }
        NutsWorkspaceUtils.of(ws).setWorkspace(termb);
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                NutsWorkspaceUtils.of(ws).setWorkspace(term);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                NutsWorkspaceUtils.of(ws).setWorkspace(term);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            NutsWorkspaceUtils.of(ws).setWorkspace(c);
            c.setParent(parent);
            return c;
        }
    }
    private NutsSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NutsSystemTerminalBase terminal){
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(getWorkspace(), NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((terminal instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NutsWorkspaceUtils.of(ws).setWorkspace(syst);
            } catch (Exception ex) {
                LOG.with().level(Level.FINEST).verb(NutsLogVerb.WARNING).log("unable to create system terminal : %s",ex.getMessage());
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                NutsWorkspaceUtils.of(ws).setWorkspace(syst);

            }
        }
        return syst;
    }

    public NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(getWorkspace(), NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst=NutsSystemTerminal_of_NutsSystemTerminalBase(terminal);
        if (this.systemTerminal != null) {
            NutsWorkspaceUtils.unsetWorkspace(this.systemTerminal);
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            if (session != null) {
                for (NutsWorkspaceListener workspaceListener : getWorkspace().events().getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNutsWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }
        return this;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private NutsWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public NutsSystemTerminalBase getParent() {
            return workspace.io().term().getSystemTerminal();
        }
    }
}
