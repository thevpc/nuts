/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsCommandArg;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsOutputFormat;
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
    private boolean sessionCopy = false;

    public NutsWorkspaceCommandBase(NutsWorkspace ws) {
        this.ws = ws;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsWorkspaceCommandBase other) {
        if (other != null) {
            this.session = other.getSession();
        }
        return (T) this;
    }

    //@Override
    public NutsSession getSession() {
        return session;
    }
    
    public NutsSession getSession(boolean autoCreate) {
        if(session!=null){
            return session;
        }
        if(autoCreate){
            session=ws.createSession();
        }
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



    protected void invalidateResult() {

    }

    public NutsSession getValidSessionCopy() {
        NutsSession s = getValidSession();
        if (!sessionCopy) {
            s = validSession = s.copy();
            sessionCopy = true;
        }
        return s;
    }

    public NutsSession getValidSession() {
        if (validSession == null) {
            validSession = NutsWorkspaceUtils.validateSession(ws, getSession());
            sessionCopy = true;
        }
        return validSession;
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
                getValidSessionCopy().setTrace(a.getBooleanValue());
                return true;
            }
            case "--ask": {
                getValidSessionCopy().setAsk(a.getBooleanValue());
                return true;
            }
            case "--force": {
                getValidSessionCopy().setForce(a.getBooleanValue());
                return true;
            }
            case "--trace-format": {
                getValidSessionCopy().setOutputFormat(NutsOutputFormat.valueOf(cmd.getValueFor(a).getString().toUpperCase()));
                return true;
            }
            case "--json": {
                getValidSessionCopy().setOutputFormat(NutsOutputFormat.JSON);
                return true;
            }
            case "--props": {
                getValidSessionCopy().setOutputFormat(NutsOutputFormat.PROPS);
                return true;
            }
            case "--plain": {
                getValidSessionCopy().setOutputFormat(NutsOutputFormat.PLAIN);
                return true;
            }
        }
        return false;
    }

    public abstract T parseOptions(String... args);

    public abstract T run();
}
