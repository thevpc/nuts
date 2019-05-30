/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsIncrementalFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public abstract class DefaultSearchFormatBase<T extends NutsIncrementalFormat> implements NutsIncrementalFormat {

    private CanonicalBuilder canonicalBuilder;
    private NutsWorkspace ws;
    private NutsSession session;
    private PrintWriter out;
    private NutsOutputFormat format;
    private NutsFetchDisplayOptions displayOptions;

    public DefaultSearchFormatBase(NutsWorkspace ws, NutsOutputFormat format) {
        this.ws = ws;
        this.format = format;
        displayOptions = new NutsFetchDisplayOptions(ws);
    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    @Override
    public void formatStart() {
        //
    }

    @Override
    public void formatComplete(long count){
        //
    }
    

    public PrintWriter getValidOut() {
        if (out == null) {
            PrintStream out = NutsWorkspaceUtils.validateSession(ws, getValidSession()).getTerminal().getOut();
            this.out = ws.io().getTerminalFormat().prepare(new PrintWriter(out));
        }
        this.out = ws.io().getTerminalFormat().prepare(out);
        return out;
    }

    @Override
    public final NutsIncrementalFormat configure(String... args) {
        return NutsConfigurableHelper.configure(this, ws, args,"search");
    }

    @Override
    public final boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        return NutsConfigurableHelper.configure(this, ws, commandLine,skipIgnored);
    }

 @Override
    public boolean configureFirst(NutsCommand cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if(getDisplayOptions().configureFirst(cmd)){
            return true;
        }
//        switch (a.getKey().getString()) {
//            case "--long": {
//                this.longFormat = cmd.nextBoolean().getValue().getBoolean();
//                return true;
//            }
//        }
        return false;
    }

    public CanonicalBuilder getCanonicalBuilder() {
        return canonicalBuilder;
    }

    public CanonicalBuilder getValidCanonicalBuilder() {
        if (canonicalBuilder == null) {
            canonicalBuilder = new CanonicalBuilder(getWs(), displayOptions).setConvertDesc(true).setConvertId(false);
        }
        return canonicalBuilder;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    @Override
    public T out(PrintStream out) {
        return setOut(out);
    }

    @Override
    public T setOut(PrintStream out) {
        if (out == null) {
            if (this.out != null) {
                this.out.flush();
            }
            this.out = null;
        } else {
            this.out = new PrintWriter(out);
        }
        return (T) this;
    }

    public T out(PrintWriter out) {
        return setOut(out);
    }

    public T setOut(PrintWriter out) {
        if (out == null) {
            if (this.out != null) {
                this.out.flush();
            }
            this.out = null;
        } else {
            this.out = out;
        }
        return (T) this;
    }

    public PrintWriter getValidPrintWriter() {
        if (out == null) {
            out = new PrintWriter(getValidSession().getTerminal().getOut());
        }
        PrintWriter pout = (out instanceof PrintWriter) ? ((PrintWriter) out) : new PrintWriter(out);
        return ws.io().getTerminalFormat().prepare(pout);
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

    @Override
    public NutsOutputFormat getSupportedFormat() {
        return format;
    }

}
