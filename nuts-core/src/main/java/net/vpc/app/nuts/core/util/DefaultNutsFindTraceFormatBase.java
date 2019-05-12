/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsOutputListFormat;
import net.vpc.app.nuts.NutsSession;

/**
 *
 * @author vpc
 */
public abstract class DefaultNutsFindTraceFormatBase<T extends NutsOutputListFormat> implements NutsOutputListFormat {

    private CanonicalBuilder canonicalBuilder;
    private NutsWorkspace ws;
    private NutsSession session;
    private PrintWriter out;
    private NutsOutputFormat format;

    public DefaultNutsFindTraceFormatBase(NutsWorkspace ws, NutsOutputFormat format) {
        this.ws = ws;
        this.format = format;
    }

    public PrintWriter getValidOut() {
        if (out == null) {
            PrintStream out = NutsWorkspaceUtils.validateSession(ws, getValidSession()).getTerminal().getOut();
            this.out = new PrintWriter(out);
        }
        return out;
    }

    @Override
    public NutsOutputListFormat setOption(String name, String value) {
        return this;
    }

    public CanonicalBuilder getCanonicalBuilder() {
        return canonicalBuilder;
    }

    public CanonicalBuilder getValidCanonicalBuilder() {
        if (canonicalBuilder == null) {
            canonicalBuilder = new CanonicalBuilder(getWs()).setConvertDesc(true).setConvertId(false);
        }
        return canonicalBuilder;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    public T out(PrintStream out) {
        return setOut(out);
    }

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

    public T session(NutsSession session) {
        return setSession(session);
    }

    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    public NutsSession getValidSession() {
        if (session == null) {
            session = NutsWorkspaceUtils.validateSession(ws, session);
        }
        return session;
    }

    @Override
    public NutsOutputFormat getSupportedFormat() {
        return format;
    }

}
