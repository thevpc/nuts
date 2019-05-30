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

import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.core.util.NutsPropertiesHolder;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by vpc on 2/1/17.
 */
public class DefaultNutsSession implements Cloneable, NutsSession {

    private NutsSessionTerminal terminal;
    private NutsPropertiesHolder properties = new NutsPropertiesHolder();
    private List<NutsListener> listeners = new ArrayList<>();
    private boolean trace = false;
    private boolean force = false;
    private boolean ask = false;
    private NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
    protected NutsIncrementalFormat outputCustomFormat = null;
    protected NutsWorkspace ws = null;

    public DefaultNutsSession(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsSession copy() {
        try {
            DefaultNutsSession cloned = (DefaultNutsSession) clone();
            cloned.properties = properties == null ? null : properties.copy();
            cloned.listeners = listeners == null ? null : new ArrayList<>(listeners);
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException(ws,e);
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
    public NutsSession setTerminal(NutsSessionTerminal terminal) {
        this.terminal = terminal;
        return this;
    }

    @Override
    public NutsIncrementalFormat getOutputCustomFormat() {
        return outputCustomFormat;
    }

    @Override
    public NutsSession outputCustomFormat(NutsIncrementalFormat traceFormat) {
        return setOutputCustomFormat(traceFormat);
    }

    @Override
    public NutsSession setOutputCustomFormat(NutsIncrementalFormat f) {
        if (f == null) {
            this.outputCustomFormat = null;
        } else {
            this.outputCustomFormat = f;
            this.setOutputFormat(f.getSupportedFormat());
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
    public final boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        return NutsConfigurableHelper.configure(this, ws, commandLine, skipIgnored);
    }

    @Override
    public Object configure(String... args) {
        return NutsConfigurableHelper.configure(this, ws, args,"nuts-session");
    }

    @Override
    public boolean configureFirst(NutsCommand cmdLine) {
        NutsArgument arg = cmdLine.peek();
        if (arg != null) {
            switch (arg.getKey().getString()) {
                case "--output-format": {
                    arg=cmdLine.nextString();
                    NutsOutputFormat outf = CoreCommonUtils.parseEnumString(arg.getValue().getString(), NutsOutputFormat.class, false);
                    this.setOutputFormat(outf);
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
        return this.outputFormat;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsSession setTrace(boolean trace) {
        this.trace = trace;
        return this;
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
        return force;
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
        this.force = force;
        return this;
    }

    @Override
    public boolean isAsk() {
        return ask;
    }

    @Override
    public NutsSession setAsk(boolean ask) {
        this.ask = ask;
        return this;
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
    public String toString() {
        return "NutsSession(properties=" + properties + '}';
    }

}
