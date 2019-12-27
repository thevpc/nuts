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
package net.vpc.app.nuts.runtime.format;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.NutsConfigurableHelper;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;
import net.vpc.app.nuts.runtime.util.fprint.ExtendedFormatAwarePrintWriter;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public abstract class NutsIncrementalOutputFormatBase implements NutsIterableOutput {

    private NutsWorkspace ws;
    private NutsSession session;
    private PrintStream out;
    private NutsFetchDisplayOptions displayOptions;
    private long index;
    private NutsIterableFormat format;

    public NutsIncrementalOutputFormatBase(NutsWorkspace ws) {
        this.ws = ws;
        displayOptions = new NutsFetchDisplayOptions(ws);
    }

    public NutsFetchDisplayOptions getDisplayOptions() {
        return displayOptions;
    }

    protected NutsIterableFormat setFormat(NutsIterableFormat format) {
        this.format = format;
        return format;
    }

    public NutsIterableFormat getEffectiveFormat() {
        return getFormat();
    }

    public final NutsIterableFormat getFormat() {
        return format;
    }

    @Override
    public void start() {
        index = 0;
        NutsIterableFormat e = getEffectiveFormat();
        if (e != null) {
            e.start();
        }
    }

    @Override
    public void next(Object object) {
        NutsIterableFormat e = getEffectiveFormat();
        if (e != null) {
            e.next(object, index);
        }
        index++;
    }

    @Override
    public void complete() {
        NutsIterableFormat e = getEffectiveFormat();
        if (e != null) {
            e.complete(index);
        }
    }

    public PrintStream getValidOut() {
        if (out == null) {
            out = NutsWorkspaceUtils.of(ws).validateSession( getValidSession()).getTerminal().getOut();
        }
//        this.out = ws.io().getTerminalFormat().prepare(out);
        return out;
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
    public final NutsIterableOutput configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "search");
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

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        String[] arr = cmd.toArray();
        if (getDisplayOptions().configureFirst(cmd)) {
            if (getFormat() != null) {
                getFormat().configureFirst(ws.commandLine().create(arr));
            }
            return true;
        }
        if (getFormat() != null) {
            if (getFormat().configureFirst(cmd)) {
                return true;
            }
        }
        return false;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

    @Override
    public NutsIterableOutput out(PrintStream out) {
        return setOut(out);
    }

    @Override
    public NutsIterableOutput setOut(PrintStream out) {
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

    @Override
    public NutsIterableOutput out(PrintWriter out) {
        return setOut(out);
    }

    @Override
    public NutsIterableOutput setOut(PrintWriter out) {
        if (out == null) {
            if (this.out != null) {
                this.out.flush();
            }
            this.out = null;
        } else {
            this.out = CoreIOUtils.toPrintStream(out,getWorkspace());
        }
        return this;
    }

//    public PrintWriter getValidPrintWriter() {
//        if (out == null) {
//            out = getValidSession().getTerminal().getOut();
//        }
//        return out;
////        PrintWriter pout = (out instanceof PrintWriter) ? ((PrintWriter) out) : new ExtendedFormatAwarePrintWriter(out);
////        NutsWorkspaceUtils.of(ws).setWorkspace(pout);
////        return ws.io().getTerminalFormat().prepare(pout);
//    }

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
    public NutsIterableOutput session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsIterableOutput setSession(NutsSession session) {
        //should copy because will change outputformat
        this.session = session == null ? null : session.copy();
        return this;
    }

    public NutsOutputFormat getOutputFormat() {
        NutsOutputFormat format = getValidSession().getOutputFormat();
        return format == null ? NutsOutputFormat.PLAIN : format;
    }

}
