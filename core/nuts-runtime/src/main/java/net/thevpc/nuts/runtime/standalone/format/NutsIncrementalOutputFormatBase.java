/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.format;

import java.io.PrintStream;
import java.io.PrintWriter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;

/**
 *
 * @author thevpc
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
        String[] arr = cmd.toStringArray();
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
        return ws.io().term().prepare(out);
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
    public NutsIterableOutput setSession(NutsSession session) {
        //should copy because will change outputformat
        this.session = session == null ? null : session.copy();
        return this;
    }

    public NutsContentType getOutputFormat() {
        NutsContentType format = getValidSession().getOutputFormat();
        return format == null ? NutsContentType.PLAIN : format;
    }

}
