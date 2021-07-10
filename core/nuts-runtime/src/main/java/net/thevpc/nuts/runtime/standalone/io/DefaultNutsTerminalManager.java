package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal2;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.io.InputStream;

public class DefaultNutsTerminalManager implements NutsTerminalManager {

    public DefaultNutsTerminalModel model;
    public NutsSession session;

    public DefaultNutsTerminalManager(DefaultNutsTerminalModel model) {
        this.model = model;
    }

    public DefaultNutsTerminalModel getModel() {
        return model;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        checkSession();
        return model.getSystemTerminal();
    }

    @Override
    public NutsTerminalManager setSystemTerminal(NutsSystemTerminalBase terminal) {
        checkSession();
        model.setSystemTerminal(terminal, session);
        return this;
    }

    @Override
    public NutsTerminalManager enableRichTerm() {
        checkSession();
        model.enableRichTerm(session);
        return this;
    }

    @Override
    public NutsSystemTerminal createSystemTerminal(NutsTerminalSpec spec) {
        checkSession();
        return model.createSystemTerminal(spec, session);
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
    public NutsSessionTerminal createTerminal() {
        checkSession();
        return model.createTerminal(session);
    }

    @Override
    public NutsSessionTerminal createTerminal(InputStream in, NutsPrintStream out, NutsPrintStream err) {
        checkSession();
        return model.createTerminal(in, out, err, session);
    }

    @Override
    public NutsSessionTerminal createTerminal(NutsSessionTerminal terminal) {
        checkSession();
        if (terminal == null) {
            return createTerminal();
        }
        if(terminal instanceof DefaultNutsSessionTerminal){
            DefaultNutsSessionTerminal t = (DefaultNutsSessionTerminal) terminal;
            return new DefaultNutsSessionTerminal(session, t);
        }
        if(terminal instanceof DefaultNutsSessionTerminal2){
            DefaultNutsSessionTerminal2 t = (DefaultNutsSessionTerminal2) terminal;
            return new DefaultNutsSessionTerminal2(session, t);
        }
        return new DefaultNutsSessionTerminal2(session, terminal);
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsTerminalManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

//    @Override
//    public NutsSessionTerminal createTerminal(NutsSystemTerminalBase parent) {
//        checkSession();
//        return model.createTerminal(parent, session);
//    }

//    @Override
//    public PrintStream prepare(PrintStream out) {
//        checkSession();
//        return model.prepare(out, session);
//    }
//
//    @Override
//    public PrintWriter prepare(PrintWriter out) {
//        checkSession();
//        return model.prepare(out, session);
//    }

//    @Override
//    public boolean isFormatted(OutputStream out) {
//        checkSession();
//        return model.isFormatted(out);
//    }
//
//    @Override
//    public boolean isFormatted(Writer out) {
//        checkSession();
//        return model.isFormatted(out);
//    }

//    @Override
//    public NutsTerminalManager sendTerminalCommand(NutsPrintStream out, NutsTerminalCommand command) {
//        checkSession();
//        model.sendTerminalCommand(out, command,session);
//        return this;
//    }

}
