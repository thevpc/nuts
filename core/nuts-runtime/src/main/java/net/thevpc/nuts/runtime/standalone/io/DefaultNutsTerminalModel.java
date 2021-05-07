package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.events.DefaultNutsWorkspaceEvent;
import net.thevpc.nuts.runtime.core.format.text.ExtendedFormatAware;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.runtime.core.terminals.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsSessionTerminalBase;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.*;
import java.util.logging.Level;

public class DefaultNutsTerminalModel {

    private NutsWorkspace ws;
    private NutsSessionTerminal terminal;
    private NutsSystemTerminal systemTerminal;
    private WorkspaceSystemTerminalAdapter workspaceSystemTerminalAdapter;
    private NutsLogger LOG;

    public DefaultNutsTerminalModel(NutsWorkspace ws) {
        this.ws = ws;
        workspaceSystemTerminalAdapter = new WorkspaceSystemTerminalAdapter(ws);
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = this.ws.log().setSession(session).of(DefaultNutsTerminalModel.class);
        }
        return LOG;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec, NutsSession session) {
        NutsSystemTerminalBase termb = ws.extensions()
                .setSession(session)
                .createSupported(NutsSystemTerminalBase.class, spec);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        return NutsSystemTerminal_of_NutsSystemTerminalBase(termb, session);
    }

    public void enableRichTerm(NutsSession session) {
        NutsSystemTerminal st = getSystemTerminal();
        if (st.isAutoCompleteSupported()) {
            //that's ok
        } else {
            NutsWorkspace ws = session.getWorkspace();
            NutsId extId = ws.id().parser().parse("net.thevpc.nuts.ext:next-term#" + ws.getApiVersion());
            if (!ws.config().isExcludedExtension(extId.toString(), ws.config().options())) {
                NutsWorkspaceExtensionManager extensions = ws.extensions();
                extensions.setSession(session).loadExtension(extId);
                NutsSystemTerminal systemTerminal = createSystemTerminal(
                        new NutsDefaultTerminalSpec()
                                .setAutoComplete(true),
                        session
                );
                setSystemTerminal(systemTerminal, session);
                if (getSystemTerminal().isAutoCompleteSupported()) {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.SUCCESS).log("enable rich terminal");
                } else {
                    _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.FAIL).log("unable to enable rich terminal");
                }
            } else {
                _LOGOP(session).level(Level.FINE).verb(NutsLogVerb.WARNING).log("enableRichTerm discarded; next-term is excluded.");
            }
        }
    }

    public NutsSystemTerminal getSystemTerminal() {
        return systemTerminal;
    }

    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    public void setTerminal(NutsSessionTerminal terminal, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        if (terminal == null) {
            terminal = createTerminal(session);
        }
        if (!(terminal instanceof UnmodifiableTerminal)) {
            terminal = new UnmodifiableTerminal(terminal, session);
        }
        this.terminal = terminal;
    }

    public NutsSessionTerminal createTerminal(InputStream in, PrintStream out, PrintStream err, NutsSession session) {
        NutsSessionTerminal t = createTerminal(session);
        if (in != null) {
            t.setIn(in);
        }
        if (out != null) {
            t.setOut(out);
        }
        if (err != null) {
            t.setErr(err);
        }
        return t;
    }

    public NutsSessionTerminal createTerminal(NutsSession session) {
        return createTerminal(null, session);
    }

    public NutsSessionTerminal createTerminal(NutsTerminalBase parent, NutsSession session) {
        if (parent == null) {
            parent = workspaceSystemTerminalAdapter;
        }
        NutsSessionTerminalBase termb = ws.extensions()
                .setSession(session)
                .createSupported(NutsSessionTerminalBase.class, null);
        if (termb == null) {
            throw new NutsExtensionNotFoundException(session, NutsSessionTerminal.class, "SessionTerminalBase");
        }
        NutsWorkspaceUtils.setSession(termb, session);
        try {
            NutsSessionTerminal term = null;
            if (termb instanceof NutsSessionTerminal) {
                term = (NutsSessionTerminal) termb;
                NutsWorkspaceUtils.setSession(term, session);
                term.setParent(parent);
            } else {
                term = new DefaultNutsSessionTerminal();
                NutsWorkspaceUtils.setSession(term, session);
                term.setParent(termb);
            }
            return term;
        } catch (Exception anyException) {
            final NutsSessionTerminal c = new DefaultNutsSessionTerminal();
            NutsWorkspaceUtils.setSession(c, session);
            c.setParent(parent);
            return c;
        }
    }

    private NutsSystemTerminal NutsSystemTerminal_of_NutsSystemTerminalBase(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst;
        if ((terminal instanceof NutsSystemTerminal)) {
            syst = (NutsSystemTerminal) terminal;
        } else {
            try {
                syst = new DefaultSystemTerminal(terminal);
                NutsWorkspaceUtils.setSession(syst, session);
            } catch (Exception ex) {
                _LOGOP(session).level(Level.FINEST).verb(NutsLogVerb.WARNING).log("unable to create system terminal : {0}", ex.getMessage());
                syst = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBase());
                NutsWorkspaceUtils.setSession(syst, session);
            }
        }
        return syst;
    }

    public void setSystemTerminal(NutsSystemTerminalBase terminal, NutsSession session) {
        if (terminal == null) {
            throw new NutsExtensionNotFoundException(session, NutsSystemTerminalBase.class, "SystemTerminalBase");
        }
        NutsSystemTerminal syst = NutsSystemTerminal_of_NutsSystemTerminalBase(terminal, session);
        if (this.systemTerminal != null) {
            NutsWorkspaceUtils.unsetWorkspace(this.systemTerminal);
        }
        NutsSystemTerminal old = this.systemTerminal;
        this.systemTerminal = syst;

        if (old != this.systemTerminal) {
            NutsWorkspaceEvent event = null;
            if (session != null) {
                for (NutsWorkspaceListener workspaceListener : session.getWorkspace().events().getWorkspaceListeners()) {
                    if (event == null) {
                        event = new DefaultNutsWorkspaceEvent(session, null, "systemTerminal", old, this.systemTerminal);
                    }
                    workspaceListener.onUpdateProperty(event);
                }
            }
        }
    }

    public PrintStream prepare(PrintStream out, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        return CoreIOUtils.toPrintStream(out, session);
    }

    public PrintWriter prepare(PrintWriter out, NutsSession session) {
        NutsWorkspaceUtils.checkSession(ws, session);
        return CoreIOUtils.toPrintWriter(out, session);
    }

    public boolean isFormatted(OutputStream out) {
        if (out instanceof ExtendedFormatAware) {
            NutsTerminalModeOp op = ((ExtendedFormatAware) out).getModeOp();
            return op != NutsTerminalModeOp.NOP;
        }
        return false;
    }

    public boolean isFormatted(Writer out) {
        if (out instanceof ExtendedFormatAware) {
            NutsTerminalModeOp op = ((ExtendedFormatAware) out).getModeOp();
            return op != NutsTerminalModeOp.NOP;
        }
        return false;
    }

    public void sendTerminalCommand(OutputStream out, NutsTerminalCommand command, NutsSession session) {
        if (isFormatted(out)) {
            if (out instanceof PrintStream) {
                try {
                    ((PrintStream) out).printf("%s", session.getWorkspace().formats().text().forCommand(command));
                    out.flush();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            } else {
                try {
                    ((PrintStream) out).write(session.getWorkspace().formats().text().forCommand(command).toString().getBytes());
                    out.flush();
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
    }

    private static class WorkspaceSystemTerminalAdapter extends AbstractSystemTerminalAdapter {

        private NutsWorkspace workspace;

        public WorkspaceSystemTerminalAdapter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        public NutsSystemTerminalBase getParent() {
            return workspace.term()
                    .setSession(NutsWorkspaceUtils.defaultSession(workspace))
                    .getSystemTerminal();
        }
    }
}
