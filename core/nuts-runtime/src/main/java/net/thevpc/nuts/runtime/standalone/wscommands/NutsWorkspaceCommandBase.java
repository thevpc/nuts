/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 *
 * @author thevpc
 * @param <T> Type
 */
public abstract class NutsWorkspaceCommandBase<T extends NutsWorkspaceCommand> implements NutsWorkspaceCommand {

    protected NutsWorkspace ws;
    protected NutsSession session;
    private final String commandName;
    private NutsLogger LOG;
//    protected final NutsLogger LOG;

    public NutsWorkspaceCommandBase(NutsWorkspace ws, String commandName) {
        this.ws = ws;
        this.commandName = commandName;
//        LOG = ws.log().of(getClass());
    }

    protected NutsLoggerOp _LOGOP(NutsSession session) {
        return _LOG(session).with().session(session);
    }

    protected NutsLogger _LOG(NutsSession session) {
        if (LOG == null) {
            LOG = session.log().of(getClass());
        }
        return LOG;
    }

    protected void checkSession(){
        NutsWorkspaceUtils.checkSession(ws, getSession());
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NutsWorkspaceCommandBase other) {
        if (other != null) {
            setSession(other.getSession());
        }
        return (T) this;
    }
    
    @Override
    public NutsSession getSession() {
        return session;
    }

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    public T setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(ws, session);
        return (T) this;
    }

    @Override
    public T copySession() {
        NutsSession s = getSession();
        if (s != null) {
            s = s.copy();
        }
        return setSession(s);
    }

    protected void invalidateResult() {

    }

    protected NutsWorkspace getWorkspace() {
        return ws;
    }

    protected void setWs(NutsWorkspace ws) {
        this.ws = ws;
        invalidateResult();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        checkSession();
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
//        switch (a.getKey().getString()) {
//        }

        if (getSession().configureFirst(cmdLine)) {
            return true;
        }
        return false;
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
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, getCommandName());
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
    public boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        checkSession();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }


}
