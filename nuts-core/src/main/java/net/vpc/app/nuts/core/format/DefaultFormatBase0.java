/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.NutsCommandLine;

/**
 *
 * @author vpc
 */
public abstract class DefaultFormatBase0<T> implements NutsConfigurable {

    protected NutsWorkspace ws;
    private NutsSession session;
    private String name;

    public DefaultFormatBase0(NutsWorkspace ws, String name) {
        this.ws = ws;
        this.name = name;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    public PrintWriter getValidPrintWriter(Writer out) {
        if (out == null) {
            out = new PrintWriter(getValidSession().getTerminal().getOut());
        }
        PrintWriter pout = (out instanceof PrintWriter) ? ((PrintWriter) out) : new PrintWriter(out);
        return ws.io().getTerminalFormat().prepare(pout);
    }

    public PrintWriter getValidPrintWriter() {
        return getValidPrintWriter(null);
    }

    public PrintStream getValidPrintStream(PrintStream out) {
        if (out == null) {
            out = getValidSession().getTerminal().getOut();
        }
        return ws.io().getTerminalFormat().prepare(out);
    }

    public PrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    public NutsSession getValidSession() {
        if (session == null) {
            session = ws.createSession();
        }
        return session;
    }

    public NutsSession getSession() {
        return session;
    }

    public T session(NutsSession session) {
        return setSession(session);
    }

    public T setSession(NutsSession session) {
        //should copy because will chage outputformat
        this.session = session == null ? null : session.copy();
        return (T) this;
    }

    public String getName() {
        return name;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, getName());
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

}
