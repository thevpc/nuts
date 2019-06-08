/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.format;

import java.io.PrintStream;
import java.io.PrintWriter;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.NutsIncrementalFormat;
import net.vpc.app.nuts.NutsIncrementalFormatContext;
import net.vpc.app.nuts.NutsIncrementalFormatHandler;

/**
 *
 * @author vpc
 */
public abstract class NutsIncrementalOutputFormatBase implements NutsIncrementalFormat {

    private NutsWorkspace ws;
    private NutsSession session;
    private PrintWriter out;
    private NutsFetchDisplayOptions displayOptions;
    private long index;
    private NutsIncrementalFormatHandler handler;
    private NutsIncrementalFormatContext context = new NutsIncrementalFormatContext() {
        public PrintWriter getWriter() {
            return NutsIncrementalOutputFormatBase.this.getValidOut();
        }

        @Override
        public NutsSession getSession() {
            return NutsIncrementalOutputFormatBase.this.getValidSession();
        }

        @Override
        public NutsWorkspace getWorkspace() {
            return NutsIncrementalOutputFormatBase.this.getWorkspace();
        }

    };

    public NutsIncrementalOutputFormatBase(NutsWorkspace ws) {
        this.ws = ws;
        displayOptions = new NutsFetchDisplayOptions(ws);
    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    protected NutsIncrementalFormatHandler prepare(NutsIncrementalFormatHandler helper) {
        this.handler = helper;
        this.handler.init(context);
        return helper;
    }

    public NutsIncrementalFormatHandler getHandler() {
        return handler;
    }

    @Override
    public void start() {
        index = 0;
        getHandler().start(context);
    }

    @Override
    public void next(Object object) {
        getHandler().next(context, object, index);
        index++;
    }

    @Override
    public void complete() {
        getHandler().complete(context, index);
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
    public final NutsIncrementalFormat configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "search");
    }

    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        return getHandler().configureFirst(cmd);
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsIncrementalFormat out(PrintStream out) {
        return setOut(out);
    }

    @Override
    public NutsIncrementalFormat setOut(PrintStream out) {
        if (out == null) {
            if (this.out != null) {
                this.out.flush();
            }
            this.out = null;
        } else {
            this.out = new PrintWriter(out);
        }
        return this;
    }

    public NutsIncrementalFormat out(PrintWriter out) {
        return setOut(out);
    }

    public NutsIncrementalFormat setOut(PrintWriter out) {
        if (out == null) {
            if (this.out != null) {
                this.out.flush();
            }
            this.out = null;
        } else {
            this.out = out;
        }
        return this;
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

    @Override
    public NutsIncrementalFormat session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsIncrementalFormat setSession(NutsSession session) {
        //should copy because will chage outputformat
        this.session = session == null ? null : session.copy();
        return this;
    }

    public NutsOutputFormat getOutputFormat() {
        NutsOutputFormat format = getValidSession().getOutputFormat();
        return format == null ? NutsOutputFormat.PLAIN : format;
    }

}
