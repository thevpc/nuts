package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsTerminalBase;

import java.io.*;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

public class DefaultNutsTerminalManager implements NutsTerminalManager {

    public DefaultNutsTerminalModel model;
    public NutsSession session;

    public DefaultNutsTerminalManager(DefaultNutsTerminalModel model) {
        this.model = model;
    }

    public DefaultNutsTerminalModel getModel() {
        return model;
    }

    public NutsTerminalManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal) {
        checkSession();
        model.setSystemTerminal(terminal, session);
        return this;
    }
    

    @Override
    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec) {
        checkSession();
        return model.createSystemTerminal(spec, session);
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsTerminalManager enableRichTerm() {
        checkSession();
        model.enableRichTerm(session);
        return this;
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        checkSession();
        return model.getSystemTerminal();
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        checkSession();
        return model.getTerminal();
    }

    @Override
    public NutsTerminalManager setTerminal(NutsSessionTerminal terminal) {
        checkSession();
        model.setTerminal(terminal, session);
        return this;
    }

    @Override
    public NutsSessionTerminal createTerminal(InputStream in, PrintStream out, PrintStream err) {
        checkSession();
        return model.createTerminal(in, out, err, session);
    }

    @Override
    public NutsSessionTerminal createTerminal() {
        checkSession();
        return model.createTerminal(session);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsTerminalBase parent) {
        checkSession();
        return model.createTerminal(parent, session);
    }

    @Override
    public PrintStream prepare(PrintStream out) {
        checkSession();
        return model.prepare(out, session);
    }

    @Override
    public PrintWriter prepare(PrintWriter out) {
        checkSession();
        return model.prepare(out, session);
    }

    @Override
    public boolean isFormatted(OutputStream out) {
        checkSession();
        return model.isFormatted(out);
    }

    @Override
    public boolean isFormatted(Writer out) {
        checkSession();
        return model.isFormatted(out);
    }

    @Override
    public NutsTerminalManager sendTerminalCommand(OutputStream out, NutsTerminalCommand command) {
        checkSession();
        model.sendTerminalCommand(out, command,session);
        return this;
    }

}
