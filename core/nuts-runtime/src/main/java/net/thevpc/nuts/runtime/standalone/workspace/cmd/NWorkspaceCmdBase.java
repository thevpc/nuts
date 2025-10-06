/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.log.NLog;

/**
 * @param <T> Type
 * @author thevpc
 */
public abstract class NWorkspaceCmdBase<T extends NWorkspaceCmd> implements NWorkspaceCmd {

    private final String commandName;
//    protected final NutsLogger LOG;

    public NWorkspaceCmdBase(String commandName) {
        this.commandName = commandName;
//        LOG = ws.log().of(getClass());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    protected NLog _LOG() {
        return NLog.of(getClass());
    }

    public String getCommandName() {
        return commandName;
    }

    //@Override
    protected T copyFromWorkspaceCommandBase(NWorkspaceCmdBase other) {
        if (other != null) {
            //setSession(other.getSession());
        }
        return (T) this;
    }



    protected void invalidateResult() {

    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().orNull();
        if (a == null) {
            return false;
        }
//        switch(a.key()) {
//        }

        if (NSession.of().configureFirst(cmdLine)) {
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
        configure(skipUnsupported, NCmdLine.of(args).setCommandName(getCommandName()));
        return (T) this;
    }


}
