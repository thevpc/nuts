/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 */
public abstract class DefaultFormatBase0<T> implements NutsCommandLineConfigurable {

    private final NutsWorkspace workspace;
    private NutsSession session;
    private final String name;
    private boolean ntf=true;

    public DefaultFormatBase0(NutsWorkspace workspace, String name) {
        this.workspace = workspace;
        this.name = name;
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(getWorkspace(), getSession());
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

//    public PrintWriter getValidPrintWriter(Writer out) {
//        checkSession();
//        return (out == null)
//                ? CoreIOUtils.toPrintWriter(getSession().getTerminal().getOut(), getSession())
//                : CoreIOUtils.toPrintWriter(out, getSession());
//    }

//    public PrintWriter getValidPrintWriter() {
//        return getValidPrintWriter(null);
//    }

    public NutsPrintStream getValidPrintStream(NutsPrintStream out) {
        checkSession();
        if (out == null) {
            out = getSession().getTerminal().getOut();
        }
        return out;
    }

    public NutsPrintStream getValidPrintStream() {
        return getValidPrintStream(null);
    }

    public NutsSession getSession() {
        return session;
    }

    public T setSession(NutsSession session) {
        //should copy because will change outputformat
        session = NutsWorkspaceUtils.bindSession(workspace, session);
        this.session =session == null ? null : session.copy();
        return (T) this;
    }

    public String getName() {
        return name;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, getName());
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
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    public boolean isNtf() {
        return ntf;
    }

    public T setNtf(boolean ntf) {
        this.ntf = ntf;
        return (T)this;
    }

    
}
