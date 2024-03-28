/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCmdBaseRepo;

/**
 * @author thevpc
 */
public abstract class AbstractNRemoveUserCmd extends NWorkspaceCmdBaseRepo<NRemoveUserCmd> implements NRemoveUserCmd {

    protected String login;

    public AbstractNRemoveUserCmd(NSession session) {
        super(session, "remove-user");
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public NRemoveUserCmd setUsername(String username) {
        this.login = username;
        return this;
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch (a.key()) {
            default: {
                if (super.configureFirst(cmdLine)) {
                    return true;
                }
            }
        }
        return false;
    }

}
