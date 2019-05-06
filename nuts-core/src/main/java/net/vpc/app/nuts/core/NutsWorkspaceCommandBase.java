/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import java.util.Arrays;
import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTraceFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 * @param <T>
 */
public abstract class NutsWorkspaceCommandBase<T> {

    protected NutsWorkspace ws;
    private NutsSession session;
    private NutsSession validSession;
    private boolean ask = false;
    private boolean trace = false;
    private boolean force = false;
    private NutsOutputFormat outputFormat = NutsOutputFormat.PLAIN;
    private final NutsTraceFormat[] traceFormats = new NutsTraceFormat[NutsOutputFormat.values().length];

    public NutsWorkspaceCommandBase(NutsWorkspace ws) {
        this.ws = ws;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsWorkspaceCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
            this.trace = other.isTrace();
            this.force = other.isForce();
            this.ask = other.isAsk();
            System.arraycopy(other.traceFormats, 0, this.traceFormats, 0, NutsOutputFormat.values().length);
        }
        return (T) this;
    }

    public NutsTraceFormat getTraceFormat() {
        return traceFormats[getOutputFormat().ordinal()];
    }

    public T unsetTraceFormat(NutsOutputFormat f) {
        traceFormats[f.ordinal()] = null;
        return (T) this;
    }

    public T traceFormat(NutsTraceFormat traceFormat) {
        return setTraceFormat(traceFormat);
    }

    public T setTraceFormat(NutsTraceFormat f) {
        if (f == null) {
            throw new NullPointerException();
        }
        traceFormats[f.getSupportedFormat().ordinal()] = f;
        return (T) this;
    }

    public NutsTraceFormat[] getTraceFormats() {
        return Arrays.copyOf(traceFormats, traceFormats.length);
    }

    //@Override
    public NutsSession getSession() {
        return session;
    }

    //@Override
    public T session(NutsSession session) {
        return setSession(session);
    }

    //@Override
    public T setSession(NutsSession session) {
        this.session = session;
        return (T) this;
    }

    public T outputFormat(NutsOutputFormat outputFormat) {
        return setOutputFormat(outputFormat);
    }

    public T setOutputFormat(NutsOutputFormat outputFormat) {
        if (outputFormat == null) {
            outputFormat = NutsOutputFormat.PLAIN;
        }
        this.outputFormat = outputFormat;
        return (T) this;
    }

    public NutsOutputFormat getOutputFormat() {
        return this.outputFormat;
    }

    public boolean isTrace() {
        return trace;
    }

    public T setTrace(boolean trace) {
        this.trace = trace;
        return (T) this;
    }

    public T trace(boolean trace) {
        return setTrace(trace);
    }

    public T trace() {
        return trace(true);
    }

    public boolean isForce() {
        return force;
    }

    public T force() {
        return force(true);
    }

    public T force(boolean force) {
        return setForce(force);
    }

    public T setForce(boolean force) {
        this.force = force;
        return (T) this;
    }

//    public boolean isLenient() {
//        return lenient;
//    }
//
//    public T setLenient(boolean ignoreNotFound) {
//        this.lenient = ignoreNotFound;
//        return (T) this;
//    }
//
//    public T lenient() {
//        return setLenient(true);
//    }
//
//    public T lenient(boolean lenient) {
//        return setLenient(lenient);
//    }
    protected void invalidateResult() {

    }

    public NutsSession getValidSession() {
        if (validSession == null) {
            validSession = NutsWorkspaceUtils.validateSession(ws, getSession());
        }
        return validSession;
    }

    public T json() {
        return setOutputFormat(NutsOutputFormat.JSON);
    }

    public T plain() {
        return setOutputFormat(NutsOutputFormat.PLAIN);
    }

    public T props() {
        return setOutputFormat(NutsOutputFormat.PROPS);
    }

    public boolean isAsk() {
        return ask;
    }

    public T setAsk(boolean ask) {
        this.ask = ask;
        return (T) this;
    }

    public T ask(boolean ask) {
        return setAsk(true);
    }

    public T ask() {
        return ask(true);
    }

    protected NutsWorkspace getWs() {
        return ws;
    }

    protected void setWs(NutsWorkspace ws) {
        this.ws = ws;
        invalidateResult();
    }

    protected boolean parseOption(NutsCommandArg a, NutsCommandLine cmd) {

        switch (a.strKey()) {
            case "--trace": {
                this.setTrace(a.getBooleanValue());
                return true;
            }
            case "--ask": {
                this.setAsk(a.getBooleanValue());
                return true;
            }
            case "--force": {
                this.setForce(a.getBooleanValue());
                return true;
            }
            case "--trace-format": {
                this.setOutputFormat(NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                return true;
            }
            case "--json": {
                this.setOutputFormat(NutsOutputFormat.JSON);
                return true;
            }
            case "--props": {
                this.setOutputFormat(NutsOutputFormat.PROPS);
                return true;
            }
            case "--plain": {
                this.setOutputFormat(NutsOutputFormat.PLAIN);
                return true;
            }
        }
        return false;
    }

    public abstract T parseOptions(String... args);

    public abstract T run();
}
