/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgument;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.NWorkspaceCommandBaseRepo;

/**
 * @author thevpc
 */
public abstract class AbstractNRemoveUserCommand extends NWorkspaceCommandBaseRepo<NRemoveUserCommand> implements NRemoveUserCommand {

    protected String login;

    public AbstractNRemoveUserCommand(NSession ws) {
        super(ws, "remove-user");
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public NRemoveUserCommand setUsername(String username) {
        this.login = username;
        return this;
    }


    @Override
    public boolean configureFirst(NCommandLine cmdLine) {
        NArgument a = cmdLine.peek().get(session);
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