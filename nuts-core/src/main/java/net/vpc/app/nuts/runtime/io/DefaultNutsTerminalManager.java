package net.vpc.app.nuts.runtime.io;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.DefaultNutsWorkspaceEvent;
import net.vpc.app.nuts.runtime.terminals.*;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;

public class DefaultNutsTerminalManager implements NutsTerminalManager {
    private NutsWorkspace ws;
    private NutsTerminalFormat terminalMetrics = new DefaultNutsTerminalFormat();
    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;

    public DefaultNutsTerminalManager(NutsWorkspace ws) {
        this.ws = ws;
        NutsWorkspaceUtils.of(ws).setWorkspace(terminalMetrics);
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);
    }

    @Override
    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec) {
        NutsSystemTerminalBase termb = ws.extensions().createSupported(NutsSystemTerminalBase.class, spec);
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
    public NutsTerminalManager enableRichTerm() {
        NutsSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NutsWorkspaceExtensionManager extensions = getWorkspace().extensions();
            extensions.loadExtension(ws.id().parser().parse("net.vpc.app.nuts.ext:next-term#"+getWorkspace().getApiVersion()));
            getWorkspace().io().term().setSystemTerminal(
                    createSystemTerminal(new NutsDefaultTerminalSpec()
                            .setAutoComplete(true)
                    ));
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
    public NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal) {
        //TODO : should pass session in method
        return setSystemTerminal(terminal, null);
    }

    @Override
    public NutsTerminalManager setTerminal(NutsSessionTerminal terminal) {
        if (terminal == null) {
            terminal = createTerminal();
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
    public NutsSessionTerminal createTerminal() {
        return createTerminal(null);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        if (parent == null) {
            parent = workspaceSystemTerminalAdapter;
        }
        NutsSessionTerminalBase termb = ws.extensions().createSupported(NutsSessionTerminalBase.class, null);
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
