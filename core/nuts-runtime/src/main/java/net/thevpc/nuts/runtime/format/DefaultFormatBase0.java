/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import net.thevpc.nuts.NutsConfigurable;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.util.NutsConfigurableHelper;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public abstract class DefaultFormatBase0<T> implements NutsConfigurable {

    private NutsWorkspace workspace;
    private NutsSession session;
    private String name;

    public DefaultFormatBase0(NutsWorkspace workspace, String name) {
        this.workspace = workspace;
        this.name = name;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public PrintWriter getValidPrintWriter(Writer out) {
        return (out == null) ?
                CoreIOUtils.toPrintWriter(getValidSession().getTerminal().getOut(), getWorkspace())
                :
                CoreIOUtils.toPrintWriter(out, getWorkspace());
    }

    public PrintWriter getValidPrintWriter() {
        return getValidPrintWriter(null);
    }

    public PrintStream getValidPrintStream(PrintStream out) {
        if (out == null) {
            out = getValidSession().getTerminal().getOut();
        }
        return getWorkspace().io().term().getTerminalFormat().prepare(out);
    }

    public PrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    public NutsSession getValidSession() {
        if (session == null) {
            session = getWorkspace().createSession();
        }
        return session;
    }

    public NutsSession getSession() {
        return session;
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
        return NutsConfigurableHelper.configure(this, getWorkspace(), skipUnsupported, args, getName());
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
        return NutsConfigurableHelper.configure(this, getWorkspace(), skipUnsupported, commandLine);
    }

}
