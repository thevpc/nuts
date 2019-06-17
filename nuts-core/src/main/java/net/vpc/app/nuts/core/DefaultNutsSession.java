/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import java.io.PrintStream;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsPropertiesHolder;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.lang.reflect.Array;
import java.util.*;
import net.vpc.app.nuts.core.format.CustomNutsIncrementalOutputFormat;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNutsSession implements Cloneable, NutsSession {

    private NutsSessionTerminal terminal;
    private NutsPropertiesHolder properties = new NutsPropertiesHolder();
    private List<NutsListener> listeners = new ArrayList<>();
    private boolean trace;
    private boolean verbose = false;
    private NutsConfirmationMode confirm = null;
    private NutsOutputFormat outputFormat;
    protected NutsIterableFormat iterFormatHandler = null;
    protected NutsIterableOutput iterFormat = null;
    protected NutsWorkspace ws = null;
    protected List<String> outputFormatOptions = new ArrayList<>();
    private PrintStream out;
    private PrintStream err;
    private PrintStream out0;
    private PrintStream err0;

    public DefaultNutsSession(NutsWorkspace ws) {
        this.ws = ws;
        this.trace = ws.config().options().isTrace();
    }

    @Override
    public NutsSession copy() {
        try {
            DefaultNutsSession cloned = (DefaultNutsSession) clone();
            cloned.properties = properties == null ? null : properties.copy();
            cloned.outputFormatOptions = outputFormatOptions == null ? null : new ArrayList<>(outputFormatOptions);
            cloned.listeners = listeners == null ? null : new ArrayList<>(listeners);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(ws, e);
        }
    }

    @Override
    public NutsSession addListeners(NutsListener listener) {
        if (listener != null) {
            if (listeners == null) {
                listeners = new ArrayList<>();
            }
            listeners.add(listener);
        }
        return this;
    }

    @Override
    public NutsSession removeListeners(NutsListener listener) {
        if (listener != null) {
            if (listeners != null) {
                listeners.remove(listener);
            }
        }
        return this;
    }

    @Override
    public <T extends NutsListener> T[] getListeners(Class<T> type) {
        if (listeners != null) {
            List<NutsListener> found = new ArrayList<>();
            for (NutsListener listener : listeners) {
                if (type.isInstance(listener)) {
                    found.add(listener);
                }
            }
            return found.toArray((T[]) Array.newInstance(type, 0));
        }
        return (T[]) Array.newInstance(type, 0);
    }

    @Override
    public NutsListener[] getListeners() {
        if (listeners == null) {
            return new NutsListener[0];
        }
        return listeners.toArray(new NutsListener[0]);
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties.getProperties();
    }

    @Override
    public NutsSession setProperties(Map<String, Object> properties) {
        this.properties.setProperties(properties);
        return this;
    }

    @Override
    public Object getProperty(String key) {
        return properties.getProperty(key);
    }

    @Override
    public NutsSession setProperty(String key, Object value) {
        this.properties.setProperty(key, value);
        return this;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsSessionTerminal terminal() {
        return getTerminal();
    }

    @Override
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        this.out0 = (terminal.fout());
        this.err0 = (terminal.ferr());
        this.out = out0;
        this.err = err0;
        return this;
    }

    @Override
    public NutsIterableFormat getIterableFormat() {
        return iterFormatHandler;
    }

    @Override
    public NutsIterableOutput getIterableOutput() {
        if (iterFormatHandler == null) {
            return null;
        }
        if (iterFormat == null) {
            iterFormat = new CustomNutsIncrementalOutputFormat(ws, iterFormatHandler);
            iterFormat.session(this);
        }
        return iterFormat;
    }

    @Override
    public NutsSession iterableFormat(NutsIterableFormat traceFormat) {
        return setIterableFormat(traceFormat);
    }

    @Override
    public NutsSession setIterableFormat(NutsIterableFormat f) {
        if (f == null) {
            this.iterFormatHandler = null;
        } else {
            this.iterFormatHandler = f;
            this.setOutputFormat(f.getOutputFormat());
        }
        return this;
    }

    @Override
    public NutsSession json() {
        return setOutputFormat(NutsOutputFormat.JSON);
    }

    @Override
    public NutsSession plain() {
        return setOutputFormat(NutsOutputFormat.PLAIN);
    }

    @Override
    public NutsSession tree() {
        return setOutputFormat(NutsOutputFormat.TREE);
    }

    @Override
    public NutsSession table() {
        return setOutputFormat(NutsOutputFormat.TABLE);
    }

    @Override
    public NutsSession xml() {
        return setOutputFormat(NutsOutputFormat.XML);
    }

    @Override
    public NutsSession props() {
        return setOutputFormat(NutsOutputFormat.PROPS);
    }

    @Override
    public NutsSession outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    @Override
    public NutsSession setOutputFormat(NutsOutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsOutputFormat.PLAIN;
        }
        this.outputFormat = outputFormat;
        return this;
    }

    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, commandLine);
    }

    @Override
    public Object configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args, "nuts-session");
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument arg = cmdLine.peek();
        if (arg != null) {
            switch (arg.getStringKey()) {
                case "--output-format": {
                    arg = cmdLine.nextString();
                    NutsOutputFormat outf = CoreCommonUtils.parseEnumString(arg.getStringValue(), NutsOutputFormat.class, false);
                    this.setOutputFormat(outf);
                    cmdLine.skip();
                    return true;
                }
                case "-T":
                case "--output-format-option": {
                    arg = cmdLine.nextString();
                    this.addOutputFormatOptions(arg.getStringValue());
                    cmdLine.skip();
                    return true;
                }
                case "--json": {
                    this.setOutputFormat(NutsOutputFormat.JSON);
                    cmdLine.skip();
                    return true;
                }
                case "--props": {
                    this.setOutputFormat(NutsOutputFormat.PROPS);
                    cmdLine.skip();
                    return true;
                }
                case "--plain": {
                    this.setOutputFormat(NutsOutputFormat.PLAIN);
                    cmdLine.skip();
                    return true;
                }
                case "--table": {
                    this.setOutputFormat(NutsOutputFormat.TABLE);
                    cmdLine.skip();
                    return true;
                }
                case "--tree": {
                    this.setOutputFormat(NutsOutputFormat.TREE);
                    cmdLine.skip();
                    return true;
                }
                case "--xml": {
                    this.setOutputFormat(NutsOutputFormat.XML);
                    cmdLine.skip();
                    return true;
                }
                case "--force":
                case "--yes":
                case "-y": {
                    this.setConfirm(NutsConfirmationMode.YES);
                    cmdLine.skip();
                    return true;
                }
                case "--ask": {
                    this.setConfirm(NutsConfirmationMode.ASK);
                    cmdLine.skip();
                    return true;
                }
                case "--no":
                case "-n": {
                    this.setConfirm(NutsConfirmationMode.NO);
                    cmdLine.skip();
                    return true;
                }
                case "--error": {
                    this.setConfirm(NutsConfirmationMode.ERROR);
                    cmdLine.skip();
                    return true;
                }
                case "--trace": {
                    this.setTrace(cmdLine.nextBoolean().getBooleanValue());
                    return true;
                }
                case "--verbose": {
                    this.setVerbose(cmdLine.nextBoolean().getBooleanValue());
                    return true;
                }
                case "--term-system": {
                    cmdLine.skip();
                    setTerminalMode(null);
                    return true;
                }
                case "--term-filtered": {
                    cmdLine.skip();
                    setTerminalMode(NutsTerminalMode.FILTERED);
                    return true;
                }
                case "--no-color": {
                    cmdLine.skip();
                    setTerminalMode(NutsTerminalMode.FILTERED);
                    return true;
                }
                case "--term-formatted": {
                    cmdLine.skip();
                    setTerminalMode(NutsTerminalMode.FORMATTED);
                    return true;
                }
                case "--term-inherited": {
                    cmdLine.skip();
                    setTerminalMode(NutsTerminalMode.INHERITED);
                    return true;
                }
                case "--term": {
                    String s = cmdLine.nextString().getStringValue("").toLowerCase();
                    switch (s) {
                        case "":
                        case "system":
                        case "auto": {
                            setTerminalMode(null);
                            break;
                        }
                        case "filtered": {
                            setTerminalMode(NutsTerminalMode.FILTERED);
                            break;
                        }
                        case "formatted": {
                            setTerminalMode(NutsTerminalMode.FORMATTED);
                            break;
                        }
                        case "inherited": {
                            setTerminalMode(NutsTerminalMode.INHERITED);
                            break;
                        }
                    }
                    return true;
                }
                case "--color": {
                    String s = cmdLine.nextString().getStringValue("").toLowerCase();
                    switch (s) {
                        case "":
                        case "system":
                        case "auto": {
                            setTerminalMode(null);
                            break;
                        }
                        case "filtered":
                        case "never": {
                            setTerminalMode(NutsTerminalMode.FILTERED);
                            break;
                        }
                        case "always":
                        case "formatted": {
                            setTerminalMode(NutsTerminalMode.FORMATTED);
                            break;
                        }
                        case "inherited": {
                            setTerminalMode(NutsTerminalMode.INHERITED);
                            break;
                        }
                        default: {
                            Boolean bval = cmdLine.newArgument(s).getBoolean(false);
                            setTerminalMode(bval ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public NutsOutputFormat getOutputFormat(NutsOutputFormat defaultValue) {
        return outputFormat == null ? defaultValue : outputFormat;
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
        NutsIterableFormat f = getIterableFormat();
        if (f != null) {
            NutsOutputFormat o = f.getOutputFormat();
            if (o != null) {
                return o;
            }
        }
        if (this.outputFormat != null) {
            return this.outputFormat;
        }
        NutsOutputFormat o = ws.config().options().getOutputFormat();
        if (o != null) {
            return o;
        }
        return NutsOutputFormat.PLAIN;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public boolean isPlainTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() == NutsOutputFormat.PLAIN;
    }

    @Override
    public boolean isPlainOut() {
        return getOutputFormat() == NutsOutputFormat.PLAIN;
    }

    @Override
    public boolean isStructuredOut() {
        return !isIterableOut()
                && getOutputFormat() != NutsOutputFormat.PLAIN;
    }

    @Override
    public boolean isIterableOut() {
        return getIterableFormat() != null;
    }

    @Override
    public boolean isIterableTrace() {
        return isTrace()
                && isIterableOut();
    }

    @Override
    public boolean isStructuredTrace() {
        return isTrace()
                && !isIterableOut()
                && getOutputFormat() != NutsOutputFormat.PLAIN;
    }

    @Override
    public NutsSession setTrace(boolean trace) {
        this.trace = trace;
        return this;
    }

    @Override
    public NutsSession setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    @Override
    public boolean isVerbose() {
        return isTrace() && verbose;
    }

    @Override
    public NutsSession verbose() {
        return setVerbose(true);
    }

    @Override
    public NutsSession verbose(boolean verbose) {
        return setVerbose(verbose);
    }

    @Override
    public NutsSession trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsSession trace() {
        return trace(true);
    }

    @Override
    public boolean isForce() {
        return getConfirm() == NutsConfirmationMode.YES;
    }

    @Override
    public NutsSession force() {
        return force(true);
    }

    @Override
    public NutsSession force(boolean force) {
        return setForce(force);
    }

    @Override
    public NutsSession setForce(boolean force) {
        return setConfirm(force ? NutsConfirmationMode.YES : null);
    }

    @Override
    public boolean isAsk() {
        return getConfirm() == NutsConfirmationMode.ASK;
    }

    @Override
    public NutsSession setAsk(boolean ask) {
        return setConfirm(ask ? NutsConfirmationMode.ASK : null);
    }

    @Override
    public NutsSession ask(boolean ask) {
        return setAsk(true);
    }

    @Override
    public NutsSession ask() {
        return ask(true);
    }

    @Override
    public NutsConfirmationMode getConfirm() {
        if (confirm != null) {
            return confirm;
        }
        NutsConfirmationMode c = ws.config().options().getConfirm();
        if (c != null) {
            return c;
        }
        return NutsConfirmationMode.ASK;
    }

    @Override
    public NutsSession confirm(NutsConfirmationMode confirm) {
        return setConfirm(confirm);
    }

    @Override
    public NutsSession setConfirm(NutsConfirmationMode confirm) {
        this.confirm = confirm;
        return this;
    }

    @Override
    public NutsSession setOutputFormatOptions(String... options) {
        outputFormatOptions.clear();
        return addOutputFormatOptions(options);
    }

    @Override
    public NutsSession addOutputFormatOptions(String... options) {
        for (String option : options) {
            if (!CoreStringUtils.isBlank(option)) {
                outputFormatOptions.add(option);
            }
        }
        return this;
    }

    @Override
    public String[] getOutputFormatOptions() {
        return outputFormatOptions.toArray(new String[0]);
    }

    @Override
    public String toString() {
        return "NutsSession(properties=" + properties + '}';
    }

    @Override
    public PrintStream out() {
        return out;
    }

    @Override
    public NutsSession setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    @Override
    public PrintStream err() {
        return err;
    }

    @Override
    public NutsSession setErr(PrintStream err) {
        this.err = err;
        return this;
    }

    protected PrintStream getOut0() {
        return out0;
    }

    protected NutsSession setOut0(PrintStream out0) {
        this.out0 = out0;
        return this;
    }

    protected PrintStream getErr0() {
        return err0;
    }

    protected NutsSession setErr0(PrintStream err0) {
        this.err0 = err0;
        return this;
    }

    @Override
    public void setTerminalMode(NutsTerminalMode mode) {
        ws.io().getSystemTerminal().setMode(mode);
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return ws.io().getSystemTerminal().getOutMode();
    }

    public NutsObjectPrintStream oout() {
        return new NutsObjectPrintStreamImpl(ws, this, out());
    }

    public NutsObjectPrintStream oerr() {
        return new NutsObjectPrintStreamImpl(ws, this, err());
    }

    private class NutsObjectPrintStreamImpl implements NutsObjectPrintStream {

        private NutsWorkspace ws;
        private NutsSession session;
        private PrintStream out;

        public NutsObjectPrintStreamImpl(NutsWorkspace ws, NutsSession session, PrintStream out) {
            this.ws = ws;
            this.session = session;
            this.out = out;
        }

        @Override
        public void print(Object o) {
            printObject(o, false);
        }

        @Override
        public void println(Object o) {
            printObject(o, true);
        }

        private void printObject(Object anyObject, boolean newLine) {
            NutsObjectFormat a = ws.format().object().session(session).value(anyObject);
            a.configure(true, ws.config().options().getOutputFormatOptions());
            a.configure(true, session.getOutputFormatOptions()
            );
            if (newLine) {
                a.println(out);
            } else {
                a.print(out);
            }
        }
    }
}
