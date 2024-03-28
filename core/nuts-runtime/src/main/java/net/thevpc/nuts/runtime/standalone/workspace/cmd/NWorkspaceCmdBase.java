/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NWorkspaceCmdBase<T extends NWorkspaceCmd> implements NWorkspaceCmd {

    protected NWorkspace ws;
    protected NSession session;
    private final String commandName;
    private NLog LOG;
//    protected final NutsLogger LOG;

    public NWorkspaceCmdBase(NSession session, String commandName) {
        this.ws = session.getWorkspace();
        this.session = session;
        this.commandName = commandName;
//        LOG = ws.log().of(getClass());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    protected NLogOp _LOGOP(NSession session) {
        return _LOG(session).with().session(session);
    }

    protected NLog _LOG(NSession session) {
        if (LOG == null) {
            LOG = NLog.of(getClass(), session);
        }
        return LOG;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(ws, getSession());
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NWorkspaceCmdBase other) {
        if (other != null) {
            setSession(other.getSession());
        }
        return (T) this;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    public T setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(ws, session);
        return (T) this;
    }

    @Override
    public T copySession() {
        NSession s = getSession();
        if (s != null) {
            s = s.copy();
        }
        return setSession(s);
    }

    protected void invalidateResult() {

    }

    protected NWorkspace getWorkspace() {
        return ws;
    }

    protected void setWs(NWorkspace ws) {
        this.ws = ws;
        invalidateResult();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        checkSession();
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
//        switch(a.key()) {
//        }

        if (getSession().configureFirst(cmdLine)) {
            return true;
        }
        return false;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public T configure(boolean skipUnsupported, String... args) {
        checkSession();
        configure(skipUnsupported, NCmdLine.of(args).setSession(getSession()).setCommandName(getCommandName()));
        return (T) this;
    }


}
